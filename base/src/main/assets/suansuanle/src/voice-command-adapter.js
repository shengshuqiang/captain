const DIGIT_ALIASES = new Map([
  ["0", ["零", "0"]],
  ["1", ["一", "壹", "幺", "1"]],
  ["2", ["二", "两", "贰", "2"]],
  ["3", ["三", "叁", "3"]],
  ["4", ["四", "肆", "4"]],
  ["5", ["五", "伍", "5"]],
  ["6", ["六", "陆", "6"]],
  ["7", ["七", "柒", "7"]],
  ["8", ["八", "捌", "8"]],
  ["9", ["九", "玖", "9"]],
]);

const BACKSPACE_COMMANDS = ["退格", "删除", "回删", "删除一位"];
const SUBMIT_COMMANDS = ["提交", "确认", "完成", "回车", "确定"];

function normalizeTranscript(transcript) {
  return String(transcript || "")
    .replace(/\s+/g, "")
    .replace(/[，。！？、,.!?]/g, "")
    .replace(/^(请|麻烦)?/, "")
    .replace(/(一下|吧)?$/, "");
}

function matchVoiceAction(transcript) {
  const normalized = normalizeTranscript(transcript);
  if (!normalized) {
    return null;
  }
  if (BACKSPACE_COMMANDS.includes(normalized)) {
    return { type: "backspace" };
  }
  if (SUBMIT_COMMANDS.includes(normalized)) {
    return { type: "submit" };
  }
  for (const [digit, aliases] of DIGIT_ALIASES.entries()) {
    if (aliases.includes(normalized)) {
      return { type: "digit", value: digit };
    }
  }
  return null;
}

/**
 * v1 只做单次命令识别，确保动作和键盘完全同源。
 */
export class VoiceCommandAdapter {
  constructor({ nativeBridge, announcementService, onDispatch, onStateChange, onToast }) {
    this.nativeBridge = nativeBridge;
    this.announcementService = announcementService;
    this.onDispatch = onDispatch;
    this.onStateChange = onStateChange;
    this.onToast = onToast;
    this.available = nativeBridge.isVoiceAvailable();
    this.listening = false;
    this.pending = false;
    this.statusText = this.available
      ? "点击按钮后说数字、退格或提交"
      : "当前设备不支持语音控制";
    this.unsubscribe = nativeBridge.on((event) => this.handleVoiceEvent(event));
  }

  getState() {
    return {
      available: this.available,
      listening: this.listening,
      pending: this.pending,
      statusText: this.statusText,
    };
  }

  setStatus(statusText, options = {}) {
    this.statusText = statusText;
    if (typeof options.listening === "boolean") {
      this.listening = options.listening;
    }
    if (typeof options.pending === "boolean") {
      this.pending = options.pending;
    }
    this.onStateChange(this.getState());
  }

  toggleListening() {
    if (!this.available) {
      const message = "当前设备不支持语音识别";
      this.onToast(message);
      this.announcementService.announce(message);
      this.setStatus(message, { listening: false, pending: false });
      return;
    }
    if (this.listening || this.pending) {
      this.stopListening();
      return;
    }
    this.pending = true;
    this.setStatus("正在启动语音识别", { pending: true, listening: false });
    if (!this.nativeBridge.startVoiceCommand()) {
      const message = "语音识别启动失败";
      this.onToast(message);
      this.announcementService.announce(message);
      this.setStatus(message, { listening: false, pending: false });
    }
  }

  stopListening() {
    this.nativeBridge.stopVoiceCommand();
    this.setStatus("已停止语音输入", { listening: false, pending: false });
  }

  handleVoiceEvent(event) {
    const type = event && event.type;
    const payload = (event && event.payload) || {};
    if (type === "status") {
      this.handleStatus(payload);
      return;
    }
    if (type === "result") {
      this.handleResult(payload);
      return;
    }
    if (type === "error") {
      this.handleError(payload);
    }
  }

  handleStatus(payload) {
    const state = payload.state;
    const message = payload.message || "语音状态更新";
    switch (state) {
      case "listening":
      case "ready":
      case "hearing":
        this.setStatus(message, { listening: true, pending: false });
        break;
      case "processing":
      case "awaiting_permission":
        this.setStatus(message, { listening: false, pending: true });
        break;
      default:
        this.setStatus(message, { listening: false, pending: false });
        break;
    }
  }

  handleResult(payload) {
    const transcript = payload.transcript || "";
    const action = matchVoiceAction(transcript);
    if (!action) {
      const message = "没听清，请再说一次";
      this.onToast(message);
      this.announcementService.announce(message);
      this.setStatus(message, { listening: false, pending: false });
      return;
    }
    this.setStatus(`识别：${transcript}`, { listening: false, pending: false });
    this.onDispatch(action);
  }

  handleError(payload) {
    const message = payload.message || "语音识别失败，请重试";
    if (payload.code !== "client_error") {
      this.onToast(message);
      this.announcementService.announce(message);
    }
    this.setStatus(message, { listening: false, pending: false });
  }

  destroy() {
    this.stopListening();
    if (this.unsubscribe) {
      this.unsubscribe();
    }
  }
}
