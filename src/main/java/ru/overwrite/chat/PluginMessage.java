package ru.overwrite.chat;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PluginMessage implements PluginMessageListener {

    private final PromisedChat plugin;

    public PluginMessage(PromisedChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();
        if (subchannel.equalsIgnoreCase("pchat")) {
            String chatMessage = input.readUTF();
            String permission = input.readUTF();
            boolean hover = input.readBoolean();
            if (hover) {
                BaseComponent[] comp = ComponentSerializer.parse(chatMessage);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission(permission)) {
                        onlinePlayer.spigot().sendMessage(comp);
                    }
                }
                Bukkit.getConsoleSender().sendMessage(comp);
                return;
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(permission)) {
                    onlinePlayer.sendMessage(chatMessage);
                }
            }
        }
    }

    public void sendCrossProxy(Player player, String message, String permission, boolean hover) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("pchat");
        out.writeUTF(message);
        out.writeUTF(permission);
        out.writeBoolean(hover);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
