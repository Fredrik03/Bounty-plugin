package com.bounty.commands;

import com.bounty.BountyPlugin;
import com.bounty.gui.BountySetGUI;
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
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bounty.set")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /setbounty <player>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer §e" + args[0] + " §cis not online!");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot set a bounty on yourself!");
            return true;
        }

        // Check max bounties
        int bountyCount = plugin.getBountyManager().getBountyCount(target);
        int maxBounties = plugin.getConfig().getInt("max-bounties-per-player", 10);
        if (bountyCount >= maxBounties) {
            player.sendMessage("§c" + target.getName() + " already has the maximum number of bounties!");
            return true;
        }

        // Open GUI for item selection
        new BountySetGUI(plugin, player, target).open();
        
        return true;
    }
}


