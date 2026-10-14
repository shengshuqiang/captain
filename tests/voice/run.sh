#!/usr/bin/env bash
set -euo pipefail
project_root="$(cd "$(dirname "$0")/../.." && pwd)"
test_classes="$(mktemp -d)"
trap 'rm -rf "$test_classes"' EXIT
voice_source="$project_root/captain/src/main/java/com/shuqiang/captain/voice"
javac -encoding UTF-8 -source 8 -target 8 -d "$test_classes" \
  "$voice_source/VoiceNote.java" "$voice_source/ModelArchive.java" \
  "$voice_source/TranscriptBuffer.java" "$project_root/tests/voice/VoiceCoreChecks.java"
java -cp "$test_classes" VoiceCoreChecks
python3 "$project_root/tests/voice/run-session-checks.py"
