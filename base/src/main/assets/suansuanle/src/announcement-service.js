/**
 * 播报优先级：原生 TTS -> speechSynthesis -> aria-live。
 */
export class AnnouncementService {
  constructor({ liveRegion, nativeBridge }) {
    this.liveRegion = liveRegion;
    this.nativeBridge = nativeBridge;
    this.liveRegionTimer = null;
  }

  announce(text, options = {}) {
    if (!text) {
      return "none";
    }
    const { interrupt = true } = options;
    this.updateLiveRegion(text);
    if (this.nativeBridge.speak(text, interrupt)) {
      return "native";
    }
    if ("speechSynthesis" in window && "SpeechSynthesisUtterance" in window) {
      try {
        if (interrupt) {
          window.speechSynthesis.cancel();
        }
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = "zh-CN";
        utterance.rate = 1;
        utterance.pitch = 1;
        window.speechSynthesis.speak(utterance);
        return "speech";
      } catch (error) {
        // 继续回退到 live region。
      }
    }
    return "live";
  }

  stop() {
    this.nativeBridge.stopSpeak();
    if ("speechSynthesis" in window) {
      window.speechSynthesis.cancel();
    }
  }

  updateLiveRegion(text) {
    if (!this.liveRegion) {
      return;
    }
    window.clearTimeout(this.liveRegionTimer);
    this.liveRegion.textContent = "";
    this.liveRegionTimer = window.setTimeout(() => {
      this.liveRegion.textContent = text;
    }, 30);
  }
}
