import {
  buildQuestionAnnouncement,
  buildScoreDesc,
  createEmptyRecord,
  createRecord,
  formatDurationLabel,
  generateQuestions,
  getRecordStats,
  QUESTION_OPTIONS,
  RANGE_OPTIONS,
  requiredAnswerLength,
  summarizeSettings,
  TIME_OPTIONS,
} from "./game-logic.js";

const DEFAULT_SETTINGS = {
  isAdd: true,
  numberRange: RANGE_OPTIONS[0],
  limitTime: TIME_OPTIONS[0].value,
  questionCount: QUESTION_OPTIONS[0],
};

export class GameController {
  constructor({
    announcementService,
    audioManager,
    onStateChange,
    onToast,
    onGameStarted = () => {},
    onGameFinished = () => {},
  }) {
    this.announcementService = announcementService;
    this.audioManager = audioManager;
    this.onStateChange = onStateChange;
    this.onToast = onToast;
    this.onGameStarted = onGameStarted;
    this.onGameFinished = onGameFinished;
    this.timerId = null;
    this.sessionSeed = 1;
    this.pendingTaskIds = new Set();
    this.state = {
      settings: { ...DEFAULT_SETTINGS },
      isModalOpen: true,
      voiceState: {
        available: false,
        listening: false,
        pending: false,
        statusText: "当前设备不支持语音控制",
      },
      game: null,
    };
  }

  notify() {
    this.onStateChange(this.getSnapshot());
  }

  setVoiceState(voiceState) {
    this.state.voiceState = { ...voiceState };
    this.notify();
  }

  openSettings() {
    this.stopTimer();
    this.clearPendingTasks();
    this.announcementService.stop();
    this.audioManager.stopAll();
    this.state.game = null;
    this.state.isModalOpen = true;
    this.notify();
  }

  startGame(nextSettings) {
    this.stopTimer();
    this.clearPendingTasks();
    this.announcementService.stop();
    this.audioManager.prime();
    const settings = { ...nextSettings };
    const questions = generateQuestions(settings);
    const sessionId = this.sessionSeed;
    this.sessionSeed += 1;
    this.state.settings = settings;
    this.state.isModalOpen = false;
    this.state.game = {
      sessionId,
      settings,
      questions,
      currentIndex: 0,
      answerInput: "",
      currentModified: false,
      records: [],
      feedback: "",
      finished: false,
      locked: false,
      selectedRecordIndex: 0,
      remainingTime: settings.limitTime,
      deadlineAt: Date.now() + settings.limitTime,
      questionStartRemainingTime: settings.limitTime,
    };
    this.onGameStarted();
    this.audioManager.play("start");
    this.startTimer();
    this.announceCurrentQuestion(true);
    this.notify();
  }

  startTimer() {
    this.stopTimer();
    this.timerId = window.setInterval(() => {
      if (!this.state.game || this.state.game.finished) {
        return;
      }
      this.syncRemainingTime();
      if (this.state.game.remainingTime <= 0) {
        this.finishGame("timeout");
        return;
      }
      this.notify();
    }, 100);
  }

