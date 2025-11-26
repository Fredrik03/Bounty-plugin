package com.bounty;

import com.bounty.commands.BountyCommand;
import com.bounty.commands.SetBountyCommand;
import com.bounty.listeners.PlayerDeathListener;
import com.bounty.listeners.PlayerQuitListener;
import com.bounty.managers.BountyManager;
import com.bounty.managers.LeaderboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BountyPlugin extends JavaPlugin {

    private BountyManager bountyManager;
    private LeaderboardManager leaderboardManager;

    @Override
    public void onEnable() {
        // Initialize managers
        this.bountyManager = new BountyManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        
        // Register commands
        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("bounties").setExecutor(new BountyCommand(this));
        getCommand("setbounty").setExecutor(new SetBountyCommand(this));
        getCommand("leaderboard").setExecutor(new BountyCommand(this));
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        getLogger().info("BountyPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (bountyManager != null) {
            bountyManager.saveAll();
        }
        if (leaderboardManager != null) {
            leaderboardManager.saveAll();
        }
        
        getLogger().info("BountyPlugin has been disabled!");
    }

    public BountyManager getBountyManager() {
        return bountyManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}

