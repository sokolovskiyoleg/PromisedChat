package ru.overwrite.chat.configuration.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public record ChatSettings(
        String localFormat,
        String globalFormat,
        int chatRadius,
        boolean forceGlobal,
        Object2ObjectMap<String, String> perGroupColor
) {
    public static ChatSettings create(FileConfiguration config) {
        ConfigurationSection format = config.getConfigurationSection("format");
        String localFormat = format.getString("local");
        String globalFormat = format.getString("global");
        int chatRadius = format.getInt("localRadius");
        boolean forceGlobal = format.getBoolean("forceGlobal");
        ConfigurationSection donatePlaceholders = config.getConfigurationSection("donatePlaceholders");
        Set<String> keys = donatePlaceholders.getKeys(false);
        Object2ObjectMap<String, String> perGroupColor = new Object2ObjectOpenHashMap<>(keys.size());
        for (String groupName : keys) {
            String color = donatePlaceholders.getString(groupName);
            perGroupColor.put(groupName, color);
        }

        return new ChatSettings(
                localFormat,
                globalFormat,
                chatRadius,
                forceGlobal,
                perGroupColor
        );
    }
}

