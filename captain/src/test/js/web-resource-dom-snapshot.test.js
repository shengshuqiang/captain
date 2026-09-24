// 运行：node captain/src/test/js/web-resource-dom-snapshot.test.js
// 执行生产 Java 中的真实脚本，验证嗅探不产生网络请求或修改图片加载属性。
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/java/com/shuqiang/captain/xhs/parser/WebResourceDomSnapshot.java'), 'utf8');
const template = source.split('private static final String TEMPLATE =')[1].split('static final String SCRIPT')[0];
const script = [...template.matchAll(/"(?:\\.|[^"\\])*"/g)].map(m => JSON.parse(m[0])).join('');
function media(url, extras = {}) {
  return Object.freeze({tagName: 'VIDEO', currentSrc: url, src: url, readyState: 4,
    paused: false, videoWidth: 640, videoHeight: 360,
    getBoundingClientRect: () => ({width: 640, height: 360}),
    getAttribute: () => '', ...extras});
}
let video = media('https://fixture.example/ad.mp4');
let imageQueries = 0;
const img = media('', {tagName: 'IMG', loading: 'lazy', complete: false,
  getAttribute: key => key === 'data-src' ? '/original.jpg' : key === 'srcset' ? '/one.jpg 1x, /two.jpg 2x' : ''});
const context = {URL, Image: function () {throw Error('unexpected image probe');},
  fetch() {throw Error('unexpected request');},
  document: {baseURI: 'https://fixture.example/page', title: 'fixture', querySelectorAll(q) {
    if (q === 'img') {imageQueries++; return [img];}
    if (q === 'video,video source') return [video, video];
    return [];
  }}, window: {getComputedStyle: () => ({display: 'block', visibility: 'visible', opacity: '1'})}};
context.getComputedStyle = context.window.getComputedStyle;
function run(videoOnly = false) {
  return JSON.parse(vm.runInNewContext(script.replace('__CAPTAIN_VIDEO_ONLY__', String(videoOnly)), context));
}
const before = run();
assert.equal(before.items.filter(x => x.kind === 'video').length, 1);
assert.equal(before.items.filter(x => x.kind === 'image').length, 3);
assert.equal(img.loading, 'lazy');
assert.deepEqual(before.playingUrls, [video.src, video.src]);
const queries = imageQueries;
video = media('https://fixture.example/main.mp4');
assert.equal(run(true).items[0].url, video.src);
assert.equal(imageQueries, queries, 'periodic player observation must not scan images');
video = media('blob:https://fixture.example/test');
assert.equal(run(true).items.length, 0, 'blob must not become a downloadable URL');
video = media('https://fixture.example/stream.m3u8');
assert.equal(run(true).items[0].url, video.src);
console.log('PASS: passive snapshot, lazy images, metadata-only discovery, deduplication, delayed player replacement, blob and HLS');
