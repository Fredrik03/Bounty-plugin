package com.bounty.managers;

import com.bounty.BountyPlugin;
import com.bounty.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemRewardManager {

    private final BountyPlugin plugin;
    private final Map<UUID, List<ItemStack>> pendingRewards;
    private final Map<UUID, BukkitTask> pendingTasks;

    public ItemRewardManager(BountyPlugin plugin) {
        this.plugin = plugin;
        this.pendingRewards = new HashMap<>();
        this.pendingTasks = new HashMap<>();
        
        // Start task to periodically try to give pending rewards
        startRewardDistributionTask();
    }

    /**
     * Claim bounties and give rewards to the killer
     */
    public void claimBounties(Player victim, Player killer) {
        List<ItemStack> rewardItems = plugin.getBountyManager().claimBounties(victim, killer);
        
        if (rewardItems.isEmpty()) {
            return;
        }

        // Track claim in leaderboard
        plugin.getLeaderboardManager().addClaim(killer);

        // Try to give items immediately
        List<ItemStack> remainingItems = giveItemsToPlayer(killer, rewardItems);
        
        if (!remainingItems.isEmpty()) {
            // Add to pending rewards
            pendingRewards.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).addAll(remainingItems);
            MessageUtils.sendMessage(killer, "§eYou claimed bounties on §c" + victim.getName() + "§e!");
            MessageUtils.sendMessage(killer, "§7Some items couldn't fit in your inventory. They will be given when you have space.");
            
            // Start checking for inventory space
            startInventoryCheck(killer);
        } else {
            MessageUtils.sendMessage(killer, "§aYou claimed bounties on §c" + victim.getName() + "§a!");
        }

        // Broadcast if enabled
        if (plugin.getConfig().getBoolean("broadcast-bounty-claim", true)) {
            String message = plugin.getConfig().getString("bounty-claim-message", 
                "&a{claimer} has claimed the bounty on {target}!");
            message = message.replace("{claimer}", killer.getName())
                           .replace("{target}", victim.getName());
            Bukkit.broadcast(Component.text(message.replace("&", "§"), NamedTextColor.GREEN));
        }
    }

    /**
     * Try to give items to player, return items that couldn't fit
     */
    private List<ItemStack> giveItemsToPlayer(Player player, List<ItemStack> items) {
        List<ItemStack> remaining = new ArrayList<>();
        
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            
            // Try to add to inventory
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            
            if (!leftover.isEmpty()) {
                // Some items couldn't fit
                remaining.addAll(leftover.values());
            }
        }
        
        return remaining;
    }

    /**
     * Start checking player's inventory periodically
     */
    private void startInventoryCheck(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Cancel existing task if any
        if (pendingTasks.containsKey(uuid)) {
            pendingTasks.get(uuid).cancel();
        }
        
        // Create new task that checks every 20 ticks (1 second)
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                // Player offline, cancel task
                pendingTasks.remove(uuid);
                return;
            }
            
            List<ItemStack> pending = pendingRewards.get(uuid);
            if (pending == null || pending.isEmpty()) {
                // No more pending items, cancel task
                pendingTasks.remove(uuid);
                pendingRewards.remove(uuid);
                return;
            }
            
            // Try to give items
            List<ItemStack> remaining = giveItemsToPlayer(player, new ArrayList<>(pending));
            
            if (remaining.isEmpty()) {
                // All items given
                MessageUtils.sendMessage(player, "§aAll bounty rewards have been added to your inventory!");
                pendingRewards.remove(uuid);
                pendingTasks.remove(uuid).cancel();
            } else {
                // Update pending list
                pending.clear();
                pending.addAll(remaining);
            }
        }, 20L, 20L); // Check every second
        
        pendingTasks.put(uuid, task);
    }

    /**
     * Start periodic task to distribute rewards
     */
    private void startRewardDistributionTask() {
        // This runs every 5 seconds to check all players with pending rewards
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new ArrayList<>(pendingRewards.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    List<ItemStack> pending = pendingRewards.get(uuid);
                    if (pending != null && !pending.isEmpty()) {
                        List<ItemStack> remaining = giveItemsToPlayer(player, new ArrayList<>(pending));
                        if (remaining.isEmpty()) {
                            MessageUtils.sendMessage(player, "§aAll bounty rewards have been added to your inventory!");
                            pendingRewards.remove(uuid);
                            if (pendingTasks.containsKey(uuid)) {
                                pendingTasks.remove(uuid).cancel();
                            }
                        } else {
                            pending.clear();
                            pending.addAll(remaining);
                        }
                    }
                }
            }
        }, 100L, 100L); // Every 5 seconds
    }

    /**
     * Get pending rewards for a player (for debugging/admin)
     */
    public List<ItemStack> getPendingRewards(Player player) {
        return new ArrayList<>(pendingRewards.getOrDefault(player.getUniqueId(), new ArrayList<>()));
    }

    /**
     * Clear pending rewards (admin function)
     */
    public void clearPendingRewards(Player player) {
        pendingRewards.remove(player.getUniqueId());
        if (pendingTasks.containsKey(player.getUniqueId())) {
            pendingTasks.remove(player.getUniqueId()).cancel();
        }
    }
}

