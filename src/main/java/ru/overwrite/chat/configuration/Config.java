package ru.overwrite.chat.configuration;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import ru.overwrite.chat.configuration.data.*;

@Getter
public class Config {

    private ChatSettings chatSettings;
    private HoverSettings hoverSettings;
    private CooldownSettings cooldownSettings;
    private NewbieChatSettings newbieChatSettings;
    private AutoMessageSettings autoMessageSettings;

    public void setupConfigs(FileConfiguration config) {
        this.chatSettings = ChatSettings.create(config);
        this.hoverSettings = HoverSettings.create(config);
        this.cooldownSettings = CooldownSettings.create(config);
        this.newbieChatSettings = NewbieChatSettings.create(config);
        this.autoMessageSettings = AutoMessageSettings.create(config);
    }
}
