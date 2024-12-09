package me.fliqq.cratehaven.object;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
@Getter
public class Reward {
    public enum RewardType {
        ITEM,
        COMMAND,
        PERMISSION
    }

    private final RewardType type;
    private final Component displayName;
    private Rarity rarity;
    private final ItemStack item;
    private final String command;
    private final String permission;

}
