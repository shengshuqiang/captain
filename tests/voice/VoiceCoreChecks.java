import com.shuqiang.captain.voice.ModelArchive;
import com.shuqiang.captain.voice.TranscriptBuffer;
import com.shuqiang.captain.voice.VoiceNote;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;

/** Dependency-free regression checks for the offline notebook's pure Java core. */
public final class VoiceCoreChecks {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    private static byte[] archive(String... paths) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            for (String path : paths) {
                out.putNextEntry(new ZipEntry(path));
                out.write("fixture".getBytes("UTF-8"));
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
    private static void rejects(File destination, String... paths) throws Exception {
        try {
            ModelArchive.extract(new ByteArrayInputStream(archive(paths)), destination);
            throw new AssertionError("Unsafe/invalid archive accepted");
        } catch (IOException expected) { checks++; }
    }
    public static void main(String[] args) throws Exception {
        TranscriptBuffer transcript = new TranscriptBuffer("已有文字");
        equal("已有文字\n今天", transcript.update("今天", false));
        equal("已有文字\n今天开会", transcript.update("今天开会", false));
        equal("已有文字\n今天开会", transcript.update("今天开会", true));
        equal("已有文字\n今天开会", transcript.update("", false));
        equal("已有文字\n今天开会\n记得带电脑", transcript.update("记得带电脑", false));
        equal("已有文字\n今天开会\n记得带电脑", transcript.update("", true));
        equal("已有文字\n今天开会\n记得带电脑。", transcript.update("记得带电脑。", true));
        long midnight = 1727712000000L; // 2024-10-01 00:00 UTC
        equal("2024-10-01", VoiceNote.dayAt(midnight, TimeZone.getTimeZone("Asia/Shanghai")));
        equal("2024-09-30", VoiceNote.dayAt(midnight - 1, TimeZone.getTimeZone("UTC")));
        equal("语音记事本 · 2024-10-01\n\n第一条\n\n第二条", VoiceNote.shareText("2024-10-01", Arrays.asList(
                new VoiceNote("1", "2024-10-01", midnight, "第一条"),
                new VoiceNote("2", "2024-10-01", midnight + 1, "第二条"))));
        File root = Files.createTempDirectory("voice-core-checks").toFile();
        try {
            File model = ModelArchive.extract(new ByteArrayInputStream(archive(
                    "model/am/final.mdl", "model/conf/model.conf")), new File(root, "valid"));
            equal(true, new File(model, "am/final.mdl").isFile());
            File flat = ModelArchive.extract(new ByteArrayInputStream(archive(
                    "am/final.mdl", "conf/model.conf")), new File(root, "flat"));
            equal("flat", flat.getName());
            rejects(new File(root, "traversal"), "../escaped");
            equal(false, new File(root, "escaped").exists());
            rejects(new File(root, "nested"), "model/../../escaped");
            rejects(new File(root, "absolute"), "/../../escaped");
            rejects(new File(root, "incomplete"), "model/am/final.mdl");
            rejects(new File(root, "multiple"), "a/am/final.mdl", "a/conf/model.conf",
                    "b/am/final.mdl", "b/conf/model.conf");
            String[] many = new String[4097];
            for (int i = 0; i < many.length; i++) many[i] = "file" + i;
            rejects(new File(root, "many"), many);
            // A full disk must not masquerade as a successful expansion-limit rejection.
            if (Files.getFileStore(root.toPath()).getUsableSpace() < 320L * 1024 * 1024) {
                System.out.println("SKIP: 256 MiB expansion-limit case requires 320 MiB free disk");
                System.out.println("Passed " + checks + " voice notebook core checks");
                return;
            }
            // A highly compressed oversized entry exercises the streamed expansion cap.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
                zip.putNextEntry(new ZipEntry("huge"));
                byte[] megabyte = new byte[1024 * 1024];
                for (int i = 0; i < 257; i++) zip.write(megabyte);
                zip.closeEntry();
            }
            try {
                ModelArchive.extract(new ByteArrayInputStream(bytes.toByteArray()), new File(root, "huge"));
                throw new AssertionError("Oversized archive accepted");
            } catch (IOException expected) {
                equal("模型解压后超过 256 MB", expected.getMessage());
            }
        } finally { ModelArchive.delete(root); }
        System.out.println("Passed " + checks + " voice notebook core checks");
    }
}
