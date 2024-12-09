package me.fliqq.cratehaven.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import me.fliqq.cratehaven.manager.CrateManager;
import me.fliqq.cratehaven.object.Crate;

public class SetCrateCommand implements CommandExecutor {

    private final CrateManager crateManager;

    public SetCrateCommand(CrateManager crateManager) {
        this.crateManager = crateManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) return true; // Ensure sender is a player
        Player player = (Player) sender;

        if (!player.hasPermission("cratehaven.setcrate")) {
            player.sendMessage("You don't have permission to set crates");
            return true;
        }

        // Check if an argument was provided
        if (args.length < 1) {
            player.sendMessage("Please specify a crate ID.");
            return true;
        }

        Block block = player.getTargetBlockExact(10);
        if (block == null || block.getType() != Material.ENDER_CHEST) {
            player.sendMessage("You must look at an Ender Chest");
            return true;
        }

        // Check if the block already has persistent data indicating it's a crate
        BlockState state = block.getState();
        if (state instanceof EnderChest) {
            EnderChest enderChest = (EnderChest) state;

            NamespacedKey key = new NamespacedKey("cratehaven", "crate_id");
            if (enderChest.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                player.sendMessage("This Ender Chest is already set as a crate.");
                return true; 
            }


            for (Crate crate : crateManager.getCrates().keySet()) {
                if (crate.getId().equalsIgnoreCase(args[0])) {
                    enderChest.getPersistentDataContainer().set(key, PersistentDataType.STRING, crate.getId());
                    enderChest.update();

                    Location location = block.getLocation().add(0.5, 1.5, 0.5);
                    ArmorStand hologram = (ArmorStand) player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                    hologram.customName(crate.getDisplayName());
                    hologram.setCustomNameVisible(true);
                    hologram.setGravity(false);
                    hologram.setVisible(false);
                    hologram.setBasePlate(false);
                    hologram.setMarker(true);

                    player.sendMessage("Crate set successfully!");
                    player.sendMessage("Setting crate ID: " + crate.getId());
                    return true;
                }
            }

        }

        player.sendMessage("Crate ID not found");
        return true;
    }
}
