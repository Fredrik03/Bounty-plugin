package com.bounty.managers;

import com.bounty.BountyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LeaderboardManager {

    private final BountyPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    // UUID -> claim count
    private final Map<UUID, Integer> claimCounts;

    public LeaderboardManager(BountyPlugin plugin) {
        this.plugin = plugin;
        this.claimCounts = new HashMap<>();
        setupDataFile();
        loadLeaderboard();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create leaderboard.yml file!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Increment claim count for a player
     */
    public void addClaim(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        int current = claimCounts.getOrDefault(uuid, 0);
        claimCounts.put(uuid, current + 1);
        saveLeaderboard();
    }

    /**
     * Get claim count for a player
     */
    public int getClaimCount(Player player) {
        if (player == null) {
            return 0;
        }
        return claimCounts.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Get top claimers (sorted by count, descending)
     */
    public List<Map.Entry<UUID, Integer>> getTopClaimers(int limit) {
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(claimCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        if (limit > 0 && limit < sorted.size()) {
            return sorted.subList(0, limit);
        }
        return sorted;
    }

    /**
     * Get player's rank (1-based)
     */
    public int getRank(Player player) {
        if (player == null) {
            return -1;
        }
        int playerClaims = getClaimCount(player);
        if (playerClaims == 0) {
            return -1; // Not ranked
        }
        
        List<Map.Entry<UUID, Integer>> sorted = getTopClaimers(0);
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(player.getUniqueId())) {
                return i + 1;
            }
        }
        return -1;
    }

    private void loadLeaderboard() {
        if (dataConfig.contains("claims")) {
            for (String key : dataConfig.getConfigurationSection("claims").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int count = dataConfig.getInt("claims." + key, 0);
                    claimCounts.put(uuid, count);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in leaderboard: " + key);
                }
            }
        }
    }

    private void saveLeaderboard() {
        dataConfig.set("claims", null);
        for (Map.Entry<UUID, Integer> entry : claimCounts.entrySet()) {
            dataConfig.set("claims." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save leaderboard.yml file!");
            e.printStackTrace();
        }
    }

    public void saveAll() {
        saveLeaderboard();
    }
}

