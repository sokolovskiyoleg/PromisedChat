package ru.overwrite.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.chat.configuration.Config;
import ru.overwrite.chat.configuration.data.ChatSettings;
import ru.overwrite.chat.configuration.data.CooldownSettings;
import ru.overwrite.chat.configuration.data.HoverSettings;
import ru.overwrite.chat.configuration.data.NewbieChatSettings;
import ru.overwrite.chat.utils.ExpiringMap;
import ru.overwrite.chat.utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {

    private final PromisedChat plugin;
    private final Config pluginConfig;
    private final ExpiringMap<String, Long> localCooldowns;
    private final ExpiringMap<String, Long> globalCooldowns;

    public ChatListener(PromisedChat plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.localCooldowns = new ExpiringMap<>(pluginConfig.getCooldownSettings().localRateLimit(), TimeUnit.MILLISECONDS);
        this.globalCooldowns = new ExpiringMap<>(pluginConfig.getCooldownSettings().globalRateLimit(), TimeUnit.MILLISECONDS);
    }

    private final String[] searchList = {"<player>", "<prefix>", "<suffix>", "<dph>"};

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (checkNewbie(p, e)) {
            return;
        }
        ChatSettings chatSettings = pluginConfig.getChatSettings();
        CooldownSettings cooldownSettings = pluginConfig.getCooldownSettings();
        HoverSettings hoverSettings = pluginConfig.getHoverSettings();
        String name = p.getName();
        String message = e.getMessage();
        String prefix = plugin.getChat().getPlayerPrefix(p);
        String suffix = plugin.getChat().getPlayerSuffix(p);
        String globalMessage = removeGlobalPrefix(message, chatSettings);
        String[] replacementList = {name, prefix, suffix, getDonatePlaceholder(p, chatSettings)};
        if (chatSettings.forceGlobal() || (message.charAt(0) == '!' && !globalMessage.isBlank())) {
            if (processCooldown(e, p, name, globalCooldowns, cooldownSettings.globalRateLimit(), cooldownSettings)) {
                return;
            }
            String globalFormat = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(chatSettings.globalFormat(), searchList, replacementList)));
            String colorizedMessage = Utils.formatByPerm(p, globalMessage);
            if (hoverSettings.hoverEnabled()) {
                e.setCancelled(true);
                sendHover(p, replacementList, globalFormat, Bukkit.getOnlinePlayers(), colorizedMessage, hoverSettings);
                return;
            }
            e.setFormat(getFormatWithMessage(globalFormat, colorizedMessage));
            return;
        }
        if (processCooldown(e, p, name, localCooldowns, cooldownSettings.localRateLimit(), cooldownSettings)) {
            return;
        }
        String localFormat = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(chatSettings.localFormat(), searchList, replacementList)));
        e.getRecipients().clear();
        List<Player> playersInRadius = getRadius(p, chatSettings);
        if (!playersInRadius.isEmpty()) {
            e.getRecipients().addAll(playersInRadius);
        }
        String colorizedMessage = Utils.formatByPerm(p, message);
        if (hoverSettings.hoverEnabled()) {
            e.setCancelled(true);
            sendHover(p, replacementList, localFormat, playersInRadius, colorizedMessage, hoverSettings);
            return;
        }
        e.setFormat(getFormatWithMessage(localFormat, colorizedMessage));
    }

    private boolean checkNewbie(Player p, Cancellable e) {
        NewbieChatSettings newbieChatSettings = pluginConfig.getNewbieChatSettings();
        if (newbieChatSettings.enabled()) {
            if (p.hasPermission("pchat.bypass.newbie")) {
                return false;
            }
            long time = (System.currentTimeMillis() - p.getFirstPlayed()) / 1000;
            if (time <= newbieChatSettings.cooldown()) {
                String cooldown = Utils.getTime((int) (newbieChatSettings.cooldown() - time), " ч. ", " мин. ", " сек. ");
                p.sendMessage(newbieChatSettings.message().replace("%time%", cooldown));
                e.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private void sendHover(Player p, String[] replacementList, String format, Collection<? extends Player> recipients, String chatMessage, HoverSettings hoverSettings) {
        String hoverText = Utils.colorize(Utils.replacePlaceholders(p, Utils.replaceEach(hoverSettings.hoverMessage(), searchList, replacementList)));
        HoverEvent hoverEvent = new HoverEvent(Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText)));
        String formatWithMessage = getFormatWithMessage(format, chatMessage);
        BaseComponent[] comp = TextComponent.fromLegacyText(formatWithMessage);
        for (BaseComponent component : comp) {
            component.setHoverEvent(hoverEvent);
        }
        if (hoverSettings.clickEventEnabled()) {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.valueOf(hoverSettings.clickAction()), Utils.replacePlaceholders(p, Utils.replaceEach(hoverSettings.clickActionValue(), searchList, replacementList)));
            for (BaseComponent component : comp) {
                component.setClickEvent(clickEvent);
            }
        }
        for (Player recipient : recipients) {
            recipient.spigot().sendMessage(comp);
        }
        // Костыли... костыли вечны.
        Bukkit.getConsoleSender().sendMessage(formatWithMessage);
    }

    private boolean processCooldown(Cancellable e, Player p, String name, ExpiringMap<String, Long> playerCooldown, long rateLimit, CooldownSettings cooldownSettings) {
        if (p.hasPermission("pchat.bypass.cooldown")) {
            return false;
        }
        if (playerCooldown.containsKey(name)) {
            String cooldown = Utils.getTime((int) (rateLimit / 1000 + (playerCooldown.get(name) - System.currentTimeMillis()) / 1000), " ч. ", " мин. ", " сек. ");
            p.sendMessage(cooldownSettings.tooFastMessage().replace("%time%", cooldown));
            e.setCancelled(true);
            return true;
        }
        playerCooldown.put(name, System.currentTimeMillis());
        return false;
    }

    private List<Player> getRadius(Player p, ChatSettings chatSettings) {
        List<Player> plist = new ObjectArrayList<>();
        double maxDist = Math.pow(chatSettings.chatRadius(), 2.0D);
        Location loc = p.getLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != p.getWorld()) {
                continue;
            }
            if (loc.distanceSquared(player.getLocation()) <= maxDist) {
                plist.add(player);
            }
        }
        return plist;
    }

    private String removeGlobalPrefix(String message, ChatSettings chatSettings) {
        return chatSettings.forceGlobal() ? message : message.substring(1).trim();
    }

    private String getFormatWithMessage(String format, String chatMessage) {
        return format
                .replace("<message>", chatMessage)
                .replace("%", "%%"); // Это надо чтобы PAPI не выёбывался
    }

    private String getDonatePlaceholder(Player p, ChatSettings chatSettings) {
        String primaryGroup = plugin.getPerms().getPrimaryGroup(p);
        return chatSettings.perGroupColor().getOrDefault(primaryGroup, "");
    }

}
