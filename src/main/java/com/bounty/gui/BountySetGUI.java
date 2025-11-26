package com.bounty.gui;

import com.bounty.BountyPlugin;
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

import java.util.ArrayList;
import java.util.List;

public class BountySetGUI implements Listener {

    private final BountyPlugin plugin;
    private final Player setter;
    private final Player target;
    private final Inventory gui;
    private static final int CONFIRM_SLOT = 49;
    private static final int CANCEL_SLOT = 45;

    public BountySetGUI(BountyPlugin plugin, Player setter, Player target) {
        this.plugin = plugin;
        this.setter = setter;
        this.target = target;
        
        // Create 54 slot inventory (6 rows)
        this.gui = Bukkit.createInventory(null, 54, 
            Component.text("Set Bounty on " + target.getName(), NamedTextColor.DARK_RED));
        
        // Add confirm and cancel buttons
        setupButtons();
        
        // Register listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        setter.openInventory(gui);
    }

    private void setupButtons() {
        // Confirm button (green wool)
        ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
        gui.setItem(CONFIRM_SLOT, confirmButton);
        
        // Cancel button (red wool)
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        gui.setItem(CANCEL_SLOT, cancelButton);
        
        // Info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        gui.setItem(4, infoItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(gui)) {
            return;
        }

        if (!event.getWhoClicked().equals(setter)) {
            return;
        }

        int slot = event.getRawSlot();

        // Prevent moving confirm/cancel buttons
        if (slot == CONFIRM_SLOT || slot == CANCEL_SLOT || slot == 4) {
            event.setCancelled(true);
            
            if (slot == CONFIRM_SLOT) {
                handleConfirm();
            } else if (slot == CANCEL_SLOT) {
                handleCancel();
            }
            return;
        }

        // Allow placing items in the inventory (slots 0-53 except button slots)
        if (slot >= 0 && slot < 54 && slot != CONFIRM_SLOT && slot != CANCEL_SLOT && slot != 4) {
            // Allow item placement/removal
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateConfirmButton();
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) && event.getPlayer().equals(setter)) {
            // Unregister listener
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }

    private void updateConfirmButton() {
        List<ItemStack> items = getItemsFromGUI();
        if (items.isEmpty()) {
            // Disable confirm button or show message
            ItemStack confirmButton = new ItemStack(Material.BARRIER);
            gui.setItem(CONFIRM_SLOT, confirmButton);
        } else {
            ItemStack confirmButton = new ItemStack(Material.LIME_WOOL);
            gui.setItem(CONFIRM_SLOT, confirmButton);
        }
    }

    private List<ItemStack> getItemsFromGUI() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (i == CONFIRM_SLOT || i == CANCEL_SLOT || i == 4) {
                continue;
            }
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }
        return items;
    }

    private void handleConfirm() {
        List<ItemStack> items = getItemsFromGUI();
        
        if (items.isEmpty()) {
            setter.sendMessage("§cYou must place at least one item in the bounty!");
            return;
        }

        // Check if player has all items in their inventory
        if (!hasItemsInInventory(setter, items)) {
            setter.sendMessage("§cYou don't have all the required items in your inventory!");
            return;
        }

        // Remove items from player's inventory
        removeItemsFromInventory(setter, items);

        // Set the bounty
        if (plugin.getBountyManager().setBounty(target, setter, items)) {
            setter.sendMessage("§aBounty set on §e" + target.getName() + "§a!");
            setter.sendMessage("§7Items have been removed from your inventory.");
            setter.closeInventory();
        } else {
            setter.sendMessage("§cFailed to set bounty! Returning items...");
            // Return items
            for (ItemStack item : items) {
                setter.getInventory().addItem(item);
            }
        }
    }

    private void handleCancel() {
        // Return items to player if any were placed
        List<ItemStack> items = getItemsFromGUI();
        for (ItemStack item : items) {
            setter.getInventory().addItem(item);
        }
        setter.closeInventory();
    }

    private boolean hasItemsInInventory(Player player, List<ItemStack> requiredItems) {
        Inventory inv = player.getInventory();
        List<ItemStack> requiredCopy = new ArrayList<>();
        for (ItemStack item : requiredItems) {
            requiredCopy.add(item.clone());
        }

        for (ItemStack required : requiredCopy) {
            int needed = required.getAmount();
            for (ItemStack invItem : inv.getContents()) {
                if (invItem != null && invItem.isSimilar(required)) {
                    needed -= invItem.getAmount();
                    if (needed <= 0) {
                        break;
                    }
                }
            }
            if (needed > 0) {
                return false;
            }
        }
        return true;
    }

    private void removeItemsFromInventory(Player player, List<ItemStack> itemsToRemove) {
        Inventory inv = player.getInventory();
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack item : itemsToRemove) {
            toRemove.add(item.clone());
        }

        for (ItemStack removeItem : toRemove) {
            int toRemoveAmount = removeItem.getAmount();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack invItem = inv.getItem(i);
                if (invItem != null && invItem.isSimilar(removeItem)) {
                    int amount = invItem.getAmount();
                    if (amount <= toRemoveAmount) {
                        inv.setItem(i, null);
                        toRemoveAmount -= amount;
                    } else {
                        invItem.setAmount(amount - toRemoveAmount);
                        toRemoveAmount = 0;
                    }
                    if (toRemoveAmount <= 0) {
                        break;
                    }
                }
            }
        }
    }
}

