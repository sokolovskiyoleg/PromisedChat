package ru.overwrite.chat.configuration.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public record AutoMessageSettings(
        boolean enabled,
        boolean random,
        int messageInterval,
        ObjectList<List<String>> messages
) {
    public static AutoMessageSettings create(FileConfiguration config) {
        ConfigurationSection autoMessage = config.getConfigurationSection("autoMessage");

        if (!autoMessage.getBoolean("enable")) {
            return new AutoMessageSettings(false, false, 0, List.of());
        }

        ConfigurationSection messages = autoMessage.getConfigurationSection("messages");
        Set<String> keys = messages.getKeys(false);
        ObjectList<List<String>> autoMessages = new ObjectArrayList<>(keys.size());
        for (String messageName : keys) {
            autoMessages.add(messages.getStringList(messageName));
        }

        return new AutoMessageSettings(
                true,
                autoMessage.getBoolean("random"),
                autoMessage.getInt("messageInterval"),
                autoMessages
        );
    }
}
