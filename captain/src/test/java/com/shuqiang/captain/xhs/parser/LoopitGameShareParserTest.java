package com.shuqiang.captain.xhs.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoopitGameShareParserTest {

    @Test
    public void isLoopitGameShareUrlOnlyAcceptsGameSharePages() {
        Assert.assertTrue(LoopitGameShareParser.isLoopitGameShareUrl(
                "https://share.loopit.me/game/705d46bb-ff4d-49a1-b02b-a643bf5bb652?l_data=x"
        ));
        Assert.assertTrue(LoopitGameShareParser.isLoopitGameShareUrl(
                "https://share.pagesapp.net/open/game/705d46bb-ff4d-49a1-b02b-a643bf5bb652"
        ));
        Assert.assertFalse(LoopitGameShareParser.isLoopitGameShareUrl(
                "https://example.com/game/705d46bb-ff4d-49a1-b02b-a643bf5bb652"
        ));
    }

    @Test
    public void parseProjectShareResponseReadsRuntimeIndexAndVisibleImages() throws Exception {
        String json = "{"
                + "\"code\":0,"
                + "\"success\":true,"
                + "\"data\":{"
                + "\"success\":true,"
                + "\"project_id\":\"705d46bb\","
                + "\"index_url\":\"https://cdn-cf.loopit.me/public/game/705d46bb/v/workspace/dist/index.html\","
                + "\"project_desc\":\"The clothes wont work\","
                + "\"author_name\":\"Jacobi\","
                + "\"author_avatar_url\":\"https://d1xi3gzoy9qjg1.cloudfront.net/user/avatar.png?Expires=1&Signature=s\","
                + "\"front_cover\":\"https://d1xi3gzoy9qjg1.cloudfront.net/code/data/cover.png?Expires=1&Signature=s\""
                + "}"
                + "}";

        LoopitGameShareParser.ProjectShareInfo info = LoopitGameShareParser.parseProjectShareResponse(json);

        Assert.assertEquals("705d46bb", info.projectId);
        Assert.assertEquals("https://cdn-cf.loopit.me/public/game/705d46bb/v/workspace/dist/index.html", info.indexUrl);
        Assert.assertEquals("Jacobi", info.authorName);
        Assert.assertTrue(info.frontCover.contains("cover.png"));
        Assert.assertTrue(info.authorAvatarUrl.contains("avatar.png"));
    }

    @Test
    public void extractDistImageUrlsCollectsBundledRuntimeImages() {
        String indexUrl = "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/index.html";
        String html = "<html><head>"
                + "<script src=\"assets/index.js\"></script>"
                + "<link rel=\"stylesheet\" href=\"assets/index.css\">"
                + "</head><body><img src=\"public/assets/image/cover.webp\"/></body></html>";
        String jsUrl = "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/assets/index.js";
        String cssUrl = "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/assets/index.css";
        Map<String, String> textAssets = new LinkedHashMap<>();
        textAssets.put(jsUrl, "const a='public/assets/image/wolf_slim@r1.png';"
                + "const b='public/assets/image/wolf_slim@r1.png';"
                + "const dynamic=`photo_${s}.jpg`;");
        textAssets.put(cssUrl, ".hero{background:url(../image/bg_dressing_room@r1.png)}");

        List<String> imageUrls = LoopitGameShareParser.extractDistImageUrls(indexUrl, html, textAssets);

        Assert.assertEquals(3, imageUrls.size());
        Assert.assertEquals(
                "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/assets/image/cover.webp",
                imageUrls.get(0)
        );
        Assert.assertEquals(
                "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/assets/image/wolf_slim@r1.png",
                imageUrls.get(1)
        );
        Assert.assertEquals(
                "https://cdn-cf.loopit.me/public/game/p1/v/workspace/dist/image/bg_dressing_room@r1.png",
                imageUrls.get(2)
        );
    }
}
