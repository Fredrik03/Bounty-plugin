package com.bounty.managers;

import com.bounty.BountyPlugin;
import com.bounty.models.Bounty;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyManager {

    private final BountyPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    // In-memory storage: target UUID -> list of bounties
    private final Map<UUID, List<Bounty>> bounties;
    
    public BountyManager(BountyPlugin plugin) {
        this.plugin = plugin;
        this.bounties = new HashMap<>();
        setupDataFile();
        loadBounties();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "bounties.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create bounties.yml file!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Set a bounty on a target player
     * @param target The player to set bounty on
     * @param setter The player setting the bounty
     * @param items The items to use as reward
     * @return true if successful, false otherwise
     */
    public boolean setBounty(Player target, Player setter, List<ItemStack> items) {
        if (target == null || setter == null || items == null || items.isEmpty()) {
            return false;
        }
        
        UUID targetUUID = target.getUniqueId();
        UUID setterUUID = setter.getUniqueId();
        
        // Check max bounties per player
        int maxBounties = plugin.getConfig().getInt("max-bounties-per-player", 10);
        List<Bounty> existingBounties = bounties.getOrDefault(targetUUID, new ArrayList<>());
        if (existingBounties.size() >= maxBounties) {
            return false;
        }
        
        // Create bounty
        Bounty bounty = new Bounty(targetUUID, setterUUID, items);
        
        // Add to storage
        bounties.computeIfAbsent(targetUUID, k -> new ArrayList<>()).add(bounty);
        
        // Save to file
        saveBounties();
        
        return true;
    }

    /**
     * Get all bounties on a player
     */
    public List<Bounty> getBounties(Player player) {
        if (player == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(bounties.getOrDefault(player.getUniqueId(), new ArrayList<>()));
    }

    /**
     * Get all bounties on a player by UUID
     */
    public List<Bounty> getBounties(UUID uuid) {
        return new ArrayList<>(bounties.getOrDefault(uuid, new ArrayList<>()));
    }

    /**
     * Claim all bounties on a player (when they die)
     * @param target The player who died
     * @param claimer The player who killed them
     * @return List of all reward items from claimed bounties
     */
    public List<ItemStack> claimBounties(Player target, Player claimer) {
        if (target == null || claimer == null) {
            return new ArrayList<>();
        }
        
        UUID targetUUID = target.getUniqueId();
        List<Bounty> targetBounties = bounties.get(targetUUID);
        
        if (targetBounties == null || targetBounties.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Collect all reward items
        List<ItemStack> allRewards = new ArrayList<>();
        for (Bounty bounty : targetBounties) {
            allRewards.addAll(bounty.getRewardItemsCopy());
        }
        
        // Remove bounties from storage
        bounties.remove(targetUUID);
        saveBounties();
        
        return allRewards;
    }

    /**
     * Remove a specific bounty (admin only)
     */
    public boolean removeBounty(Player target, int index) {
        if (target == null) {
            return false;
        }
        
        UUID targetUUID = target.getUniqueId();
        List<Bounty> targetBounties = bounties.get(targetUUID);
        
        if (targetBounties == null || index < 0 || index >= targetBounties.size()) {
            return false;
        }
        
        targetBounties.remove(index);
        if (targetBounties.isEmpty()) {
            bounties.remove(targetUUID);
        }
        saveBounties();
        
        return true;
    }

    /**
     * Check if a player has any bounties
     */
    public boolean hasBounties(Player player) {
        if (player == null) {
            return false;
        }
        List<Bounty> targetBounties = bounties.get(player.getUniqueId());
        return targetBounties != null && !targetBounties.isEmpty();
    }

    /**
     * Get count of bounties on a player
     */
    public int getBountyCount(Player player) {
        if (player == null) {
            return 0;
        }
        List<Bounty> targetBounties = bounties.get(player.getUniqueId());
        return targetBounties != null ? targetBounties.size() : 0;
    }

    private void loadBounties() {
        // Load bounties from file
        // This is a simplified version - in production you'd want proper serialization
        // For now, we'll keep bounties in memory and save on changes
        // Full persistence would require ItemStack serialization
    }

    private void saveBounties() {
        // Save bounties to file
        // Simplified - full implementation would serialize ItemStacks
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bounties.yml file!");
            e.printStackTrace();
        }
    }

    public void saveAll() {
        saveBounties();
    }
}

