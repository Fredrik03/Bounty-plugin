package com.bounty.listeners;

import com.bounty.BountyPlugin;
import com.bounty.managers.ItemRewardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final BountyPlugin plugin;
    private final ItemRewardManager rewardManager;

    public PlayerDeathListener(BountyPlugin plugin) {
        this.plugin = plugin;
        this.rewardManager = new ItemRewardManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Only process if killed by another player
        if (killer == null || killer.equals(victim)) {
            return;
        }

        // Check if victim has bounties
        if (!plugin.getBountyManager().hasBounties(victim)) {
            return;
        }

        // Claim bounties
        rewardManager.claimBounties(victim, killer);
    }
}

