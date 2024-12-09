package me.fliqq.cratehaven.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import me.fliqq.cratehaven.manager.CrateManager;
import me.fliqq.cratehaven.object.Crate;
import me.fliqq.cratehaven.object.Key; // Assuming you have a Key class

public class GiveKeyCommand implements CommandExecutor {

    private final CrateManager crateManager;

    public GiveKeyCommand(CrateManager crateManager) {
        this.crateManager = crateManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("cratehaven.give")){
            player.sendMessage("You do not have the permission to perform this command.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("Please specify a key ID.");
            return true;
        }

        for (Crate crate : crateManager.getCrates().keySet()) {
            if (crate.getId().equalsIgnoreCase(args[0])) {
                Key key = crate.getKey();
                ItemStack keyItem = createKeyItem(key, crate);
                player.getInventory().addItem(keyItem);
                player.sendMessage("You have been given a " + key.getDisplayName() + "!");

                return true;
            }
        }

        player.sendMessage("Key ID not found.");
        return true;
    }

    @SuppressWarnings("deprecation")
    private ItemStack createKeyItem(Key key, Crate crate) {
        ItemStack keyItem = new ItemStack(key.getMaterial()); 
        ItemMeta meta = keyItem.getItemMeta();

        if (meta != null) {
            meta.displayName(key.getDisplayName());
            meta.setLore(key.getLore());
            meta.setUnbreakable(true);
            if(key.isGlowing()){
                meta.addEnchant(Enchantment.EFFICIENCY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            NamespacedKey persistentKey = new NamespacedKey("cratehaven", "crate_id");
            meta.getPersistentDataContainer().set(persistentKey, PersistentDataType.STRING, crate.getId());
            keyItem.setItemMeta(meta);
        }

        return keyItem;
    }
}
