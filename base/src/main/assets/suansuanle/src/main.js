import { AnnouncementService } from "./announcement-service.js";
import { AudioManager } from "./audio-manager.js";
import { EquationCanvas } from "./equation-canvas.js";
import { GameActionDispatcher } from "./game-action-dispatcher.js";
import { GameController } from "./game-controller.js";
import { QUESTION_OPTIONS, RANGE_OPTIONS, TIME_OPTIONS, formatTimer } from "./game-logic.js";
import { NativeVoiceBridge } from "./native-voice.js";
import { VoiceCommandAdapter } from "./voice-command-adapter.js";

const app = document.getElementById("app");

app.innerHTML = `
  <div class="ssl-page">
    <header class="ssl-header">
      <div class="ssl-title">算算乐</div>
      <button id="settingsButton" class="ssl-ghost-button" type="button">重新开始</button>
    </header>
    <section class="ssl-topbar">
      <div class="ssl-card ssl-timer-card">
        <div class="ssl-timer-emoji">💣</div>
        <div id="timerText" class="ssl-timer-text">00:00:000</div>
      </div>
      <div class="ssl-card ssl-score-card">
        <div id="scoreTitle" class="ssl-score-title">0/20</div>
        <div id="scoreDesc" class="ssl-score-desc">◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯◯</div>
      </div>
    </section>
    <canvas id="equationCanvas" class="ssl-canvas"></canvas>
    <div id="feedbackText" class="ssl-feedback"></div>
    <section id="keyboard" class="ssl-keyboard"></section>
    <section id="voicePanel" class="ssl-card ssl-voice-panel">
      <button id="voiceButton" class="ssl-voice-button" type="button">点击说话</button>
      <div id="voiceStatus" class="ssl-voice-status">正在初始化语音能力</div>
    </section>
    <section id="finishPanel" class="ssl-card ssl-finish" hidden>
      <div id="finishTitle" class="ssl-finish-title">挑战完成</div>
      <div id="finishSummary" class="ssl-finish-summary"></div>
      <div id="finishDetail" class="ssl-finish-detail"></div>
      <button id="replayButton" class="ssl-primary-button" type="button">再来一局</button>
      <div id="recordGrid" class="ssl-record-grid"></div>
    </section>
    <div id="settingsModal" class="ssl-modal" hidden>
      <form id="settingsForm" class="ssl-modal-card">
        <div class="ssl-modal-title">请选择运算类型和范围</div>
        <div class="ssl-modal-desc">语音控制默认支持数字、退格和提交，其他命令暂不开放。</div>
        <div class="ssl-field">
          <label class="ssl-field-label">运算类型</label>
          <div class="ssl-radio-row">
            <label class="ssl-radio-chip"><input type="radio" name="operation" value="add" checked />进位加法</label>
            <label class="ssl-radio-chip"><input type="radio" name="operation" value="sub" />退位减法</label>
          </div>
        </div>
        <div class="ssl-field">
          <label for="rangeSelect" class="ssl-field-label">运算范围</label>
          <select id="rangeSelect" class="ssl-select" name="range">
            ${RANGE_OPTIONS.map((option) => `<option value="${option}">${option}以内</option>`).join("")}
          </select>
        </div>
        <div class="ssl-field">
          <label class="ssl-field-label">时长</label>
          <div class="ssl-radio-row">
            ${TIME_OPTIONS.map(
              (option, index) =>
                `<label class="ssl-radio-chip"><input type="radio" name="limitTime" value="${option.value}" ${
                  index === 0 ? "checked" : ""
                } />${option.label}</label>`
            ).join("")}
          </div>
        </div>
        <div class="ssl-field">
          <label class="ssl-field-label">题量</label>
          <div class="ssl-radio-row">
            ${QUESTION_OPTIONS.map(
              (option, index) =>
                `<label class="ssl-radio-chip"><input type="radio" name="questionCount" value="${option}" ${
                  index === 0 ? "checked" : ""
                } />${option}题</label>`
            ).join("")}
          </div>
        </div>
        <button class="ssl-primary-button" type="submit">确认开始</button>
      </form>
    </div>
    <div id="toast" class="ssl-toast" hidden></div>
    <div id="liveRegion" class="sr-only" role="status" aria-live="polite" aria-atomic="true"></div>
  </div>
`;

