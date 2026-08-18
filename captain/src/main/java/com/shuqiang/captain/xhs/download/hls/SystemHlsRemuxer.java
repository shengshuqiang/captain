package com.shuqiang.captain.xhs.download.hls;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

/** 使用 Android MediaExtractor/MediaMuxer 将 H.264 + AAC TS 无损转封装为 MP4。 */
public final class SystemHlsRemuxer {
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024 * 1024;
    private static final int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

    public boolean probe(File transportStream, File probeOutput, int expectedHeight) {
        try {
            remux(transportStream, probeOutput);
            verify(probeOutput, expectedHeight, 0);
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (probeOutput.exists()) {
                probeOutput.delete();
            }
        }
    }

    public void remuxAndVerify(File transportStream, File mp4Output, int expectedHeight,
                               double expectedDurationSec) throws IOException {
        remux(transportStream, mp4Output);
        verify(mp4Output, expectedHeight, expectedDurationSec);
    }

    private void remux(File transportStream, File output) throws IOException {
        if (output.exists() && !output.delete()) {
            throw new IOException("无法清理旧的转封装文件");
        }
        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer = null;
        boolean muxerStarted = false;
        boolean succeeded = false;
        try {
            extractor.setDataSource(transportStream.getAbsolutePath());
            int trackCount = extractor.getTrackCount();
            int[] outputTracks = new int[trackCount];
            long[] lastPresentationTimes = new long[trackCount];
            Arrays.fill(outputTracks, -1);
            Arrays.fill(lastPresentationTimes, -1);
            muxer = new MediaMuxer(output.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int selectedCount = 0;
            boolean hasVideo = false;
            boolean hasAudio = false;
            int bufferSize = DEFAULT_BUFFER_SIZE;
            for (int track = 0; track < trackCount; track++) {
                MediaFormat format = extractor.getTrackFormat(track);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (!isSupportedMime(mime)) {
                    continue;
                }
                if (mime.startsWith("video/")) {
                    if (hasVideo) {
                        throw new IOException("暂不支持多个视频轨");
                    }
                    hasVideo = true;
                } else {
                    if (hasAudio) {
                        throw new IOException("暂不支持多个音频轨");
                    }
                    hasAudio = true;
                }
                extractor.selectTrack(track);
                outputTracks[track] = muxer.addTrack(format);
                selectedCount++;
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    bufferSize = Math.max(bufferSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
                }
            }
            if (!hasVideo || !hasAudio || selectedCount != 2) {
                throw new IOException("TS 必须同时包含单个 H.264 视频轨和 AAC 音频轨");
            }
            bufferSize = Math.min(bufferSize, MAX_BUFFER_SIZE);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            muxer.start();
            muxerStarted = true;
            long firstPresentationTime = -1;
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new IOException("系统转封装已取消");
                }
                int inputTrack = extractor.getSampleTrackIndex();
                if (inputTrack < 0) {
                    break;
                }
                int outputTrack = inputTrack < outputTracks.length ? outputTracks[inputTrack] : -1;
                if (outputTrack < 0) {
                    extractor.advance();
                    continue;
                }
                buffer.clear();
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }
                long sampleTime = extractor.getSampleTime();
                if (firstPresentationTime < 0) {
                    firstPresentationTime = Math.max(sampleTime, 0);
                }
                long normalizedTime = Math.max(0, sampleTime - firstPresentationTime);
                if (lastPresentationTimes[inputTrack] > normalizedTime) {
                    throw new IOException("TS 样本时间戳不连续，无法安全转封装");
                }
                if (lastPresentationTimes[inputTrack] == normalizedTime) {
                    normalizedTime = lastPresentationTimes[inputTrack] + 1;
                }
                lastPresentationTimes[inputTrack] = normalizedTime;
                bufferInfo.set(0, sampleSize, normalizedTime, extractor.getSampleFlags());
                muxer.writeSampleData(outputTrack, buffer, bufferInfo);
                extractor.advance();
            }
            succeeded = true;
        } catch (RuntimeException exception) {
            throw new IOException("系统转封装失败", exception);
        } finally {
            extractor.release();
            if (muxer != null) {
                if (muxerStarted) {
                    try {
                        muxer.stop();
                    } catch (RuntimeException exception) {
                        succeeded = false;
                    }
                }
                try {
                    muxer.release();
                } catch (RuntimeException exception) {
                    succeeded = false;
                }
            }
            if (!succeeded && output.exists()) {
                output.delete();
            }
        }
        if (!succeeded || !output.isFile() || output.length() <= 0) {
            throw new IOException("系统转封装未生成有效 MP4");
        }
    }

    private void verify(File mp4File, int expectedHeight, double expectedDurationSec) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mp4File.getAbsolutePath());
            boolean hasVideo = false;
            boolean hasAudio = false;
            int actualHeight = 0;
            long durationUs = 0;
            for (int track = 0; track < extractor.getTrackCount(); track++) {
                MediaFormat format = extractor.getTrackFormat(track);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if ("video/avc".equalsIgnoreCase(mime)) {
                    hasVideo = true;
                    if (format.containsKey(MediaFormat.KEY_HEIGHT)) {
                        actualHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                    }
                } else if (isAac(mime)) {
                    hasAudio = true;
                }
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    durationUs = Math.max(durationUs, format.getLong(MediaFormat.KEY_DURATION));
                }
            }
            if (!hasVideo || !hasAudio) {
                throw new IOException("MP4 缺少 H.264 视频轨或 AAC 音频轨");
            }
            if (expectedHeight > 0 && actualHeight > 0 && Math.abs(expectedHeight - actualHeight) > 16) {
                throw new IOException("MP4 分辨率与所选画质不一致");
            }
            if (expectedDurationSec > 0 && durationUs > 0) {
                double actualDurationSec = durationUs / 1_000_000d;
                double tolerance = Math.max(10, expectedDurationSec * 0.03d);
                if (Math.abs(actualDurationSec - expectedDurationSec) > tolerance) {
                    throw new IOException("MP4 时长校验失败");
                }
            }
        } catch (RuntimeException exception) {
            throw new IOException("MP4 轨道校验失败", exception);
        } finally {
            extractor.release();
        }
    }

    private boolean isSupportedMime(String mime) {
        return "video/avc".equalsIgnoreCase(mime) || isAac(mime);
    }

    private boolean isAac(String mime) {
        String lower = mime == null ? "" : mime.toLowerCase(Locale.US);
        return "audio/mp4a-latm".equals(lower) || "audio/aac".equals(lower);
    }
}
