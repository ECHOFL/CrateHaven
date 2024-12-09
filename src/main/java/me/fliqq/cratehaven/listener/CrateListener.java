package me.fliqq.cratehaven.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.fliqq.cratehaven.manager.CrateManager;
import me.fliqq.cratehaven.object.Crate;
import me.fliqq.cratehaven.object.Reward;
import net.kyori.adventure.text.Component;

public class CrateListener implements Listener {

    private final CrateManager crateManager;

    public CrateListener(CrateManager crateManager) {
        this.crateManager = crateManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.ENDER_CHEST) {
                // Check if the player is holding a key
                ItemStack itemInHand = event.getItem();
                if (itemInHand != null && itemInHand.hasItemMeta()) {
                    NamespacedKey key = new NamespacedKey("cratehaven", "crate_id");
                    
                    String keyId = itemInHand.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (keyId != null) {
                        Crate crate = crateManager.getCrateByBlock(block); 
                        if (crate != null && crate.getId().equals(keyId)) {
                            openCrate(event.getPlayer(), crate);
                            event.setCancelled(true); 
                        } else {
                            event.getPlayer().sendMessage("This key does not match this crate.");
                        }
                    } else {
                        event.getPlayer().sendMessage("You need a valid key to open this crate.");
                    }
                }
            }
        }
        else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            Block block = event.getClickedBlock();
            if(block != null && block.getType() == Material.ENDER_CHEST){
                Crate crate = crateManager.getCrateByBlock(block);
                if(crate!=null){
                    showAvailableRewards(event.getPlayer(), crate);
                }              
            }
        }
    }
    private void showAvailableRewards(Player player, Crate crate) {
        List<Reward> rewards = crateManager.getCrates().get(crate); 
        if (rewards.isEmpty()) {
            player.sendMessage("This crate has no available rewards.");
            return;
        }
        int size = Math.min((int) Math.ceil(rewards.size() / 9.0) * 9, 54);
        Inventory rewardInventory = Bukkit.createInventory(player, size, Component.text("CrateHaven").append(crate.getDisplayName()));

        for (Reward reward : rewards) {
            ItemStack item;
            switch (reward.getType()) {
                case ITEM:
                    item = new ItemStack(reward.getItem().getType(), reward.getItem().getAmount());
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(reward.getDisplayName()); 
                        meta.lore(reward.getItem().getItemMeta().lore()); 
                        item.setItemMeta(meta);
                    }
                    break;
                case COMMAND:
                    item = new ItemStack(Material.SHULKER_SHELL);
                    meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(reward.getDisplayName());
                        item.setItemMeta(meta);
                    }
                    break;
                case PERMISSION:
                    item = new ItemStack(Material.PAPER); 
                    meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(reward.getDisplayName());
                        item.setItemMeta(meta);
                    }
                    break;
                default:
                    continue;

            }
            rewardInventory.addItem(item);
            player.openInventory(rewardInventory);
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory is your rewards inventory
        if (event.getView().title().contains(Component.text("CrateHaven"))) { 
            event.setCancelled(true); 
        }
    }



    private void openCrate(Player player, Crate crate) {
        // Logic to open the crate and give rewards to the player
        player.sendMessage("You opened " + crate.getId() + "!");
        // Add reward logic here, e.g., giving items from the crate's rewards
    }
}
