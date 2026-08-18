package com.shuqiang.captain.xhs.download;

import com.shuqiang.captain.xhs.model.XhsMediaTransport;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Locale;

import okhttp3.Response;

/** 下载并校验单个直链文件，禁止将 HTML、JSON 或 HLS 清单伪装成媒体发布。 */
public final class DirectMediaDownloadEngine implements MediaDownloadEngine {
    @Override
    public boolean supports(XhsDownloadItem item) {
        return item.getTransport() == XhsMediaTransport.DIRECT_FILE;
    }

    @Override
    public XhsSaveItemResult download(MediaDownloadContext context, XhsDownloadItem item) throws IOException {
        context.transition(DownloadStage.RESOLVING, 0, 0);
        ResolvedMediaSource source = context.getSourceResolver().resolve(item);
        if (source.getTransport() != XhsMediaTransport.DIRECT_FILE) {
            throw new IOException("解析结果不是直链文件");
        }
        return downloadResolved(context, item, source, false);
    }

    /** 供 HLS 能力探测失败时复用同一下载与发布门禁。 */
    public XhsSaveItemResult downloadResolved(MediaDownloadContext context, XhsDownloadItem item,
                                              ResolvedMediaSource source, boolean explicitFallback) throws IOException {
        context.transition(DownloadStage.DIRECT_DOWNLOAD, 0, 0);
        File target = context.file("direct.part");
        try (Response response = context.getHttpSession().execute(source.getUrl(), source.getUserAgent(),
                source.getReferer(), "*/*")) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new DownloadHttpSession.HttpStatusException(response.code());
            }
            rejectTextResponse(response);
            long totalBytes = response.body().contentLength();
            try (InputStream input = response.body().byteStream();
                 FileOutputStream output = new FileOutputStream(target, false)) {
                copyWithProgress(context, input, output, totalBytes);
            }
        } catch (DownloadHttpSession.HttpStatusException exception) {
            if (item.requiresRuntimeSession() && exception.indicatesExpiredSource()) {
                throw new IOException("浏览会话已失效，请重新验证并提取");
            }
            throw exception;
        }
        validateDownloadedFile(target, item);
        context.transition(DownloadStage.PUBLISHING, 0, 0);
        XhsSaveItemResult published = context.getPublisher().publish(context.getAppContext(), context.getNoteId(),
                item, context.getSelectedIndex(), target);
        if (!explicitFallback) {
            return published;
        }
        return new XhsSaveItemResult(published.getItemId(), published.getSaveStatus(), published.getSavedUri(),
                "系统不支持该 HLS 转封装，已明确回退保存 " + source.getQualityHeight() + "P MP4",
                published.getDisplayName());
    }

    private void copyWithProgress(MediaDownloadContext context, InputStream input,
                                  FileOutputStream output, long totalBytes) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        long completed = 0;
        long lastReported = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            context.throwIfCancelled();
            output.write(buffer, 0, count);
            completed += count;
            if (completed - lastReported >= 1024 * 1024) {
                context.transition(DownloadStage.DIRECT_DOWNLOAD, toInt(completed), toInt(totalBytes));
                lastReported = completed;
            }
        }
        output.flush();
    }

    private void rejectTextResponse(Response response) throws IOException {
        String contentType = response.header("Content-Type", "").toLowerCase(Locale.US);
        if (contentType.contains("text/html") || contentType.contains("application/json")
                || contentType.contains("mpegurl")) {
            throw new IOException("直链响应不是媒体文件");
        }
    }

    private void validateDownloadedFile(File file, XhsDownloadItem item) throws IOException {
        byte[] header = new byte[16];
        int length;
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            length = input.read(header);
        }
        int firstContent = firstNonWhitespace(header, length);
        if (length < 4 || startsWithAt(header, length, "#EXTM3U", firstContent)
                || firstContent < 0 || header[firstContent] == '<' || header[firstContent] == '{'
                || header[firstContent] == '[') {
            throw new IOException("下载内容不是有效媒体文件");
        }
        if (item.getMediaType() == XhsMediaType.PDF && !startsWith(header, length, "%PDF")) {
            throw new IOException("PDF 文件头校验失败");
        }
        if (item.getMediaType() == XhsMediaType.VIDEO && !isVideoHeader(header, length, item.getFileExtension())) {
            throw new IOException("视频文件头校验失败");
        }
        if (item.getMediaType() == XhsMediaType.IMAGE && !isImageHeader(header, length)) {
            throw new IOException("图片文件头校验失败");
        }
    }

    private boolean isVideoHeader(byte[] value, int length, String extension) {
        boolean mp4 = length >= 8 && value[4] == 'f' && value[5] == 't' && value[6] == 'y' && value[7] == 'p';
        boolean webm = length >= 4 && (value[0] & 0xff) == 0x1a && (value[1] & 0xff) == 0x45
                && (value[2] & 0xff) == 0xdf && (value[3] & 0xff) == 0xa3;
        return "webm".equalsIgnoreCase(extension) ? webm : mp4;
    }

    private boolean isImageHeader(byte[] value, int length) {
        boolean jpeg = length >= 3 && (value[0] & 0xff) == 0xff && (value[1] & 0xff) == 0xd8;
        boolean png = length >= 8 && (value[0] & 0xff) == 0x89 && value[1] == 'P' && value[2] == 'N';
        boolean gif = startsWith(value, length, "GIF8");
        boolean webp = length >= 12 && startsWith(value, length, "RIFF")
                && value[8] == 'W' && value[9] == 'E' && value[10] == 'B' && value[11] == 'P';
        boolean bmp = length >= 2 && value[0] == 'B' && value[1] == 'M';
        boolean avif = length >= 12 && value[4] == 'f' && value[5] == 't' && value[6] == 'y' && value[7] == 'p';
        return jpeg || png || gif || webp || bmp || avif;
    }

    private boolean startsWith(byte[] value, int length, String prefix) {
        return startsWithAt(value, length, prefix, 0);
    }

    private boolean startsWithAt(byte[] value, int length, String prefix, int offset) {
        if (offset < 0 || length - offset < prefix.length()) {
            return false;
        }
        for (int index = 0; index < prefix.length(); index++) {
            if (value[offset + index] != (byte) prefix.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private int firstNonWhitespace(byte[] value, int length) {
        for (int index = 0; index < length; index++) {
            byte current = value[index];
            if (current != ' ' && current != '\n' && current != '\r' && current != '\t') {
                return index;
            }
        }
        return -1;
    }

    private int toInt(long value) {
        return value <= 0 ? 0 : (value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value);
    }
}
