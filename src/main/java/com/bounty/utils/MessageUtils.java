package com.bounty.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    private static final String PREFIX = "§8[§6Bounty§8] §r";

    /**
     * Send a message with plugin prefix
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    /**
     * Send a message with plugin prefix (Component version)
     */
    public static void sendMessage(CommandSender sender, Component message) {
        Component prefix = Component.text("[Bounty] ", NamedTextColor.GOLD);
        sender.sendMessage(prefix.append(message));
    }

    /**
     * Send a message to a player with plugin prefix
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    /**
     * Get the plugin prefix
     */
    public static String getPrefix() {
        return PREFIX;
    }
}

