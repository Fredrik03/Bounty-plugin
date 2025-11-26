package com.bounty.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bounty {
    
    private final UUID targetPlayer;
    private final UUID setterPlayer;
    private final List<ItemStack> rewardItems;
    private final long timestamp;
    
    public Bounty(UUID targetPlayer, UUID setterPlayer, List<ItemStack> rewardItems) {
        this.targetPlayer = targetPlayer;
        this.setterPlayer = setterPlayer;
        this.rewardItems = new ArrayList<>(rewardItems);
        this.timestamp = System.currentTimeMillis();
    }
    
    public UUID getTargetPlayer() {
        return targetPlayer;
    }
    
    public UUID getSetterPlayer() {
        return setterPlayer;
    }
    
    public List<ItemStack> getRewardItems() {
        return new ArrayList<>(rewardItems);
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Creates a deep copy of reward items to avoid reference issues
     */
    public List<ItemStack> getRewardItemsCopy() {
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack item : rewardItems) {
            if (item != null) {
                copy.add(item.clone());
            }
        }
        return copy;
    }
}

