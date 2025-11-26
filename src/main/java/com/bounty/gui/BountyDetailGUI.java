package com.bounty.gui;

import com.bounty.BountyPlugin;
import com.bounty.gui.BountiesGUI.BountyEntry;
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

public class BountyDetailGUI implements Listener {

    private final BountyPlugin plugin;
    private final Player viewer;
    private final BountyEntry entry;
    private Inventory gui;

    public BountyDetailGUI(BountyPlugin plugin, Player viewer, BountyEntry entry) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.entry = entry;
        buildGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        viewer.openInventory(gui);
    }

    private void buildGUI() {
        gui = Bukkit.createInventory(null, 54, 
            Component.text("Bounty Details", NamedTextColor.DARK_RED));

        Player target = entry.getTarget();
        com.bounty.models.Bounty bounty = entry.getBounty();
        String setterName = Bukkit.getOfflinePlayer(bounty.getSetterPlayer()).getName();

        // Info item at top
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Bounty Information", NamedTextColor.GOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Target: " + target.getName(), NamedTextColor.YELLOW));
            lore.add(Component.text("Set by: " + setterName, NamedTextColor.GRAY));
            lore.add(Component.text("Reward Items: " + bounty.getRewardItems().size(), NamedTextColor.GREEN));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(4, infoItem);

        // Display reward items
        List<ItemStack> rewardItems = bounty.getRewardItems();
        int startSlot = 9;
        for (int i = 0; i < rewardItems.size() && startSlot + i < 45; i++) {
            ItemStack item = rewardItems.get(i);
            if (item != null && !item.getType().isAir()) {
                gui.setItem(startSlot + i, item.clone());
            }
        }

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("Back to Bounties", NamedTextColor.YELLOW));
            backButton.setItemMeta(backMeta);
        }
        gui.setItem(49, backButton);
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
        if (slot == 49) {
            // Back button
            new BountiesGUI(plugin, viewer).open();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) && event.getPlayer().equals(viewer)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}

