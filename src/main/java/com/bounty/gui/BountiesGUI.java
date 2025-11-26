package com.bounty.gui;

import com.bounty.BountyPlugin;
import com.bounty.models.Bounty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BountiesGUI implements Listener {

    private final BountyPlugin plugin;
    private final Player viewer;
    private Inventory gui;
    private List<BountyEntry> bountyEntries;

    public BountiesGUI(BountyPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.bountyEntries = new ArrayList<>();
        buildGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        viewer.openInventory(gui);
    }

    private void buildGUI() {
        // Collect all bounties from all players
        List<BountyEntry> allBounties = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Bounty> bounties = plugin.getBountyManager().getBounties(player);
            for (Bounty bounty : bounties) {
                allBounties.add(new BountyEntry(player, bounty));
            }
        }

        this.bountyEntries = allBounties;

        // Calculate inventory size (multiple of 9, at least 9, max 54)
        int size = Math.max(9, Math.min(54, ((allBounties.size() + 8) / 9) * 9));
        
        gui = Bukkit.createInventory(null, size, 
            Component.text("Active Bounties", NamedTextColor.DARK_RED));

        // Fill with bounty items
        for (int i = 0; i < allBounties.size() && i < size; i++) {
            BountyEntry entry = allBounties.get(i);
            ItemStack displayItem = createBountyDisplayItem(entry);
            gui.setItem(i, displayItem);
        }

        // Add info item at the end if there's space
        if (size > allBounties.size()) {
            ItemStack infoItem = new ItemStack(Material.PAPER);
            ItemMeta meta = infoItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Bounty Information", NamedTextColor.GOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Total Active Bounties: " + allBounties.size(), NamedTextColor.YELLOW));
                lore.add(Component.text("Click a bounty to see details", NamedTextColor.GRAY));
                meta.lore(lore);
                infoItem.setItemMeta(meta);
            }
            gui.setItem(size - 1, infoItem);
        }
    }

    private ItemStack createBountyDisplayItem(BountyEntry entry) {
        Player target = entry.getTarget();
        Bounty bounty = entry.getBounty();
        
        // Use first item from bounty as display, or player head
        ItemStack displayItem;
        List<ItemStack> rewardItems = bounty.getRewardItems();
        
        if (!rewardItems.isEmpty() && rewardItems.get(0) != null) {
            displayItem = rewardItems.get(0).clone();
            displayItem.setAmount(1);
        } else {
            displayItem = new ItemStack(Material.PLAYER_HEAD);
        }

        ItemMeta meta = displayItem.getItemMeta();
        if (meta != null) {
            String setterName = Bukkit.getOfflinePlayer(bounty.getSetterPlayer()).getName();
            meta.displayName(Component.text("Bounty on " + target.getName(), NamedTextColor.RED));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Target: " + target.getName(), NamedTextColor.YELLOW));
            lore.add(Component.text("Set by: " + setterName, NamedTextColor.GRAY));
            lore.add(Component.text("Reward Items: " + rewardItems.size(), NamedTextColor.GREEN));
            lore.add(Component.empty());
            lore.add(Component.text("Reward Items:", NamedTextColor.GOLD));
            
            // List items (max 10 to avoid too long lore)
            int itemCount = 0;
            for (ItemStack item : rewardItems) {
                if (item != null && !item.getType().isAir() && itemCount < 10) {
                    String itemName = item.getType().name().toLowerCase().replace("_", " ");
                    int amount = item.getAmount();
                    lore.add(Component.text("  - " + amount + "x " + itemName, NamedTextColor.WHITE));
                    itemCount++;
                }
            }
            if (rewardItems.size() > 10) {
                lore.add(Component.text("  ... and " + (rewardItems.size() - 10) + " more", NamedTextColor.GRAY));
            }
            
            meta.lore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(gui)) {
            return;
        }

        if (!event.getWhoClicked().equals(viewer)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot >= 0 && slot < bountyEntries.size()) {
            BountyEntry entry = bountyEntries.get(slot);
            // Open detailed view
            new BountyDetailGUI(plugin, viewer, entry).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) && event.getPlayer().equals(viewer)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }

    public static class BountyEntry {
        private final Player target;
        private final Bounty bounty;

        public BountyEntry(Player target, Bounty bounty) {
            this.target = target;
            this.bounty = bounty;
        }

        public Player getTarget() {
            return target;
        }

        public Bounty getBounty() {
            return bounty;
        }
    }
}

