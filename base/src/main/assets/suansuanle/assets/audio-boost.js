// Phone speakers could barely hear the original WebAudio gain envelope.
// Boost only the audible peak gain while preserving the near-zero fade values.
(function () {
  var GAIN_MULTIPLIER = 4;
  var MAX_GAIN = 0.18;
  var SILENT_FLOOR = 0.00011;

  function boostGain(value) {
    if (typeof value !== "number" || value <= SILENT_FLOOR) {
      return value;
    }
    return Math.min(value * GAIN_MULTIPLIER, MAX_GAIN);
  }

  function patchAudioContext(Ctor) {
    if (!Ctor || !Ctor.prototype || Ctor.prototype.__suansuanleGainPatched) {
      return;
    }
    var originalCreateGain = Ctor.prototype.createGain;
    if (typeof originalCreateGain !== "function") {
      return;
    }
    Ctor.prototype.createGain = function () {
      var gainNode = originalCreateGain.apply(this, arguments);
      var gainParam = gainNode && gainNode.gain;
      if (
        !gainParam ||
        gainParam.__suansuanleGainPatched ||
        typeof gainParam.exponentialRampToValueAtTime !== "function"
      ) {
        return gainNode;
      }
      var originalRamp = gainParam.exponentialRampToValueAtTime.bind(gainParam);
      gainParam.exponentialRampToValueAtTime = function (value, endTime) {
        return originalRamp(boostGain(value), endTime);
      };
      gainParam.__suansuanleGainPatched = true;
      return gainNode;
    };
    Ctor.prototype.__suansuanleGainPatched = true;
  }

  patchAudioContext(window.AudioContext);
  patchAudioContext(window.webkitAudioContext);
})();
