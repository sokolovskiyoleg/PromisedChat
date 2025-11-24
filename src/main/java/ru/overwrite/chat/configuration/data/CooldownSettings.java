package ru.overwrite.chat.configuration.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.chat.utils.Utils;

public record CooldownSettings(
        long localRateLimit,
        long globalRateLimit,
        String tooFastMessage
) {
    public static CooldownSettings create(FileConfiguration config) {
        ConfigurationSection cooldown = config.getConfigurationSection("cooldown");

        return new CooldownSettings(
                cooldown.getLong("localCooldown"),
                cooldown.getLong("globalCooldown"),
                Utils.colorize(cooldown.getString("cooldownMessage"))
        );
    }
}
