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

import androidx.annotation.RequiresApi;

import com.shuqiang.captain.xhs.download.XhsDownloadItem;
import com.shuqiang.captain.xhs.model.XhsMediaType;
import com.shuqiang.captain.xhs.model.XhsSaveItemResult;
import com.shuqiang.captain.xhs.model.XhsSaveStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 将已验证的本地媒体原子发布到系统媒体库，发布失败时清除半成品。
 */
public final class XhsMediaPublisher {
    private static final String IMAGE_PATH = Environment.DIRECTORY_PICTURES + "/Captain/WebSniffer/";
    private static final String VIDEO_PATH = Environment.DIRECTORY_MOVIES + "/Captain/WebSniffer/";
    private static final String FILE_PATH = Environment.DIRECTORY_DOWNLOADS + "/Captain/WebSniffer/";

    public XhsSaveItemResult publish(Context context, String noteId, XhsDownloadItem item,
                                     int selectedIndex, File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.isFile() || sourceFile.length() <= 0) {
            throw new IOException("待发布媒体文件无效");
        }
        String displayName = buildDisplayName(noteId, selectedIndex, item);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? publishWithMediaStore(context, item, displayName, sourceFile)
                : publishToPublicDirectory(context, item, displayName, sourceFile);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private XhsSaveItemResult publishWithMediaStore(Context context, XhsDownloadItem item,
                                                    String displayName, File sourceFile) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String relativePath = getRelativePath(item.getMediaType());
        Uri existing = queryExistingUri(resolver, item.getMediaType(), displayName, relativePath);
        if (existing != null) {
            return duplicateResult(item, existing.toString(), displayName);
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, buildMimeType(item));
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        Uri inserted = resolver.insert(getCollectionUri(item.getMediaType()), values);
        if (inserted == null) {
            throw new IOException("创建媒体库条目失败");
        }
        boolean published = false;
        try {
            try (InputStream input = new FileInputStream(sourceFile);
                 OutputStream output = resolver.openOutputStream(inserted, "w")) {
                if (output == null) {
                    throw new IOException("打开媒体库输出失败");
                }
                copy(input, output);
            }
            ContentValues finished = new ContentValues();
            finished.put(MediaStore.MediaColumns.IS_PENDING, 0);
            if (resolver.update(inserted, finished, null, null) <= 0) {
                throw new IOException("发布媒体库条目失败");
            }
            published = true;
        } finally {
            if (!published) {
                resolver.delete(inserted, null, null);
            }
        }
        return successResult(item, inserted.toString(), displayName);
    }

    private XhsSaveItemResult publishToPublicDirectory(Context context, XhsDownloadItem item,
                                                       String displayName, File sourceFile) throws IOException {
        File publicRoot = Environment.getExternalStoragePublicDirectory(
                item.getMediaType() == XhsMediaType.VIDEO ? Environment.DIRECTORY_MOVIES
                        : (item.getMediaType() == XhsMediaType.PDF
                        ? Environment.DIRECTORY_DOWNLOADS : Environment.DIRECTORY_PICTURES));
        File targetDir = new File(publicRoot, "Captain/WebSniffer");
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("创建媒体目录失败");
        }
        File target = new File(targetDir, displayName);
        if (target.exists()) {
            return duplicateResult(item, Uri.fromFile(target).toString(), displayName);
        }
        boolean succeeded = false;
        try {
            try (InputStream input = new FileInputStream(sourceFile);
                 OutputStream output = new FileOutputStream(target)) {
                copy(input, output);
            }
            succeeded = true;
        } finally {
            if (!succeeded && target.exists()) {
                target.delete();
            }
        }
        MediaScannerConnection.scanFile(context, new String[]{target.getAbsolutePath()},
                new String[]{buildMimeType(item)}, null);
        return successResult(item, Uri.fromFile(target).toString(), displayName);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri queryExistingUri(ContentResolver resolver, XhsMediaType type,
                                 String displayName, String relativePath) {
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND "
                + MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        try (Cursor cursor = resolver.query(getCollectionUri(type),
                new String[]{MediaStore.MediaColumns._ID}, selection,
                new String[]{displayName, relativePath}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return ContentUris.withAppendedId(getCollectionUri(type),
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri getCollectionUri(XhsMediaType type) {
        if (type == XhsMediaType.VIDEO) {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        return type == XhsMediaType.PDF
                ? MediaStore.Downloads.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    private String getRelativePath(XhsMediaType type) {
        return type == XhsMediaType.VIDEO ? VIDEO_PATH : (type == XhsMediaType.PDF ? FILE_PATH : IMAGE_PATH);
    }

    private String buildMimeType(XhsDownloadItem item) {
        String extension = item.getFileExtension() == null ? "" : item.getFileExtension().toLowerCase();
        if (item.getMediaType() == XhsMediaType.PDF) {
            return "application/pdf";
        }
        if (item.getMediaType() == XhsMediaType.VIDEO) {
            return "video/" + ("webm".equals(extension) ? "webm" : "mp4");
        }
        return "png".equals(extension) || "webp".equals(extension) || "gif".equals(extension)
                ? "image/" + extension : "image/jpeg";
    }

    private String buildDisplayName(String noteId, int selectedIndex, XhsDownloadItem item) {
        String extension = item.getFileExtension();
        if (extension == null || extension.trim().isEmpty()) {
            extension = item.getMediaType() == XhsMediaType.VIDEO ? "mp4"
                    : (item.getMediaType() == XhsMediaType.PDF ? "pdf" : "jpg");
        }
        String qualitySuffix = item.getMediaType() == XhsMediaType.VIDEO && item.getQualityHeight() > 0
                ? "_" + item.getQualityHeight() + "p" : "";
        return "captain_" + sanitize(noteId) + "_" + selectedIndex + qualitySuffix + "." + extension;
    }

    private String sanitize(String value) {
        String safe = value == null ? "web" : value.replaceAll("[^A-Za-z0-9_-]", "_");
        return safe.length() > 80 ? safe.substring(0, 80) : safe;
    }

    private XhsSaveItemResult successResult(XhsDownloadItem item, String uri, String name) {
        return new XhsSaveItemResult(item.getId(), XhsSaveStatus.SUCCESS, uri, "已保存到系统相册", name);
    }

    private XhsSaveItemResult duplicateResult(XhsDownloadItem item, String uri, String name) {
        return new XhsSaveItemResult(item.getId(), XhsSaveStatus.SKIPPED_DUPLICATE,
                uri, "文件已存在，未重复保存", name);
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("媒体发布已取消");
            }
            output.write(buffer, 0, count);
        }
        output.flush();
    }
}
