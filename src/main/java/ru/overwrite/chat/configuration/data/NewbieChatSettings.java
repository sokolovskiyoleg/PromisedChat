package ru.overwrite.chat.configuration.data;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.chat.utils.Utils;

public record NewbieChatSettings(
        boolean enabled,
        int cooldown,
        String message,
        ObjectSet<String> commands
) {
    public static NewbieChatSettings create(FileConfiguration config) {
        ConfigurationSection newbieChat = config.getConfigurationSection("newbieChat");

        return new NewbieChatSettings(
                newbieChat.getBoolean("enable"),
                newbieChat.getInt("newbieCooldown"),
                Utils.colorize(newbieChat.getString("newbieChatMessage")),
                new ObjectOpenHashSet<>(newbieChat.getStringList("newbieCommands"))
        );
    }
}
