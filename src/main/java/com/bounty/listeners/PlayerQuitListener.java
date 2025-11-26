package com.bounty.listeners;

import com.bounty.BountyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final BountyPlugin plugin;

    public PlayerQuitListener(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Cleanup is handled by ItemRewardManager automatically
        // Bounties remain active even if setter quits
    }
}


