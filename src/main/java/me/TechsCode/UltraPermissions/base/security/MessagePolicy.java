package me.TechsCode.UltraPermissions.base.security;

import me.TechsCode.UltraPermissions.base.messaging.Message;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;

/** The client transport never carries secrets, executable updates or console control. */
public final class MessagePolicy {
    private MessagePolicy() {}

    public static boolean storageKey(String key) {
        return key != null && (key.equals("*") || key.matches("[A-Za-z0-9_.:-]{1,64}"));
    }

    public static boolean model(String model) {
        return model != null && model.matches("[A-Za-z][A-Za-z0-9_]{0,47}");
    }

    public static boolean allowed(Message message, String direction) {
        if (message == null || message.getData() == null) return false;
        JsonObject data = message.getData();
        try {
            if ("cache".equals(message.getKey())) {
                return data.size() == 2 && data.get("model").isJsonPrimitive()
                    && data.get("model").getAsJsonPrimitive().isString()
                    && data.get("dataKey").isJsonPrimitive() && data.get("dataKey").getAsJsonPrimitive().isString()
                    && model(data.get("model").getAsString()) && storageKey(data.get("dataKey").getAsString());
            }
            if (!"PROXY_TO_SERVER".equals(direction) || !"networking".equals(message.getKey()) || data.size() != 2
                    || !data.get("proxyVersion").getAsJsonPrimitive().isString() || data.get("proxyVersion").getAsString().length() > 64
                    || !data.get("serverList").isJsonArray() || data.getAsJsonArray("serverList").size() > 128) return false;
            for (var serverElement : data.getAsJsonArray("serverList")) {
                JsonObject server = serverElement.getAsJsonObject();
                if (server.size() != 4 || !server.get("name").getAsString().matches("[A-Za-z0-9_.-]{1,64}")
                        || server.get("ip").getAsString().length() > 253 || server.get("port").getAsInt() < 1
                        || server.get("port").getAsInt() > 65535 || server.getAsJsonArray("players").size() > 4096) return false;
                for (var playerElement : server.getAsJsonArray("players")) {
                    JsonObject player = playerElement.getAsJsonObject();
                    if (player.size() != 2 || !player.get("name").getAsJsonPrimitive().isString()
                            || player.get("name").getAsString().isEmpty() || player.get("name").getAsString().length() > 64
                            || player.get("name").getAsString().chars().anyMatch(Character::isISOControl)) return false;
                    java.util.UUID.fromString(player.get("uuid").getAsString());
                }
            }
            return true;
        } catch (RuntimeException invalid) {
            return false;
        }
    }
}
