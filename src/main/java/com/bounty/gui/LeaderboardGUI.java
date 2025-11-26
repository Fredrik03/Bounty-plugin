package com.bounty.gui;

import com.bounty.BountyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardGUI implements Listener {

    private final BountyPlugin plugin;
    private final Player viewer;
    private Inventory gui;

    public LeaderboardGUI(BountyPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        buildGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        viewer.openInventory(gui);
    }

    private void buildGUI() {
        gui = Bukkit.createInventory(null, 54, 
            Component.text("Bounty Leaderboard", NamedTextColor.GOLD));

        List<Map.Entry<UUID, Integer>> topClaimers = plugin.getLeaderboardManager().getTopClaimers(45);

        // Add header
        ItemStack header = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta headerMeta = header.getItemMeta();
        if (headerMeta != null) {
            headerMeta.displayName(Component.text("Top Bounty Claimers", NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Ranked by total bounties claimed", NamedTextColor.GRAY));
            headerMeta.lore(lore);
            header.setItemMeta(headerMeta);
        }
        gui.setItem(4, header);

        // Fill with leaderboard entries
        int slot = 9;
        for (int i = 0; i < topClaimers.size() && slot < 45; i++) {
            Map.Entry<UUID, Integer> entry = topClaimers.get(i);
            UUID uuid = entry.getKey();
            int claims = entry.getValue();
            int rank = i + 1;

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
                
                skullMeta.setOwningPlayer(offlinePlayer);
                skullMeta.displayName(getRankColor(rank).append(Component.text("#" + rank + " " + playerName, NamedTextColor.WHITE)));
                
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Bounties Claimed: " + claims, NamedTextColor.GREEN));
                if (viewer.getUniqueId().equals(uuid)) {
                    lore.add(Component.text("(You)", NamedTextColor.YELLOW));
                }
                skullMeta.lore(lore);
                playerHead.setItemMeta(skullMeta);
            }
            gui.setItem(slot, playerHead);
            slot++;
        }

        // Player's own rank if not in top
        int playerRank = plugin.getLeaderboardManager().getRank(viewer);
        int playerClaims = plugin.getLeaderboardManager().getClaimCount(viewer);
        if (playerRank > 0 && playerRank > topClaimers.size()) {
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta playerMeta = (SkullMeta) playerItem.getItemMeta();
            if (playerMeta != null) {
                playerMeta.setOwningPlayer(viewer);
                playerMeta.displayName(Component.text("Your Rank: #" + playerRank, NamedTextColor.YELLOW));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Bounties Claimed: " + playerClaims, NamedTextColor.GREEN));
                playerMeta.lore(lore);
                playerItem.setItemMeta(playerMeta);
            }
            gui.setItem(49, playerItem);
        } else if (playerRank > 0) {
            // Player is in top, highlight their entry
            for (int i = 0; i < topClaimers.size(); i++) {
                if (topClaimers.get(i).getKey().equals(viewer.getUniqueId())) {
                    ItemStack existing = gui.getItem(9 + i);
                    if (existing != null && existing.hasItemMeta()) {
                        ItemMeta meta = existing.getItemMeta();
                        if (meta != null) {
                            List<Component> lore = meta.lore();
                            if (lore == null) {
                                lore = new ArrayList<>();
                            }
                            lore.add(Component.text("(You)", NamedTextColor.YELLOW));
                            meta.lore(lore);
                            existing.setItemMeta(meta);
                        }
                    }
                    break;
                }
            }
        }
    }

    private Component getRankColor(int rank) {
        if (rank == 1) {
            return Component.text("[1st] ", NamedTextColor.GOLD);
        } else if (rank == 2) {
            return Component.text("[2nd] ", NamedTextColor.GRAY);
        } else if (rank == 3) {
            return Component.text("[3rd] ", NamedTextColor.YELLOW);
        }
        return Component.empty();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(gui)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) && event.getPlayer().equals(viewer)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}

