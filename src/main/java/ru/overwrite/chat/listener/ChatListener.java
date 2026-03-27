package ru.overwrite.chat.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import ru.overwrite.chat.ChatManager;
import ru.overwrite.chat.PromisedChat;
import ru.overwrite.chat.configuration.Config;

public class ChatListener {

    private final PromisedChat plugin;
    private final ChatManager chatManager;
    private final Config pluginConfig;
    private Listener registeredListener;

    public ChatListener(PromisedChat plugin) {
        this.plugin = plugin;
        this.chatManager = new ChatManager(plugin);
        this.pluginConfig = plugin.getPluginConfig();
    }

    public void register() {
        unregister();

        EventPriority priority = pluginConfig.getChatPriority();
        Listener listener = new Listener() {
        };
        EventExecutor executor = (ignored, event) -> {
            if (!(event instanceof AsyncPlayerChatEvent chatEvent)) {
                return;
            }
            process(chatEvent);
        };

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvent(
                AsyncPlayerChatEvent.class,
                listener,
                priority,
                executor,
                plugin,
                true
        );

        this.registeredListener = listener;
    }

    public void unregister() {
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }
    }

    private void process(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (chatManager.checkNewbie(p, e)) {
            return;
        }

        String rawMessage = e.getMessage();

        chatManager.processChat(p, rawMessage, e);
    }

}
