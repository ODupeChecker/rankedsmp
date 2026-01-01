package com.example.rankedsmp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtils {
    private TextUtils() {
    }

    public static Component color(String message) {
        if (message == null) {
            return Component.empty();
        }
        String normalized = message.replace('&', '§');
        return LegacyComponentSerializer.legacySection().deserialize(normalized);
    }
}
