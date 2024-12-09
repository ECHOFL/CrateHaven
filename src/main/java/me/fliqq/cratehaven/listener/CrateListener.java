package me.fliqq.cratehaven.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.fliqq.cratehaven.CrateHaven;
import me.fliqq.cratehaven.manager.CrateManager;
import me.fliqq.cratehaven.object.Crate;
import me.fliqq.cratehaven.object.Rarity;
import me.fliqq.cratehaven.object.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class CrateListener implements Listener {

    private final CrateManager crateManager;
    private final CrateHaven plugin;

    public CrateListener(CrateManager crateManager, CrateHaven plugin) {
        this.crateManager = crateManager;
        this.plugin=plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.ENDER_CHEST) {
                if(crateManager.isCrate(block)) event.setCancelled(true);
                ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
                if (itemInHand != null && itemInHand.hasItemMeta()) {
                    NamespacedKey key = new NamespacedKey("cratehaven", "crate_id");
                    
                    String keyId = itemInHand.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    
                    if (keyId != null) {
                        Crate crate = crateManager.getCrate(block); 
                        if (crate != null && crate.getId().equals(keyId)) {
                            event.getPlayer().setMetadata("OpenedMenu",new FixedMetadataValue(plugin, "Crate Menu"));
                            openCrate(event.getPlayer(), crate);
                        } else {
                            event.getPlayer().sendMessage("This key does not match this crate.");
                        }
                    } else {
                        
                        event.getPlayer().sendMessage("You need a valid key to open this crate.");

                    }
                    event.setCancelled(true); 
                }
            }
        }
        else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            Block block = event.getClickedBlock();
            if(block != null && block.getType() == Material.ENDER_CHEST){
                if(crateManager.isCrate(block)){
                    showAvailableRewards(event.getPlayer(), crateManager.getCrate(block));
                }              
            }
        }
    }


    @EventHandler
    private void onQuit(PlayerQuitEvent e){
        Player player = (Player) e.getPlayer();
        if(player.hasMetadata("OpenedMenu")){
            player.removeMetadata("OpenedMenu", plugin);
        }
    }
    @EventHandler
    private void onClose(InventoryCloseEvent e){
        Player player = (Player) e.getPlayer();
        if(player.hasMetadata("OpenedMenu")){
            player.removeMetadata("OpenedMenu", plugin);
        }
    }

    private void showAvailableRewards(Player player, Crate crate) {
        List<Reward> rewards = crateManager.getCrates().get(crate); 
        if (rewards.isEmpty()) {
            player.sendMessage("This crate has no available rewards.");
            return;
        }
    
        // Calculate inventory size
        int size = Math.min((int) Math.ceil(rewards.size() / 9.0) * 9, 54);
        Inventory rewardInventory = Bukkit.createInventory(player, size, 
            Component.text("CrateHaven ").color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .append(crate.getDisplayName()));
    
        // Count rewards by rarity
        Map<Rarity, List<Reward>> rewardsByRarity = new HashMap<>();
        for (Reward reward : rewards) {
            Rarity rarity = reward.getRarity();
            rewardsByRarity.putIfAbsent(rarity, new ArrayList<>());
            rewardsByRarity.get(rarity).add(reward);
        }
    
        // Add rewards to the inventory
        for (Map.Entry<Rarity, List<Reward>> entry : rewardsByRarity.entrySet()) {
            Rarity rarity = entry.getKey();
            List<Reward> tierRewards = entry.getValue();
            
            // Calculate individual probability for each reward in this rarity tier
            double individualProbability = rarity.getProba() / tierRewards.size();
    
            for (Reward reward : tierRewards) {
                ItemStack item;
                switch (reward.getType()) {
                    case ITEM:
                        item = new ItemStack(reward.getItem().getType(), reward.getItem().getAmount());
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(reward.getDisplayName());
                            List<Component> lore = new ArrayList<>();
                            lore.add(Component.text("Rarity: ").append(Component.text(rarity.getId()).color(rarity.getColor())));
                            lore.add(Component.text("Probability: " + individualProbability + "%").color(NamedTextColor.GRAY));
                            meta.lore(lore);
                            item.setItemMeta(meta);
                        }
                        break;
                    case COMMAND:
                        item = new ItemStack(Material.SHULKER_SHELL);
                        meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(reward.getDisplayName());
                            List<Component> commandLore = new ArrayList<>();
                            commandLore.add(Component.text("Rarity: ").append(Component.text(rarity.getId()).color(rarity.getColor())));
                            commandLore.add(Component.text("Probability: " + individualProbability + "%").color(NamedTextColor.GRAY));
                            meta.lore(commandLore);
                            item.setItemMeta(meta);
                        }
                        break;
                    case PERMISSION:
                        item = new ItemStack(Material.PAPER); 
                        meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(reward.getDisplayName());
                            List<Component> permissionLore = new ArrayList<>();
                            permissionLore.add(Component.text("Rarity: ").append(Component.text(rarity.getId()).color(rarity.getColor())));
                            permissionLore.add(Component.text("Probability: " + individualProbability + "%").color(NamedTextColor.GRAY));
                            meta.lore(permissionLore);
                            item.setItemMeta(meta);
                        }
                        break;
                    default:
                        continue;
                }
                rewardInventory.addItem(item);
            }
        }
    
        // Open the inventory for the player
        player.openInventory(rewardInventory);
        player.setMetadata("OpenedMenu", new FixedMetadataValue(plugin, "Crate Menu"));
    }
    

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {                
        Player player = (Player) event.getWhoClicked();
        if(player.hasMetadata("OpenedMenu")){
            event.setCancelled(true);
        }
    }
    

    @EventHandler
    public void onCrateBlock(BlockBreakEvent event){
        if(crateManager.isCrate(event.getBlock())) event.setCancelled(true);
    }


    private void openCrate(Player player, Crate crate) {
        // Notify the player that they opened the crate
        player.sendMessage(Component.text("You opened " + crate.getId() + "!").color(NamedTextColor.GOLD));
    
        // Retrieve rewards from the crate manager
        List<Reward> rewards = crateManager.getCrates().get(crate);
        if (rewards == null || rewards.isEmpty()) {
            player.sendMessage(Component.text("This crate has no available rewards.").color(NamedTextColor.RED));
            return;
        }
    
        // Create a list to store selected rewards
        List<Reward> selectedRewards = new ArrayList<>();
    
        // Iterate through each rarity tier to select rewards
        Map<Rarity, List<Reward>> rewardsByRarity = new HashMap<>();
        for (Reward reward : rewards) {
            Rarity rarity = reward.getRarity();
            rewardsByRarity.putIfAbsent(rarity, new ArrayList<>());
            rewardsByRarity.get(rarity).add(reward);
        }
    
        // Randomly select rewards based on their rarity probabilities
        for (Map.Entry<Rarity, List<Reward>> entry : rewardsByRarity.entrySet()) {
            Rarity rarity = entry.getKey();
            List<Reward> tierRewards = entry.getValue();
    
            // Calculate individual probability for each reward in this rarity tier
            double individualProbability = rarity.getProba() / tierRewards.size();
    
            // Randomly determine if a reward from this tier should be given based on its probability
            for (Reward reward : tierRewards) {
                if (Math.random() * 100 < individualProbability) { // Check if we should give this reward
                    selectedRewards.add(reward);
                }
            }
        }
    
        // Give selected rewards to the player
        for (Reward reward : selectedRewards) {
            switch (reward.getType()) {
                case ITEM:
                    ItemStack item = new ItemStack(reward.getItem().getType(), reward.getItem().getAmount());
                    player.getInventory().addItem(item);
                    player.sendMessage(Component.text("You received: ").append(reward.getDisplayName()).color(NamedTextColor.GREEN));
                    break;
                case COMMAND:
                    String command = reward.getCommand(); // Assuming Reward has a getCommand() method
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    player.sendMessage(Component.text("Executed command: ").append(reward.getDisplayName()).color(NamedTextColor.GREEN));
                    break;
                case PERMISSION:
                    String permission = reward.getPermission(); // Assuming Reward has a getPermission() method
                    player.sendMessage(Component.text("You have received permission: ").append(reward.getDisplayName()).color(NamedTextColor.GREEN));
                    player.addAttachment(plugin, permission, true);
                    break;
                default:
                    break;
            }
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        int amount= item.getAmount();
        item.setAmount(amount-1);

        if (selectedRewards.isEmpty()) {
            player.sendMessage(Component.text("Unfortunately, you didn't receive any rewards this time.").color(NamedTextColor.RED));
        }
    }
    
}
