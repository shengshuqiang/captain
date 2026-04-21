package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 统一抽取页面里的 `window.__INITIAL_STATE__`，避免多个 parser 各自维护一套脆弱字符串裁剪。
 */
final class XhsStateJsonParser {

    private XhsStateJsonParser() {
    }

    static JsonObject extractInitialState(Document document) {
        for (Element script : document.getElementsByTag("script")) {
            String data = pickScriptData(script);
            if (data == null || !data.contains("window.__INITIAL_STATE__=")) {
                continue;
            }
            String rawState = data.substring(data.indexOf("window.__INITIAL_STATE__=")
                    + "window.__INITIAL_STATE__=".length());
            rawState = sanitizeStateJson(rawState);
            if (rawState.isEmpty()) {
                return null;
            }
            try {
                return new JsonParser().parse(rawState).getAsJsonObject();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static JsonObject getObject(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        return getObject(parent.get(memberName));
    }

    static JsonObject getObject(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    static JsonArray getArray(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        JsonElement element = parent.get(memberName);
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }
        return element.getAsJsonArray();
    }

    static String getString(JsonObject parent, String memberName) {
        if (parent == null || !parent.has(memberName)) {
            return null;
        }
        JsonElement element = parent.get(memberName);
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        String value = element.getAsString();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String pickScriptData(Element script) {
        if (script == null) {
            return null;
        }
        String data = script.data();
        if (data != null && !data.trim().isEmpty()) {
            return data.trim();
        }
        String html = script.html();
        return html == null || html.trim().isEmpty() ? null : html.trim();
    }

    private static String sanitizeStateJson(String rawState) {
        if (rawState == null) {
            return "";
        }
        String sanitized = rawState.trim();
        int functionIndex = sanitized.indexOf(";(");
        if (functionIndex >= 0) {
            sanitized = sanitized.substring(0, functionIndex);
        }
        if (sanitized.endsWith(";")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized.replace(":undefined", ":null")
                .replace(":void 0", ":null")
                .replace(":!0", ":true")
                .replace(":!1", ":false");
    }
}
