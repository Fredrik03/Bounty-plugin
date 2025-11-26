package com.bounty.commands;

import com.bounty.BountyPlugin;
import com.bounty.gui.BountiesGUI;
import com.bounty.gui.LeaderboardGUI;
import com.bounty.models.Bounty;
import com.bounty.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BountyCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public BountyCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no args and player, open GUI
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("bounty.view")) {
                    new BountiesGUI(plugin, player).open();
                    return true;
                }
            }
            MessageUtils.sendMessage(sender, "§cUsage: /bounty <list|view|remove|leaderboard>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                return handleList(sender);
            case "view":
                if (args.length < 2) {
                    if (sender instanceof Player && sender.hasPermission("bounty.view")) {
                        new BountiesGUI(plugin, (Player) sender).open();
                        return true;
                    }
                    MessageUtils.sendMessage(sender, "§cUsage: /bounty view <player>");
                    return true;
                }
                return handleView(sender, args[1]);
            case "leaderboard":
            case "lb":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("bounty.view")) {
                        new LeaderboardGUI(plugin, player).open();
                        return true;
                    }
                }
                MessageUtils.sendMessage(sender, "§cYou don't have permission to view the leaderboard!");
                return true;
            case "remove":
                if (!sender.hasPermission("bounty.remove")) {
                    MessageUtils.sendMessage(sender, "§cYou don't have permission to remove bounties!");
                    return true;
                }
                if (args.length < 3) {
                    MessageUtils.sendMessage(sender, "§cUsage: /bounty remove <player> <index>");
                    return true;
                }
                return handleRemove(sender, args[1], args[2]);
            default:
                MessageUtils.sendMessage(sender, "§cUnknown subcommand. Use: list, view, leaderboard, or remove");
                return true;
        }
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("bounty.view")) {
            MessageUtils.sendMessage(sender, "§cYou don't have permission to view bounties!");
            return true;
        }

        MessageUtils.sendMessage(sender, "§6=== Active Bounties ===");
        boolean foundAny = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Bounty> bounties = plugin.getBountyManager().getBounties(player);
            if (!bounties.isEmpty()) {
                foundAny = true;
                MessageUtils.sendMessage(sender, "§e" + player.getName() + " §7- §a" + bounties.size() + " bounty/bounties");
            }
        }

        if (!foundAny) {
            MessageUtils.sendMessage(sender, "§7No active bounties.");
        }

        return true;
    }

    private boolean handleView(CommandSender sender, String playerName) {
        if (!sender.hasPermission("bounty.view")) {
            MessageUtils.sendMessage(sender, "§cYou don't have permission to view bounties!");
            return true;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtils.sendMessage(sender, "§cPlayer §e" + playerName + " §cis not online!");
            return true;
        }

        List<Bounty> bounties = plugin.getBountyManager().getBounties(target);
        if (bounties.isEmpty()) {
            MessageUtils.sendMessage(sender, "§7" + target.getName() + " has no active bounties.");
            return true;
        }

        MessageUtils.sendMessage(sender, "§6=== Bounties on " + target.getName() + " ===");
        for (int i = 0; i < bounties.size(); i++) {
            Bounty bounty = bounties.get(i);
            String setterName = Bukkit.getOfflinePlayer(bounty.getSetterPlayer()).getName();
            int itemCount = bounty.getRewardItems().size();
            MessageUtils.sendMessage(sender, "§7[" + i + "] §e" + itemCount + " items §7(set by: §e" + setterName + "§7)");
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String playerName, String indexStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtils.sendMessage(sender, "§cPlayer §e" + playerName + " §cis not online!");
            return true;
        }

        try {
            int index = Integer.parseInt(indexStr);
            if (plugin.getBountyManager().removeBounty(target, index)) {
                MessageUtils.sendMessage(sender, "§aRemoved bounty #" + index + " from " + target.getName() + ".");
            } else {
                MessageUtils.sendMessage(sender, "§cInvalid bounty index!");
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(sender, "§cInvalid number: §e" + indexStr);
        }

        return true;
    }
}