const refs = {
  timerText: document.getElementById("timerText"),
  scoreTitle: document.getElementById("scoreTitle"),
  scoreDesc: document.getElementById("scoreDesc"),
  equationCanvas: document.getElementById("equationCanvas"),
  feedbackText: document.getElementById("feedbackText"),
  keyboard: document.getElementById("keyboard"),
  voiceButton: document.getElementById("voiceButton"),
  voiceStatus: document.getElementById("voiceStatus"),
  voicePanel: document.getElementById("voicePanel"),
  finishPanel: document.getElementById("finishPanel"),
  finishTitle: document.getElementById("finishTitle"),
  finishSummary: document.getElementById("finishSummary"),
  finishDetail: document.getElementById("finishDetail"),
  replayButton: document.getElementById("replayButton"),
  recordGrid: document.getElementById("recordGrid"),
  settingsButton: document.getElementById("settingsButton"),
  settingsModal: document.getElementById("settingsModal"),
  settingsForm: document.getElementById("settingsForm"),
  toast: document.getElementById("toast"),
  liveRegion: document.getElementById("liveRegion"),
};

const equationCanvas = new EquationCanvas(refs.equationCanvas);
const nativeVoiceBridge = new NativeVoiceBridge();
const announcementService = new AnnouncementService({
  liveRegion: refs.liveRegion,
  nativeBridge: nativeVoiceBridge,
});
const audioManager = new AudioManager();

let toastTimer = null;
function showToast(message) {
  refs.toast.textContent = message;
  refs.toast.hidden = false;
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => {
    refs.toast.hidden = true;
  }, 1800);
}

const gameController = new GameController({
  announcementService,
  audioManager,
  onStateChange: render,
  onToast: showToast,
  onGameStarted: () => {
    voiceCommandAdapter.setStatus("点击按钮后说数字、退格或提交", {
      listening: false,
      pending: false,
    });
  },
  onGameFinished: () => {
    voiceCommandAdapter.stopListening();
  },
});

const actionDispatcher = new GameActionDispatcher((action) => {
  gameController.dispatchGameAction(action);
});

const voiceCommandAdapter = new VoiceCommandAdapter({
  nativeBridge: nativeVoiceBridge,
  announcementService,
  onDispatch: (action) => actionDispatcher.dispatch(action),
  onStateChange: (voiceState) => gameController.setVoiceState(voiceState),
  onToast: showToast,
});
gameController.setVoiceState(voiceCommandAdapter.getState());

function buildKeyboard() {
  const keys = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"];
  refs.keyboard.innerHTML = `
    ${keys
      .map(
        (value) =>
          `<button class="ssl-key" type="button" data-action="digit" data-value="${value}">${value}</button>`
      )
      .join("")}
    <button class="ssl-key ssl-key--action" type="button" data-action="backspace">退格</button>
    <button class="ssl-key ssl-key--action ssl-key--submit" type="button" data-action="submit">提交</button>
  `;
}

buildKeyboard();

refs.keyboard.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }
  const action = button.dataset.action;
  if (action === "digit") {
    actionDispatcher.digit(button.dataset.value);
  } else if (action === "backspace") {
    actionDispatcher.backspace();
  } else if (action === "submit") {
    actionDispatcher.submit();
  }
});

refs.voiceButton.addEventListener("click", () => {
  voiceCommandAdapter.toggleListening();
});

refs.replayButton.addEventListener("click", () => {
  gameController.replay();
});

refs.settingsButton.addEventListener("click", () => {
  voiceCommandAdapter.stopListening();
  gameController.openSettings();
});

refs.recordGrid.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-record-index]");
  if (!button) {
    return;
  }
  gameController.selectRecord(Number(button.dataset.recordIndex));
});

