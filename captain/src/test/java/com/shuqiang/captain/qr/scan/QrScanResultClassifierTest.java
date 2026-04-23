package com.shuqiang.captain.qr.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shuqiang.captain.qr.scan.QrScanResultClassifier.ResultInfo;
import com.shuqiang.captain.qr.scan.QrScanResultClassifier.ResultType;

import org.junit.Test;

public class QrScanResultClassifierTest {
    @Test
    public void classifyShouldTreatNullAndBlankAsEmpty() {
        assertEmpty(QrScanResultClassifier.classify(null));
        assertEmpty(QrScanResultClassifier.classify(""));
        assertEmpty(QrScanResultClassifier.classify("   "));
    }

    @Test
    public void classifyShouldTreatPlainTextAsText() {
        ResultInfo resultInfo = QrScanResultClassifier.classify("hello captain");

        assertEquals(ResultType.TEXT, resultInfo.getType());
        assertEquals("hello captain", resultInfo.getDisplayText());
        assertTrue(resultInfo.canCopy());
        assertTrue(resultInfo.canGenerateQrCode());
        assertFalse(resultInfo.canOpenUrl());
    }

    @Test
    public void classifyShouldOpenOnlyHttpAndHttpsLinks() {
        assertWebUrl("http://example.com/path");
        assertWebUrl("https://example.com/path");
        assertWebUrl("  HTTPS://example.com/path  ");

        assertText("https://");
        assertText("javascript:alert(1)");
        assertText("intent://scan/#Intent;scheme=zxing;end");
        assertText("tel:10086");
    }

    @Test
    public void classifyShouldKeepDisplayTextButNormalizeUrlActionText() {
        ResultInfo resultInfo = QrScanResultClassifier.classify("  https://example.com/a  ");

        assertEquals(ResultType.WEB_URL, resultInfo.getType());
        assertEquals("  https://example.com/a  ", resultInfo.getDisplayText());
        assertEquals("https://example.com/a", resultInfo.getNormalizedText());
        assertTrue(resultInfo.canOpenUrl());
    }

    @Test
    public void classifyShouldHandleLongTextAsText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            builder.append('a');
        }
        ResultInfo resultInfo = QrScanResultClassifier.classify(builder.toString());

        assertEquals(ResultType.TEXT, resultInfo.getType());
        assertEquals(builder.toString(), resultInfo.getDisplayText());
        assertTrue(resultInfo.canGenerateQrCode());
    }

    private static void assertEmpty(ResultInfo resultInfo) {
        assertEquals(ResultType.EMPTY, resultInfo.getType());
        assertFalse(resultInfo.canCopy());
        assertFalse(resultInfo.canGenerateQrCode());
        assertFalse(resultInfo.canOpenUrl());
    }

    private static void assertWebUrl(String text) {
        ResultInfo resultInfo = QrScanResultClassifier.classify(text);
        assertEquals(ResultType.WEB_URL, resultInfo.getType());
        assertTrue(resultInfo.canOpenUrl());
        assertTrue(resultInfo.canCopy());
    }

    private static void assertText(String text) {
        ResultInfo resultInfo = QrScanResultClassifier.classify(text);
        assertEquals(ResultType.TEXT, resultInfo.getType());
        assertFalse(resultInfo.canOpenUrl());
        assertTrue(resultInfo.canCopy());
    }
}
