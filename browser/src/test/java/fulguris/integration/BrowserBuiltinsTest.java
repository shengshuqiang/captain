package fulguris.integration;

import com.anthonycr.mezzanine._Mezzanine_BlobDownload;
import com.anthonycr.mezzanine._Mezzanine_BlobHook;
import org.junit.Test;
import static org.junit.Assert.*;

/** Cross-check generated providers against packaged reader paths before opening a browser tab. */
public class BrowserBuiltinsTest {
    @Test public void downloadListenerCanReadItsBuiltinScripts() {
        assertTrue(new _Mezzanine_BlobDownload().provideJs().contains("function"));
        assertTrue(new _Mezzanine_BlobHook().provideJs().contains("function"));
    }
}
