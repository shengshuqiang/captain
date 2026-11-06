package com.shuqiang.captain.voice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Bounded ZIP extraction, rejecting traversal before writing any entry. */
public final class ModelArchive {
    private static final long MAX_BYTES = 256L * 1024 * 1024;
    private static final int MAX_ENTRIES = 4096;

    public static File extract(InputStream input, File staging) throws IOException {
        if (!staging.mkdirs() && !staging.isDirectory()) throw new IOException("无法创建模型目录");
        String prefix = staging.getCanonicalPath() + File.separator;
        long total = 0;
        int count = 0;
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                if (++count > MAX_ENTRIES) throw new IOException("模型文件数量过多");
                File destination = new File(staging, entry.getName());
                if (!destination.getCanonicalPath().startsWith(prefix)) {
                    throw new IOException("模型 ZIP 包含不安全路径");
                }
                if (entry.isDirectory()) {
                    if (!destination.mkdirs() && !destination.isDirectory()) throw new IOException("无法创建目录");
                } else {
                    File parent = destination.getParentFile();
                    if (!parent.mkdirs() && !parent.isDirectory()) throw new IOException("无法创建目录");
                    try (FileOutputStream out = new FileOutputStream(destination)) {
                        int n;
                        while ((n = zip.read(buffer)) != -1) {
                            total += n;
                            if (total > MAX_BYTES) throw new IOException("模型解压后超过 256 MB");
                            out.write(buffer, 0, n);
                        }
                    }
                }
            }
        }
        if (isModel(staging)) return staging;
        File[] children = staging.listFiles();
        File model = null;
        if (children != null) for (File child : children) {
            if (isModel(child)) {
                if (model != null) throw new IOException("ZIP 中只能包含一个模型");
                model = child;
            }
        }
        if (model == null) throw new IOException("请选择完整的 Vosk 中文模型 ZIP");
        return model;
    }

    private static boolean isModel(File file) {
        return new File(file, "am/final.mdl").isFile()
                && new File(file, "conf/model.conf").isFile();
    }

    public static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }
}