refs.settingsForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const formData = new FormData(refs.settingsForm);
  gameController.startGame({
    isAdd: formData.get("operation") !== "sub",
    numberRange: Number(formData.get("range")),
    limitTime: Number(formData.get("limitTime")),
    questionCount: Number(formData.get("questionCount")),
  });
});

document.addEventListener("keydown", (event) => {
  const snapshot = gameController.getSnapshot();
  if (snapshot.isModalOpen || (snapshot.game && snapshot.game.finished)) {
    return;
  }
  if (event.key >= "0" && event.key <= "9") {
    event.preventDefault();
    actionDispatcher.digit(event.key);
    return;
  }
  if (event.key === "Backspace") {
    event.preventDefault();
    actionDispatcher.backspace();
    return;
  }
  if (event.key === "Enter") {
    event.preventDefault();
    actionDispatcher.submit();
  }
});

window.addEventListener("beforeunload", () => {
  voiceCommandAdapter.destroy();
  gameController.destroy();
  equationCanvas.destroy();
});

function render(snapshot) {
  const game = snapshot.game;
  refs.settingsModal.hidden = !snapshot.isModalOpen;
  refs.timerText.textContent = formatTimer(game ? game.remainingTime : snapshot.settings.limitTime);
  refs.scoreTitle.textContent = game ? game.scoreTitle : `0/${snapshot.settings.questionCount}`;
  refs.scoreDesc.textContent = game ? game.scoreDesc : "◯".repeat(snapshot.settings.questionCount);
  refs.feedbackText.textContent = game ? game.feedback : "";

  const keyboardButtons = refs.keyboard.querySelectorAll("button");
  keyboardButtons.forEach((button) => {
    const action = button.dataset.action;
    if (action === "digit") {
      button.disabled = game ? game.keyboard.numDisabled : true;
    } else if (action === "backspace") {
      button.disabled = game ? game.keyboard.backspaceDisabled : true;
    } else if (action === "submit") {
      button.disabled = game ? game.keyboard.submitDisabled : true;
    }
  });
  refs.keyboard.hidden = !game || game.finished;
  refs.voicePanel.hidden = !game || game.finished;

  const voiceState = snapshot.voiceState;
  refs.voiceStatus.textContent = voiceState.statusText;
  refs.voiceButton.disabled = !voiceState.available;
  refs.voiceButton.textContent = voiceState.listening || voiceState.pending ? "停止听写" : "点击说话";

  refs.finishPanel.hidden = !game || !game.finished;
  if (game && game.finished) {
    const stats = game.stats;
    const allCorrect = stats.rightCount === snapshot.settings.questionCount && stats.emptyCount === 0;
    refs.finishTitle.textContent = allCorrect ? "挑战成功" : "再接再厉";
    refs.finishSummary.textContent = game.finishAnnouncement;
    refs.finishDetail.textContent = `对${stats.rightCount} 错${stats.wrongCount} 改${stats.modifyCount} 空${stats.emptyCount}`;
    refs.recordGrid.innerHTML = game.records
      .map((record, index) => {
        const stateClass =
          record.numC === ""
            ? "ssl-record-button--empty"
            : record.isRight
            ? "ssl-record-button--right"
            : "ssl-record-button--wrong";
        const modifyClass = record.isModify ? "ssl-record-button--modify" : "";
        const selectedClass = game.selectedRecordIndex === index ? "ssl-record-button--selected" : "";
        return `<button class="ssl-record-button ${stateClass} ${modifyClass} ${selectedClass}" type="button" data-record-index="${index}">${
          index + 1
        }</button>`;
      })
      .join("");
  } else {
    refs.recordGrid.innerHTML = "";
  }

  equationCanvas.draw({
    question: game ? game.displayedQuestion : null,
    answerInput: game ? game.displayedAnswerInput : "",
    revealAllHints: game ? game.revealAllHints : false,
  });
}

render(gameController.getSnapshot());
