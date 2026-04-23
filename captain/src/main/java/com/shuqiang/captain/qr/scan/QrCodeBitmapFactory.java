package com.shuqiang.captain.qr.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuqiang.captain.qr.utils.Utils;

public final class QrCodeBitmapFactory {
    private QrCodeBitmapFactory() {
    }

    /**
     * 只生成普通文本二维码预览，不承接旧的信息二维马保存/分享流程。
     */
    @Nullable
    public static Bitmap createTextQrCode(@NonNull Context context, @Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        if (TextUtils.isEmpty(text.trim())) {
            return null;
        }
        return Utils.createQRCodeBitmap(context, text);
    }
}
