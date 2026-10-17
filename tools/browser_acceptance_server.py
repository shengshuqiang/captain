#!/usr/bin/env python3
"""Local, synthetic MP4/HLS fixtures for playback-complete and authenticated-save acceptance."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit, parse_qs
import argparse
import json
import subprocess
import threading

COOKIE = "captain_browser_fixture=accepted"
DEFAULT_PORT = 18765
EVENT_LOCK = threading.Lock()


def generate_media(root):
    """Generate owned test media with video and audio, no external download required."""
    root.mkdir(parents=True, exist_ok=True)
    video = root / "complete.mp4"
    if not video.exists():
        subprocess.run(["ffmpeg", "-v", "error", "-f", "lavfi", "-i",
                        "testsrc2=size=320x180:rate=24", "-f", "lavfi", "-i",
                        "sine=frequency=440:sample_rate=44100", "-t", "6",
                        "-c:v", "libx264", "-preset", "ultrafast", "-crf", "30",
                        "-pix_fmt", "yuv420p", "-c:a", "aac", "-movflags", "+faststart",
                        str(video)], check=True)
    manifest = root / "complete.m3u8"
    if not manifest.exists():
        subprocess.run(["ffmpeg", "-v", "error", "-i", str(video), "-c", "copy",
                        "-hls_time", "2", "-hls_list_size", "0", "-hls_segment_filename",
                        str(root / "segment-%02d.ts"), str(manifest)], check=True)


def handler(root):
    class FixtureHandler(BaseHTTPRequestHandler):
        def log_message(self, *_args):
            pass

        def event(self, kind, **fields):
            with EVENT_LOCK:
                with (root / "events.jsonl").open("a") as stream:
                    stream.write(json.dumps({"event": kind, **fields}) + "\n")

        def do_HEAD(self):
            self.do_GET()

        def do_POST(self):
            if urlsplit(self.path).path != "/ended":
                self.send_error(404)
                return
            self.event("web_playback_ended")
            self.send_response(204)
            self.end_headers()

        def do_GET(self):
            url = urlsplit(self.path)
            if url.path in ["/", "/watch"]:
                hls = parse_qs(url.query).get("hls") == ["1"]
                media = "/media/complete.m3u8" if hls else "/media/complete.mp4"
                page = ("<!doctype html><html><head><meta name='viewport' content='width=device-width'>"
                        "<title>船长视频保存验收</title></head><body>"
                        "<h2>船长视频保存验收</h2><p id='state'>等待播放</p>"
                        f"<video id='video' width='320' height='180' controls playsinline src='{media}'></video>"
                        "<p><button style='font-size:22px;padding:12px' onclick='video.play()'>播放网页视频</button></p>"
                        "<script>video.onended=()=>{document.getElementById('state').textContent='网页视频播放结束';"
                        "fetch('/ended',{method:'POST'});};</script></body></html>")
                body = page.encode()
                self.send_response(200)
                self.send_header("Content-Type", "text/html; charset=utf-8")
                self.send_header("Set-Cookie", COOKIE + "; Path=/; SameSite=Lax")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                if self.command != "HEAD":
                    self.wfile.write(body)
                return
            if not url.path.startswith("/media/"):
                self.send_error(404)
                return
            name = Path(url.path).name
            file = root / name
            authorized = COOKIE in (self.headers.get("Cookie") or "")
            self.event("media_request", resource=name, authorized=authorized,
                       range=self.headers.get("Range"), referer=bool(self.headers.get("Referer")))
            if not authorized:
                self.send_error(403)
                return
            if not file.is_file() or file.suffix not in [".mp4", ".m3u8", ".ts"]:
                self.send_error(404)
                return
            data = file.read_bytes()
            start, end = 0, len(data) - 1
            byte_range = self.headers.get("Range")
            if byte_range:
                first, last = byte_range.removeprefix("bytes=").split("-", 1)
                start = int(first or 0)
                end = min(end, int(last)) if last else end
            if start > end:
                self.send_response(416)
                self.send_header("Content-Range", f"bytes */{len(data)}")
                self.end_headers()
                return
            self.send_response(206 if byte_range else 200)
            self.send_header("Content-Type", {".mp4": "video/mp4", ".m3u8": "application/vnd.apple.mpegurl",
                                              ".ts": "video/mp2t"}[file.suffix])
            self.send_header("Accept-Ranges", "bytes")
            self.send_header("Content-Length", str(end - start + 1))
            if byte_range:
                self.send_header("Content-Range", f"bytes {start}-{end}/{len(data)}")
            self.end_headers()
            if self.command != "HEAD":
                try:
                    self.wfile.write(data[start:end + 1])
                except (BrokenPipeError, ConnectionResetError):
                    pass

    return FixtureHandler


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, required=True)
    parser.add_argument("--port", type=int, default=DEFAULT_PORT)
    args = parser.parse_args()
    generate_media(args.root)
    server = ThreadingHTTPServer(("127.0.0.1", args.port), handler(args.root))
    print(f"Fixture ready: http://127.0.0.1:{args.port}/watch", flush=True)
    server.serve_forever()