  stopTimer() {
    if (this.timerId) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  syncRemainingTime() {
    if (!this.state.game) {
      return;
    }
    this.state.game.remainingTime = Math.max(0, this.state.game.deadlineAt - Date.now());
  }

  dispatchGameAction(action) {
    const game = this.state.game;
    if (!game || game.finished || game.locked) {
      return;
    }
    this.syncRemainingTime();
    this.announcementService.stop();
    if (action.type === "digit") {
      this.handleDigit(action.value);
      return;
    }
    if (action.type === "backspace") {
      this.handleBackspace();
      return;
    }
    if (action.type === "submit") {
      this.handleSubmit();
    }
  }

  handleDigit(value) {
    const game = this.state.game;
    const question = this.getCurrentQuestion();
    if (!game || !question) {
      return;
    }
    if (game.answerInput.length >= requiredAnswerLength(question)) {
      return;
    }
    game.feedback = "";
    game.answerInput += value;
    this.audioManager.play(`digit-${value}`);
    this.notify();
  }

  handleBackspace() {
    const game = this.state.game;
    if (!game || game.answerInput.length === 0) {
      return;
    }
    game.answerInput = game.answerInput.slice(0, -1);
    game.currentModified = true;
    game.feedback = "";
    this.audioManager.play("backspace");
    this.notify();
  }

  handleSubmit() {
    const game = this.state.game;
    const question = this.getCurrentQuestion();
    if (!game || !question) {
      return;
    }
    if (game.answerInput.length < requiredAnswerLength(question)) {
      const message = "请先填写答案";
      this.audioManager.play("warning");
      this.onToast(message);
      this.announcementService.announce(message);
      game.feedback = message;
      this.notify();
      return;
    }

    game.locked = true;
    const isRight = Number(game.answerInput) === question.numAnswer;
    const sessionId = game.sessionId;
    const elapsedTime = Math.max(
      0,
      game.questionStartRemainingTime - game.remainingTime
    );
    const record = createRecord(
      question,
      game.answerInput,
      isRight,
      game.currentModified,
      elapsedTime
    );
    game.records.push(record);
    const feedback = isRight ? "回答正确" : `回答错误，正确答案是 ${question.numAnswer}`;
    game.feedback = feedback;
    this.audioManager.play("submit");
    this.scheduleGameTask(sessionId, 220, () => {
      this.audioManager.play(isRight ? "success" : "error");
    });
    this.scheduleGameTask(sessionId, 320, () => {
      this.announcementService.announce(feedback);
    });

    if (game.currentIndex >= game.questions.length - 1) {
      this.scheduleGameTask(sessionId, 1000, () => this.finishGame("answered"));
    } else {
      this.scheduleGameTask(sessionId, 1300, () => this.moveToNextQuestion());
    }
    this.notify();
  }

  moveToNextQuestion() {
    const game = this.state.game;
    if (!game || game.finished) {
      return;
    }
    game.currentIndex += 1;
    game.answerInput = "";
    game.currentModified = false;
    game.feedback = "";
    game.locked = false;
    game.questionStartRemainingTime = game.remainingTime;
    this.announceCurrentQuestion(false, { interrupt: false });
    this.notify();
  }

  finishGame(reason) {
    const game = this.state.game;
    if (!game || game.finished) {
      return;
    }
    this.syncRemainingTime();
    this.stopTimer();
    this.clearPendingTasks();
    game.finished = true;
    game.locked = true;
    if (reason === "timeout") {
      this.completeEmptyRecords();
      game.feedback = "时间到";
      this.audioManager.play("failure");
      this.announcementService.announce(
        `时间到。${this.buildFinishAnnouncement()}`
      );
    } else {
      const stats = getRecordStats(game.records);
      const allCorrect =
        stats.rightCount === game.questions.length && stats.emptyCount === 0;
      this.audioManager.play(allCorrect ? "victory" : "failure");
      this.announcementService.announce(this.buildFinishAnnouncement(), {
        interrupt: false,
      });
    }
    game.selectedRecordIndex = 0;
    this.onGameFinished();
    this.notify();
  }

  completeEmptyRecords() {
    const game = this.state.game;
    if (!game) {
      return;
    }
    for (let index = game.currentIndex; index < game.questions.length; index += 1) {
      const question = game.questions[index];
      const alreadyRecorded = game.records.some((record) => record.id === question.id);
      if (alreadyRecorded) {
        continue;
      }
      game.records.push(createEmptyRecord(question));
    }
  }

  announceCurrentQuestion(includeSummary, options = {}) {
    const game = this.state.game;
    const question = this.getCurrentQuestion();
    if (!game || !question) {
      return;
    }
    const prefix = includeSummary
      ? `开始，${summarizeSettings(game.settings)}。`
      : "";
    this.announcementService.announce(`${prefix}${buildQuestionAnnouncement(question)}`, options);
  }

  buildFinishAnnouncement() {
    const game = this.state.game;
    if (!game) {
      return "";
    }
    const stats = getRecordStats(game.records);
    const usedTime = game.settings.limitTime - game.remainingTime;
    return `本局结束，用时${formatDurationLabel(usedTime)}，答对${stats.rightCount}题，答错${stats.wrongCount}题，改动${stats.modifyCount}题，空白${stats.emptyCount}题。`;
  }

  selectRecord(index) {
    const game = this.state.game;
    if (!game || !game.finished) {
      return;
    }
    game.selectedRecordIndex = index;
    this.notify();
  }

  replay() {
    this.startGame({ ...this.state.settings });
  }

  getCurrentQuestion() {
    if (!this.state.game) {
      return null;
    }
    return this.state.game.questions[this.state.game.currentIndex] || null;
  }

  getDisplayedQuestion() {
    const game = this.state.game;
    if (!game) {
      return null;
    }
    if (!game.finished) {
      return this.getCurrentQuestion();
    }
    return game.records[game.selectedRecordIndex] || null;
  }

  getSnapshot() {
    this.syncRemainingTime();
    const game = this.state.game;
    const settings = game && game.settings ? game.settings : this.state.settings;
    const currentQuestion = this.getCurrentQuestion();
    const displayedQuestion = this.getDisplayedQuestion();
    const answerInput =
      game && game.finished
        ? String(displayedQuestion && displayedQuestion.numC !== undefined ? displayedQuestion.numC : "")
        : game
        ? game.answerInput
        : "";
    const scoreTitle = game ? `${game.records.length}/${settings.questionCount}` : `0/${settings.questionCount}`;
    const scoreDesc = buildScoreDesc(settings.questionCount, game ? game.records : []);
    const answerLength = currentQuestion ? requiredAnswerLength(currentQuestion) : 0;
    const stats = game && game.finished ? getRecordStats(game.records) : null;
    return {
      settings,
      isModalOpen: this.state.isModalOpen,
      voiceState: this.state.voiceState,
      game: game
        ? {
            remainingTime: game.remainingTime,
            feedback: game.feedback,
            finished: game.finished,
            answerInput: game.answerInput,
            scoreTitle,
            scoreDesc,
            records: game.records,
            selectedRecordIndex: game.selectedRecordIndex,
            questionIndex: game.currentIndex,
            keyboard: {
              numDisabled: !currentQuestion || game.answerInput.length >= answerLength || game.finished,
              backspaceDisabled: !game.answerInput.length || game.finished,
              submitDisabled:
                !currentQuestion ||
                game.answerInput.length !== answerLength ||
                game.finished ||
                game.locked,
            },
            displayedQuestion,
            displayedAnswerInput: answerInput,
            revealAllHints: game.finished,
            stats,
            finishAnnouncement: game.finished ? this.buildFinishAnnouncement() : "",
          }
        : null,
    };
  }

  destroy() {
    this.stopTimer();
    this.clearPendingTasks();
    this.announcementService.stop();
  }

  scheduleGameTask(sessionId, delay, task) {
    const taskId = window.setTimeout(() => {
      this.pendingTaskIds.delete(taskId);
      const game = this.state.game;
      if (!game || game.sessionId !== sessionId || typeof task !== "function") {
        return;
      }
      task();
    }, delay);
    this.pendingTaskIds.add(taskId);
  }

  clearPendingTasks() {
    this.pendingTaskIds.forEach((taskId) => window.clearTimeout(taskId));
    this.pendingTaskIds.clear();
  }
}
