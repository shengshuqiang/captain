const SOUND_POOL_SIZE = {
  start: 1,
  submit: 2,
  success: 1,
  error: 1,
  victory: 1,
  failure: 1,
  warning: 1,
  backspace: 2,
};

function buildAudioUrl(name) {
  return new URL(`../assets/${name}.wav`, import.meta.url).href;
}

/**
 * 尝试复用现有音效资源，缺资源时静默降级，不影响主流程。
 */
export class AudioManager {
  constructor() {
    this.pools = new Map();
  }

  prime() {
    [
      "start",
      "submit",
      "success",
      "error",
      "victory",
      "failure",
      "warning",
      "backspace",
      ...Array.from({ length: 10 }, (_, value) => `digit-${value}`),
    ].forEach((name) => this.ensurePool(name));
  }

  ensurePool(name) {
    if (this.pools.has(name)) {
      return this.pools.get(name);
    }
    const size = SOUND_POOL_SIZE[name] === undefined ? 2 : SOUND_POOL_SIZE[name];
    const pool = Array.from({ length: size }, () => {
      const audio = new Audio(buildAudioUrl(name));
      audio.preload = "auto";
      audio.playsInline = true;
      return audio;
    });
    this.pools.set(name, pool);
    return pool;
  }

  async play(name) {
    const pool = this.ensurePool(name);
    const audio = pool.find((item) => item.paused || item.ended) || pool[0];
    if (!audio) {
      return false;
    }
    audio.currentTime = 0;
    try {
      await audio.play();
      return true;
    } catch (error) {
      return false;
    }
  }

  stopAll() {
    this.pools.forEach((pool) => {
      pool.forEach((audio) => {
        audio.pause();
        audio.currentTime = 0;
      });
    });
  }
}
