package com.shuqiang.captain.chess.data;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChessAvatarStore {
    private static final int AVATAR_SIZE = 256;
    private static final String AVATAR_DIRECTORY = "chess/avatar";

    public String copyAvatarFromUri(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        Bitmap source = decodeBitmap(resolver, uri);
        if (source == null) {
            throw new IOException("头像读取失败");
        }
        Bitmap cropped = cropCenterSquare(source);
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, AVATAR_SIZE, AVATAR_SIZE, true);
        File directory = new File(context.getFilesDir(), AVATAR_DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("头像目录创建失败");
        }
        String fileName = "avatar_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) + ".jpg";
        File avatarFile = new File(directory, fileName);
        FileOutputStream outputStream = new FileOutputStream(avatarFile);
        try {
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
        } finally {
            outputStream.close();
            source.recycle();
            if (cropped != source) {
                cropped.recycle();
            }
            if (scaled != cropped) {
                scaled.recycle();
            }
        }
        return AVATAR_DIRECTORY + "/" + fileName;
    }

    public File resolveAvatarFile(Context context, String relativePath) {
        if (relativePath == null) {
            return null;
        }
        return new File(context.getFilesDir(), relativePath);
    }

    private Bitmap decodeBitmap(ContentResolver resolver, Uri uri) throws IOException {
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(inputStream, null, bounds);
        } finally {
            inputStream.close();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, AVATAR_SIZE, AVATAR_SIZE);
        InputStream decodeStream = resolver.openInputStream(uri);
        if (decodeStream == null) {
            return null;
        }
        try {
            return BitmapFactory.decodeStream(decodeStream, null, options);
        } finally {
            decodeStream.close();
        }
    }

    private int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight * 2 || (width / inSampleSize) > reqWidth * 2) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private Bitmap cropCenterSquare(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int left = (bitmap.getWidth() - size) / 2;
        int top = (bitmap.getHeight() - size) / 2;
        return Bitmap.createBitmap(bitmap, left, top, size, size);
    }
}
