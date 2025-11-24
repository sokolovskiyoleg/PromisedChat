package ru.overwrite.chat.configuration.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public record HoverSettings(
        boolean hoverEnabled,
        String hoverMessage,
        boolean clickEventEnabled,
        String clickAction,
        String clickActionValue
) {
    public static HoverSettings create(FileConfiguration config) {
        ConfigurationSection hoverText = config.getConfigurationSection("hoverText");
        ConfigurationSection clickEvent = hoverText.getConfigurationSection("clickEvent");

        boolean clickEnabled = false;
        String action = null;
        String actionValue = null;

        if (clickEvent != null) {
            clickEnabled = clickEvent.getBoolean("enable");
            action = clickEvent.getString("actionType");
            actionValue = clickEvent.getString("actionValue");
        }

        return new HoverSettings(
                hoverText.getBoolean("enable"),
                hoverText.getString("format"),
                clickEnabled,
                action,
                actionValue
        );
    }
}
