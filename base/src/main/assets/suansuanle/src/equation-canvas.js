import { requiredAnswerLength } from "./game-logic.js";

function buildRem() {
  const rootFontSize = window.parseFloat(
    getComputedStyle(document.documentElement).fontSize
  );
  return (value) => value * rootFontSize;
}

/**
 * 保留原页面的算式演示方式，方便题目过程和结果播报对齐。
 */
export class EquationCanvas {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    this.rem = buildRem();
    this.handleResize = this.handleResize.bind(this);
    window.addEventListener("resize", this.handleResize);
    this.handleResize();
  }

  handleResize() {
    const ratio = window.devicePixelRatio || 1;
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = Math.max(1, Math.floor(rect.width * ratio));
    this.canvas.height = Math.max(1, Math.floor(rect.height * ratio));
    this.ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  }

  clear() {
    const rect = this.canvas.getBoundingClientRect();
    this.ctx.clearRect(0, 0, rect.width, rect.height);
  }

  draw(snapshot) {
    this.clear();
    if (!snapshot || !snapshot.question) {
      this.drawPlaceholder();
      return;
    }
    const { question, answerInput = "", revealAllHints = false } = snapshot;
    this.drawInlineEquation(question, answerInput);
    this.drawColumnEquation(question, answerInput, revealAllHints);
  }

  drawPlaceholder() {
    const rect = this.canvas.getBoundingClientRect();
    this.ctx.fillStyle = "#5d5666";
    this.ctx.font = `600 ${this.rem(0.26)}px Arial`;
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";
    this.ctx.fillText("请选择设置后开始答题", rect.width / 2, rect.height / 2);
  }

  drawInlineEquation(question, answerInput) {
    const ctx = this.ctx;
    const rect = this.canvas.getBoundingClientRect();
    const fontSize = this.rem(0.52);
    const boxGap = this.rem(0.05);
    const top = this.rem(0.15);

    ctx.save();
    ctx.font = `${fontSize}px Arial`;
    ctx.fillStyle = "#111111";
    ctx.textBaseline = "top";

    const prefix = `${question.numA} ${question.isAdd ? "+" : "-"} ${question.numB} = `;
    const prefixMetrics = ctx.measureText(prefix);
    const answerLength = requiredAnswerLength(question);
    const answerText = String(question.numAnswer);
    let answerWidth = 0;
    for (let index = 0; index < answerLength; index += 1) {
      answerWidth += ctx.measureText(answerText[index]).width;
      if (index < answerLength - 1) {
        answerWidth += boxGap;
      }
    }
    const totalWidth = prefixMetrics.width + answerWidth + this.rem(0.1);
    let cursorX = (rect.width - totalWidth) / 2;

    ctx.fillText(prefix, cursorX, top);
    cursorX += prefixMetrics.width;

    for (let index = 0; index < answerLength; index += 1) {
      const digit = answerText[index];
      const digitMetrics = ctx.measureText(digit);
      const charWidth = digitMetrics.width;
      ctx.strokeStyle = "#8d8599";
      ctx.strokeRect(cursorX, top - boxGap, charWidth + boxGap, fontSize + boxGap * 2);
      if (answerInput[index]) {
        ctx.fillStyle = answerText.startsWith(answerInput.slice(0, index + 1))
          ? "#2fa85f"
          : "#d0342c";
        ctx.fillText(answerInput[index], cursorX + boxGap / 2, top);
        ctx.fillStyle = "#111111";
      }
      cursorX += charWidth + boxGap;
    }
    ctx.restore();
  }

  drawColumnEquation(question, answerInput, revealAllHints) {
    const ctx = this.ctx;
    const rect = this.canvas.getBoundingClientRect();
    const rem = this.rem;
    const startX = rem(0.05);
    const startY = rem(0.95);
    const gapY = rem(0.45);
    const linePadding = rem(0.12);
    const mainFont = `600 ${rem(0.34)}px Arial`;
    const hintFont = `bold ${rem(0.24)}px Arial`;

    const topLine = String(question.numA).split("").join(" ");
    const secondLine = `${question.isAdd ? "+" : "-"}${String(question.numB)
      .padStart(String(question.numA).length, " ")
      .split("")
      .join(" ")}`;
    const answerLength = requiredAnswerLength(question);
    const answerLine = revealAllHints
      ? String(question.numAnswer).split("").join(" ")
      : Array.from({ length: answerLength }, (_, index) =>
          answerInput[index] === undefined ? "_" : answerInput[index]
        ).join(" ");

    ctx.save();
    ctx.font = mainFont;
    ctx.fillStyle = "#1a1620";
    ctx.textBaseline = "middle";
    ctx.textAlign = "right";

    const maxWidth = Math.max(
      ctx.measureText(topLine).width,
      ctx.measureText(secondLine).width,
      ctx.measureText(answerLine).width
    );
    const baseX = (rect.width + maxWidth) / 2;

    ctx.fillText(topLine, baseX, startY);
    if (!question.isAdd) {
      this.drawHintMarks(baseX, startY, question, topLine, secondLine, hintFont, revealAllHints);
    }

    ctx.fillText(secondLine, baseX, startY + gapY);
    if (question.isAdd) {
      this.drawHintMarks(baseX, startY + gapY, question, topLine, secondLine, hintFont, revealAllHints);
    }

    ctx.beginPath();
    ctx.lineWidth = rem(0.05);
    ctx.moveTo(baseX - maxWidth - linePadding, startY + gapY + rem(0.22));
    ctx.lineTo(baseX + linePadding, startY + gapY + rem(0.22));
    ctx.strokeStyle = "#2c2734";
    ctx.stroke();

    ctx.fillStyle = revealAllHints ? "#2fa85f" : "#1a1620";
    ctx.fillText(answerLine, baseX, startY + gapY * 2);
    ctx.restore();
  }

  drawHintMarks(baseX, currentY, question, topLine, secondLine, hintFont, revealAllHints) {
    if (!revealAllHints) {
      return;
    }
    const ctx = this.ctx;
    const flags = question.numFlags || [];
    ctx.save();
    ctx.font = hintFont;
    ctx.textAlign = "center";
    for (let index = 1; index < flags.length; index += 1) {
      if (!flags[index]) {
        continue;
      }
      const sourceLine = question.isAdd
        ? secondLine.slice(-index * 2 + 1)
        : topLine.slice(-(index + 1) * 2 + 1);
      const offset = ctx.measureText(sourceLine).width;
      const hintX = baseX - offset;
      ctx.fillStyle = question.isAdd ? "#d0342c" : "#111111";
      ctx.fillText(question.isAdd ? "1" : "●", hintX, currentY - this.rem(0.22));
    }
    ctx.restore();
  }

  destroy() {
    window.removeEventListener("resize", this.handleResize);
  }
}
