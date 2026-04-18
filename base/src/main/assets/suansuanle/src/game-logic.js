export const RANGE_OPTIONS = [20, 30, 40, 50, 60, 80, 100];
export const TIME_OPTIONS = [
  { value: 2 * 60 * 1000, label: "2分钟" },
  { value: 5 * 60 * 1000, label: "5分钟" },
  { value: 10 * 60 * 1000, label: "10分钟" },
];
export const QUESTION_OPTIONS = [20, 50, 100];

export function deconstructTime(ms) {
  const safeMs = Math.max(0, ms);
  return {
    mm: String(Math.floor(safeMs / 60000)).padStart(2, "0"),
    ss: String(Math.floor((safeMs % 60000) / 1000)).padStart(2, "0"),
    ms: String(Math.floor(safeMs % 1000)).padStart(3, "0"),
  };
}

export function formatTimer(ms) {
  const { mm, ss, ms: milli } = deconstructTime(ms);
  return `${mm}:${ss}:${milli}`;
}

export function formatLimitTime(ms) {
  return `${Math.floor(ms / 60000)}分钟`;
}

export function formatDurationLabel(ms) {
  const safeMs = Math.max(0, ms);
  const totalSeconds = Math.floor(safeMs / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  if (minutes > 0 && seconds > 0) {
    return `${minutes}分${seconds}秒`;
  }
  if (minutes > 0) {
    return `${minutes}分`;
  }
  return `${seconds}秒`;
}

export function buildScoreDesc(totalCount, records) {
  const marks = records.map((record) => (record.isRight ? "✔️" : "❌")).join("");
  const remaining = Math.max(0, totalCount - records.length);
  return `${marks}${remaining ? "◯".repeat(remaining) : ""}`;
}

function buildNumArr(value) {
  if (!Number.isInteger(value)) {
    return [0];
  }
  if (value === 0) {
    return [0];
  }
  const digits = [];
  let current = Math.abs(value);
  while (current > 0) {
    digits.push(current % 10);
    current = Math.floor(current / 10);
  }
  return digits;
}

export function addABFlags(numA, numB) {
  const digitsA = buildNumArr(numA);
  const digitsB = buildNumArr(numB);
  const maxLength = Math.max(digitsA.length, digitsB.length);
  const flags = [false];
  let currentA = 0;
  let currentB = 0;
  let place = 1;
  for (let index = 0; index < maxLength; index += 1) {
    currentA += (digitsA[index] === undefined ? 0 : digitsA[index]) * place;
    currentB += (digitsB[index] === undefined ? 0 : digitsB[index]) * place;
    place *= 10;
    flags.push(currentA + currentB >= place);
  }
  return flags;
}

export function subABFlags(numA, numB) {
  const digitsA = buildNumArr(numA);
  const digitsB = buildNumArr(numB);
  const flags = [false];
  let currentA = 0;
  let currentB = 0;
  let place = 1;
  for (let index = 0; index < digitsA.length; index += 1) {
    currentA += digitsA[index] * place;
    currentB += (digitsB[index] === undefined ? 0 : digitsB[index]) * place;
    place *= 10;
    flags.push(currentA - currentB < 0);
  }
  return flags;
}

function shuffle(items) {
  const list = items.slice();
  for (let index = list.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [list[index], list[swapIndex]] = [list[swapIndex], list[index]];
  }
  return list;
}

function createQuestion(numA, numB, isAdd, order) {
  const numAnswer = isAdd ? numA + numB : numA - numB;
  return {
    id: `${isAdd ? "add" : "sub"}-${numA}-${numB}-${order}`,
    order,
    isAdd,
    numA,
    numB,
    numAnswer,
    numFlags: isAdd ? addABFlags(numA, numB) : subABFlags(numA, numB),
  };
}

export function generateQuestions(settings) {
  const bank = [];
  if (settings.isAdd) {
    for (let numB = 0; numB <= settings.numberRange; numB += 1) {
      for (let numA = 0; numA <= settings.numberRange; numA += 1) {
        bank.push({ numA, numB });
      }
    }
  } else {
    for (let numA = 0; numA <= settings.numberRange; numA += 1) {
      for (let numB = 0; numB <= numA; numB += 1) {
        bank.push({ numA, numB });
      }
    }
  }
  return shuffle(bank)
    .slice(0, settings.questionCount)
    .map((question, index) =>
      createQuestion(question.numA, question.numB, settings.isAdd, index + 1)
    );
}

export function requiredAnswerLength(question) {
  return String(question.numAnswer).length;
}

export function createRecord(question, input, isRight, isModify, elapsedMs) {
  return {
    ...question,
    numC: input === "" ? "" : Number(input),
    isRight,
    isModify,
    time: Math.max(0, elapsedMs),
  };
}

export function createEmptyRecord(question) {
  return {
    ...question,
    numC: "",
    isRight: false,
    isModify: false,
    time: 0,
  };
}

export function summarizeSettings(settings) {
  return `${settings.numberRange}以内${settings.isAdd ? "进位加法" : "退位减法"}，限时${formatLimitTime(
    settings.limitTime
  )}，共${settings.questionCount}题`;
}

export function buildQuestionAnnouncement(question) {
  return `第${question.order}题，${question.numA}${question.isAdd ? "加" : "减"}${question.numB}等于多少？`;
}

export function getRecordStats(records) {
  return records.reduce(
    (stats, record) => {
      if (record.numC === "") {
        stats.emptyCount += 1;
        return stats;
      }
      if (record.isRight) {
        stats.rightCount += 1;
      } else {
        stats.wrongCount += 1;
      }
      if (record.isModify) {
        stats.modifyCount += 1;
      }
      return stats;
    },
    { rightCount: 0, wrongCount: 0, modifyCount: 0, emptyCount: 0 }
  );
}
