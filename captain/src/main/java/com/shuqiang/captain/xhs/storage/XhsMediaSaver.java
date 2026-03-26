package com.shuqiang.captain.xhs.storage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.shuqiang.captain.xhs.model.XhsMediaItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsParseResult;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.model.XhsSaveStatus;
import com.shuqiang.captain.xhs.parser.XhsHttpClient;
import com.shuqiang.captain.xhs.parser.XhsNetworkPolicy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Response;

/**
 * 统一封装图片/视频落盘逻辑，兼容 Android 10 前后的存储模型差异。
 */
public class XhsMediaSaver {
    private static final String IMAGE_RELATIVE_PATH = Environment.DIRECTORY_PICTURES + "/Captain/Xiaohongshu/";
    private static final String VIDEO_RELATIVE_PATH = Environment.DIRECTORY_MOVIES + "/Captain/Xiaohongshu/";

    public XhsSaveItemResult save(Context context, XhsParseResult parseResult, XhsMediaItem mediaItem, int selectedIndex)
            throws IOException {
        String displayName = buildDisplayName(parseResult.getNoteId(), selectedIndex, mediaItem);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveWithMediaStore(context, mediaItem, displayName);
        }
        return saveToPublicDirectory(context, mediaItem, displayName);
    }

    private XhsSaveItemResult saveWithMediaStore(Context context, XhsMediaItem mediaItem, String displayName)
            throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String relativePath = getRelativePath(mediaItem.getMediaType());
        Uri existingUri = queryExistingUri(resolver, mediaItem.getMediaType(), displayName, relativePath);
        if (existingUri != null) {
            return new XhsSaveItemResult(mediaItem.getId(), XhsSaveStatus.SKIPPED_DUPLICATE,
                    existingUri.toString(), "文件已存在，未重复保存", displayName);
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, buildMimeType(mediaItem));
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri collectionUri = getCollectionUri(mediaItem.getMediaType());
        Uri insertedUri = resolver.insert(collectionUri, values);
        if (insertedUri == null) {
            throw new IOException("insert media store failed");
        }

        boolean saveSucceed = false;
        try (InputStream inputStream = openRemoteStream(mediaItem.getMediaUrl());
             OutputStream outputStream = resolver.openOutputStream(insertedUri, "w")) {
            if (outputStream == null) {
                throw new IOException("openOutputStream failed");
            }
            copyStream(inputStream, outputStream);
            saveSucceed = true;
        } finally {
            if (saveSucceed) {
                ContentValues finishValues = new ContentValues();
                finishValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(insertedUri, finishValues, null, null);
            } else {
                resolver.delete(insertedUri, null, null);
            }
        }
        return new XhsSaveItemResult(mediaItem.getId(), XhsSaveStatus.SUCCESS,
                insertedUri.toString(), "已保存到系统相册", displayName);
    }

    private XhsSaveItemResult saveToPublicDirectory(Context context, XhsMediaItem mediaItem, String displayName)
            throws IOException {
        File publicRoot = Environment.getExternalStoragePublicDirectory(
                mediaItem.getMediaType() == XhsMediaType.VIDEO ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES
        );
        File targetDir = new File(publicRoot, "Captain/Xiaohongshu");
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("mkdirs failed");
        }
        File targetFile = new File(targetDir, displayName);
        if (targetFile.exists()) {
            return new XhsSaveItemResult(mediaItem.getId(), XhsSaveStatus.SKIPPED_DUPLICATE,
                    Uri.fromFile(targetFile).toString(), "文件已存在，未重复保存", displayName);
        }
        try (InputStream inputStream = openRemoteStream(mediaItem.getMediaUrl());
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            copyStream(inputStream, outputStream);
        }
        MediaScannerConnection.scanFile(context,
                new String[]{targetFile.getAbsolutePath()},
                new String[]{buildMimeType(mediaItem)},
                null);
        return new XhsSaveItemResult(mediaItem.getId(), XhsSaveStatus.SUCCESS,
                Uri.fromFile(targetFile).toString(), "已保存到系统相册", displayName);
    }

    private InputStream openRemoteStream(String mediaUrl) throws IOException {
        if (!XhsNetworkPolicy.isAllowedMediaUrl(mediaUrl)) {
            throw new IOException("url is not in whitelist");
        }
        Response response = XhsHttpClient.getClient().newCall(XhsHttpClient.buildMediaRequest(mediaUrl)).execute();
        if (!response.isSuccessful() || response.body() == null) {
            response.close();
            throw new IOException("download failed, code=" + response.code());
        }
        return new ResponseBodyInputStream(response);
    }

    private Uri getCollectionUri(XhsMediaType mediaType) {
        return mediaType == XhsMediaType.VIDEO
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    private Uri queryExistingUri(ContentResolver resolver, XhsMediaType mediaType, String displayName, String relativePath) {
        Uri collectionUri = getCollectionUri(mediaType);
        String[] projection = {MediaStore.MediaColumns._ID};
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = {displayName, relativePath};
        try (Cursor cursor = resolver.query(collectionUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                return ContentUris.withAppendedId(collectionUri, id);
            }
        }
        return null;
    }

    private String getRelativePath(XhsMediaType mediaType) {
        return mediaType == XhsMediaType.VIDEO ? VIDEO_RELATIVE_PATH : IMAGE_RELATIVE_PATH;
    }

    private String buildMimeType(XhsMediaItem mediaItem) {
        String extension = mediaItem.getFileExtension() == null ? "" : mediaItem.getFileExtension().toLowerCase();
        if (mediaItem.getMediaType() == XhsMediaType.VIDEO) {
            return "video/" + ("m3u8".equals(extension) ? "mp4" : extension);
        }
        if ("png".equals(extension) || "webp".equals(extension) || "gif".equals(extension)) {
            return "image/" + extension;
        }
        return "image/jpeg";
    }

    private String buildDisplayName(String noteId, int selectedIndex, XhsMediaItem mediaItem) {
        String fileExtension = mediaItem.getFileExtension();
        if (fileExtension == null || fileExtension.isEmpty()) {
            fileExtension = mediaItem.getMediaType() == XhsMediaType.VIDEO ? "mp4" : "jpg";
        }
        return "xhs_" + noteId + "_" + selectedIndex + "." + fileExtension;
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int count;
        while ((count = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, count);
        }
        outputStream.flush();
    }

    /**
     * 响应关闭要和网络流关闭绑定，避免下载成功后连接泄漏。
     */
    private static class ResponseBodyInputStream extends InputStream {
        private final Response response;
        private final InputStream delegate;

        private ResponseBodyInputStream(Response response) {
            this.response = response;
            this.delegate = response.body().byteStream();
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                response.close();
            }
        }
    }
}
