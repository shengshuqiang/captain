export class NativeVoiceBridge {
  constructor() {
    this.listeners = new Set();
    window.SuanSuanLeNative = window.SuanSuanLeNative || {};
    window.SuanSuanLeNative.onVoiceEvent = (event) => {
      this.listeners.forEach((listener) => listener(event || {}));
    };
  }

  hasBridge() {
    return Boolean(window.SuanSuanLeBridge);
  }

  on(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  speak(text, interrupt = true) {
    if (!text || !this.hasBridge() || typeof window.SuanSuanLeBridge.speak !== "function") {
      return false;
    }
    try {
      window.SuanSuanLeBridge.speak(text, interrupt);
      return true;
    } catch (error) {
      return false;
    }
  }

  stopSpeak() {
    if (!this.hasBridge() || typeof window.SuanSuanLeBridge.stopSpeak !== "function") {
      return;
    }
    try {
      window.SuanSuanLeBridge.stopSpeak();
    } catch (error) {
      // 原生桥异常时交给 H5 回退播报。
    }
  }

  startVoiceCommand() {
    if (!this.hasBridge() || typeof window.SuanSuanLeBridge.startVoiceCommand !== "function") {
      return false;
    }
    try {
      window.SuanSuanLeBridge.startVoiceCommand();
      return true;
    } catch (error) {
      return false;
    }
  }

  stopVoiceCommand() {
    if (!this.hasBridge() || typeof window.SuanSuanLeBridge.stopVoiceCommand !== "function") {
      return false;
    }
    try {
      window.SuanSuanLeBridge.stopVoiceCommand();
      return true;
    } catch (error) {
      return false;
    }
  }

  isVoiceAvailable() {
    if (!this.hasBridge() || typeof window.SuanSuanLeBridge.isVoiceAvailable !== "function") {
      return false;
    }
    try {
      return Boolean(window.SuanSuanLeBridge.isVoiceAvailable());
    } catch (error) {
      return false;
    }
  }
}
