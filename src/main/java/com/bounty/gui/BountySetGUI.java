package com.bounty.gui;

import com.bounty.BountyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BountySetGUI implements Listener {

    private final BountyPlugin plugin;
    private final Player setter;
    private final Player target;
    private final Inventory gui;
    
    // Button slots
    private static final int CONFIRM_SLOT = 53;
    private static final int CANCEL_SLOT = 45;
    private static final int INFO_SLOT = 49;
    
    // Item area slots (rows 1-4, columns 1-7) = 28 slots
    private static final List<Integer> ITEM_SLOTS = Arrays.asList(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    );

    public BountySetGUI(BountyPlugin plugin, Player setter, Player target) {
        this.plugin = plugin;
        this.setter = setter;
        this.target = target;
        
        // Create 54 slot inventory (6 rows)
        this.gui = Bukkit.createInventory(null, 54, 
            Component.text("Set Bounty on " + target.getName(), NamedTextColor.DARK_RED));
        
        // Setup GUI layout
        setupLayout();
        setupButtons();
        
        // Register listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        setter.openInventory(gui);
    }

    private void setupLayout() {
        // Fill borders with glass panes
        Material borderMaterial = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack border = new ItemStack(borderMaterial);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.empty());
            border.setItemMeta(borderMeta);
        }
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border); // Top row
            gui.setItem(i + 45, border); // Bottom row
        }
        
        // Left and right columns
        for (int i = 9; i < 45; i += 9) {
            gui.setItem(i, border); // Left column
            gui.setItem(i + 8, border); // Right column
        }
        
        // Item area slots are left empty - players can place items directly
    }

    private void setupButtons() {
        // Info button in center
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("Bounty Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Target: ", NamedTextColor.GRAY)
                .append(Component.text(target.getName(), NamedTextColor.RED)));
            lore.add(Component.empty());
            lore.add(Component.text("Place items in the", NamedTextColor.YELLOW));
            lore.add(Component.text("gray area above", NamedTextColor.YELLOW));
            lore.add(Component.text("to set the bounty reward.", NamedTextColor.YELLOW));
            lore.add(Component.empty());
            lore.add(Component.text("Click ", NamedTextColor.GRAY)
                .append(Component.text("CONFIRM", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" when done.", NamedTextColor.GRAY)));
            infoMeta.lore(lore);
            infoButton.setItemMeta(infoMeta);
        }
        gui.setItem(INFO_SLOT, infoButton);
        
        // Confirm button (green)
        updateConfirmButton();
        
        // Cancel button (red)
        ItemStack cancelButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(Component.text("CANCEL", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to cancel and", NamedTextColor.GRAY));
            lore.add(Component.text("return all items.", NamedTextColor.GRAY));
            cancelMeta.lore(lore);
            cancelButton.setItemMeta(cancelMeta);
        }
        gui.setItem(CANCEL_SLOT, cancelButton);
    }

    private void updateConfirmButton() {
        List<ItemStack> items = getItemsFromGUI();
        ItemStack confirmButton;
        
        if (items.isEmpty()) {
            // Disabled state
            confirmButton = new ItemStack(Material.BARRIER);
            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("NO ITEMS", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Place at least one", NamedTextColor.GRAY));
                lore.add(Component.text("item to continue.", NamedTextColor.GRAY));
                meta.lore(lore);
                confirmButton.setItemMeta(meta);
            }
        } else {
            // Enabled state
            confirmButton = new ItemStack(Material.LIME_CONCRETE);
            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                int totalItems = items.size();
                int totalAmount = items.stream().mapToInt(ItemStack::getAmount).sum();
                
                meta.displayName(Component.text("CONFIRM BOUNTY", NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, true));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Items: ", NamedTextColor.GRAY)
                    .append(Component.text(totalItems + " types", NamedTextColor.WHITE)));
                lore.add(Component.text("Total: ", NamedTextColor.GRAY)
                    .append(Component.text(totalAmount + " items", NamedTextColor.WHITE)));
                lore.add(Component.empty());
                lore.add(Component.text("Click to set bounty!", NamedTextColor.GREEN));
                meta.lore(lore);
                confirmButton.setItemMeta(meta);
            }
        }
        gui.setItem(CONFIRM_SLOT, confirmButton);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if this is our GUI
        if (event.getView().getTopInventory() != gui) {
            return;
        }

        if (!event.getWhoClicked().equals(setter)) {
            return;
        }

        int slot = event.getRawSlot();
        
        // Check if clicking in the top inventory (our GUI)
        if (slot < gui.getSize()) {
            // Check if clicking in item area
            if (ITEM_SLOTS.contains(slot)) {
                // Allow all item operations in item area
                // Don't cancel - let items be placed/removed freely
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updateConfirmButton();
                }, 1L);
                return;
            }

            // Prevent clicking on borders, buttons, and info
            event.setCancelled(true);
            
            if (slot == CONFIRM_SLOT) {
                handleConfirm();
            } else if (slot == CANCEL_SLOT) {
                handleCancel();
            }
        } else {
            // Clicking in player inventory - allow it
            // This allows dragging from player inventory to GUI
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check if this is our GUI
        if (event.getView().getTopInventory() != gui) {
            return;
        }

        if (!event.getWhoClicked().equals(setter)) {
            return;
        }

        // Check if dragging into item area
        boolean draggingIntoItemArea = false;
        for (int slot : event.getRawSlots()) {
            if (slot < gui.getSize() && ITEM_SLOTS.contains(slot)) {
                draggingIntoItemArea = true;
                break;
            }
        }

        if (draggingIntoItemArea) {
            // Allow dragging into item area
            // Don't cancel - let items be placed
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateConfirmButton();
            }, 1L);
        } else {
            // Prevent dragging into borders/buttons
            for (int slot : event.getRawSlots()) {
                if (slot < gui.getSize() && !ITEM_SLOTS.contains(slot)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) && event.getPlayer().equals(setter)) {
            // If closing without confirming, return items
            if (event.getReason() != InventoryCloseEvent.Reason.PLUGIN) {
                List<ItemStack> items = getItemsFromGUI();
                for (ItemStack item : items) {
                    if (item != null && !item.getType().isAir()) {
                        setter.getInventory().addItem(item);
                    }
                }
            }
            
            // Unregister listener
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }

    private List<ItemStack> getItemsFromGUI() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot : ITEM_SLOTS) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR 
                && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
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

        // Set the bounty with items from GUI
        if (plugin.getBountyManager().setBounty(target, setter, items)) {
            setter.sendMessage("§a§l✓ Bounty set on §e§l" + target.getName() + "§a§l!");
            setter.sendMessage("§7Items have been used for the bounty.");
            setter.closeInventory();
        } else {
            setter.sendMessage("§cFailed to set bounty! Returning items...");
            // Return items to player's inventory
            for (ItemStack item : items) {
                setter.getInventory().addItem(item);
            }
        }
    }

    private void handleCancel() {
        // Return items to player
        List<ItemStack> items = getItemsFromGUI();
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                setter.getInventory().addItem(item);
            }
        }
        setter.sendMessage("§7Bounty cancelled. Items returned to inventory.");
        setter.closeInventory();
    }
}
