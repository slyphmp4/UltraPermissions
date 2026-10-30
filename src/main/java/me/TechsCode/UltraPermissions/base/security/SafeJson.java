package me.TechsCode.UltraPermissions.base.security;

import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonParser;

/** Bounds structure before invoking the JSON parser, including malformed/deep input. */
public final class SafeJson {
    private SafeJson() {}

    public static JsonObject object(String text, int maximum) {
        if (text == null || text.length() > maximum) throw new IllegalArgumentException("JSON exceeds limit");
        int depth = 0, tokens = 0;
        boolean quoted = false, escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') quoted = false;
            } else if (c == '"') {
                quoted = true;
                if (++tokens > 100000) throw new IllegalArgumentException("Too many JSON values");
            } else if (c == '{' || c == '[') {
                if (++depth > 32 || ++tokens > 100000) throw new IllegalArgumentException("JSON structure exceeds limit");
            } else if (c == '}' || c == ']') {
                if (--depth < 0) throw new IllegalArgumentException("Invalid JSON structure");
            }
        }
        if (quoted || depth != 0) throw new IllegalArgumentException("Incomplete JSON structure");
        return JsonParser.parseString(text).getAsJsonObject();
    }
}
