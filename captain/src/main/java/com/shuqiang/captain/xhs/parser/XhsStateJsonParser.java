package com.shuqiang.captain.xhs.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * 统一抽取页面里的结构化状态，避免多个 parser 各自维护一套脆弱字符串裁剪。
 */
final class XhsStateJsonParser {
    private static final int MAX_STATE_SCRIPT_CHARS = 4 * 1024 * 1024;
    private static final int MAX_TOTAL_STATE_CHARS = 4 * 1024 * 1024;
    private static final int MAX_STRUCTURED_STATES = 8;
    private static final String[] STRUCTURED_STATE_MARKERS = {
            "window._ROUTER_DATA",
            "window.__INITIAL_STATE__",
            "window.__NEXT_DATA__",
            "window.__APOLLO_STATE__",
            "window.__PRELOADED_STATE__",
            "window.__NUXT__"
    };

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

    /**
     * 提取常见页面状态变量和 JSON script，供通用媒体解析器按字段语义读取。
     */
    static ArrayList<JsonObject> extractStructuredStates(Document document) {
        ArrayList<JsonObject> states = new ArrayList<>();
        if (document == null) {
            return states;
        }
        int parsedStateChars = 0;
        for (Element script : document.getElementsByTag("script")) {
            if (states.size() >= MAX_STRUCTURED_STATES) {
                break;
            }
            String data = pickScriptData(script);
            if (data == null || data.length() > MAX_STATE_SCRIPT_CHARS) {
                continue;
            }
            String scriptType = script.attr("type");
            boolean jsonScript = "application/json".equalsIgnoreCase(scriptType)
                    || "application/ld+json".equalsIgnoreCase(scriptType);
            if (!jsonScript && !containsStructuredStateMarker(data)) {
                continue;
            }
            if (parsedStateChars + data.length() > MAX_TOTAL_STATE_CHARS) {
                continue;
            }
            parsedStateChars += data.length();
            JsonObject assignedState = extractAssignedState(data);
            if (assignedState != null) {
                states.add(assignedState);
                continue;
            }
            if (!jsonScript) {
                continue;
            }
            JsonObject jsonState = parseJsonObject(data);
            if (jsonState != null) {
                states.add(jsonState);
            }
        }
        return states;
    }

    private static boolean containsStructuredStateMarker(String scriptData) {
        for (String marker : STRUCTURED_STATE_MARKERS) {
            if (scriptData.contains(marker)) {
                return true;
            }
        }
        return false;
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

    private static JsonObject extractAssignedState(String scriptData) {
        for (String marker : STRUCTURED_STATE_MARKERS) {
            int searchFrom = 0;
            while (searchFrom < scriptData.length()) {
                int markerIndex = findMarkerOutsideStringOrComment(scriptData, marker, searchFrom);
                if (markerIndex < 0) {
                    break;
                }
                int assignmentIndex = skipWhitespace(scriptData, markerIndex + marker.length());
                if (assignmentIndex >= scriptData.length() || scriptData.charAt(assignmentIndex) != '=') {
                    searchFrom = markerIndex + marker.length();
                    continue;
                }
                int objectStart = skipWhitespace(scriptData, assignmentIndex + 1);
                if (objectStart >= scriptData.length() || scriptData.charAt(objectStart) != '{') {
                    searchFrom = markerIndex + marker.length();
                    continue;
                }
                int objectEnd = findMatchingObjectEnd(scriptData, objectStart);
                if (objectEnd < 0) {
                    searchFrom = markerIndex + marker.length();
                    continue;
                }
                JsonObject state = parseJsonObject(scriptData.substring(objectStart, objectEnd + 1));
                if (state != null) {
                    return state;
                }
                searchFrom = objectEnd + 1;
            }
        }
        return null;
    }

    /**
     * 状态变量名只在脚本代码区生效，避免页面文案或注释里的 marker 触发误解析。
     */
    private static int findMarkerOutsideStringOrComment(String data, String marker, int searchFrom) {
        char quote = 0;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int i = Math.max(0, searchFrom); i <= data.length() - marker.length(); i++) {
            char current = data.charAt(i);
            char next = i + 1 < data.length() ? data.charAt(i + 1) : 0;
            if (lineComment) {
                if (current == '\n' || current == '\r') {
                    lineComment = false;
                }
                continue;
            }
            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == quote) {
                    quote = 0;
                }
                continue;
            }
            if (current == '/' && next == '/') {
                lineComment = true;
                i++;
                continue;
            }
            if (current == '/' && next == '*') {
                blockComment = true;
                i++;
                continue;
            }
            if (current == '"' || current == '\'' || current == '`') {
                quote = current;
                continue;
            }
            if (data.startsWith(marker, i)) {
                return i;
            }
        }
        return -1;
    }

    private static int skipWhitespace(String data, int startIndex) {
        int index = startIndex;
        while (index < data.length() && Character.isWhitespace(data.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * 只在 JSON 字符串之外统计花括号，避免脚本尾部逻辑或字符串内容截断状态对象。
     */
    private static int findMatchingObjectEnd(String rawData, int objectStart) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = objectStart; i < rawData.length(); i++) {
            char current = rawData.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static JsonObject parseJsonObject(String rawState) {
        try {
            JsonElement element = new JsonParser().parse(sanitizeStateJson(rawState));
            return getObject(element);
        } catch (Exception ignored) {
            return null;
        }
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
