package me.fliqq.cratehaven.object;


import java.util.List;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
@Getter
public class Key {
    private Material material;
    private Component displayName;
    private boolean glowing;
    private List<String> lore;

    
    
}
