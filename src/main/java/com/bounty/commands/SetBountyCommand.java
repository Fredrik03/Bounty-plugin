package com.bounty.commands;

import com.bounty.BountyPlugin;
import com.bounty.gui.BountySetGUI;
import com.bounty.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetBountyCommand implements CommandExecutor {

    private final BountyPlugin plugin;

    public SetBountyCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bounty.set")) {
            MessageUtils.sendMessage(player, "§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            MessageUtils.sendMessage(player, "§cUsage: /setbounty <player>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            MessageUtils.sendMessage(player, "§cPlayer §e" + args[0] + " §cis not online!");
            return true;
        }

        if (target.equals(player)) {
            MessageUtils.sendMessage(player, "§cYou cannot set a bounty on yourself!");
            return true;
        }

        // Check max bounties
        int bountyCount = plugin.getBountyManager().getBountyCount(target);
        int maxBounties = plugin.getConfig().getInt("max-bounties-per-player", 10);
        if (bountyCount >= maxBounties) {
            MessageUtils.sendMessage(player, "§c" + target.getName() + " already has the maximum number of bounties!");
            return true;
        }

        // Open GUI for item selection
        new BountySetGUI(plugin, player, target).open();
        
        return true;
    }
}


