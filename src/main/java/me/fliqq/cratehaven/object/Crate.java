package me.fliqq.cratehaven.object;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
@Getter

public class Crate {
    final String id;
    final Component displayName;
    final Key key;
    final List<Rarity> rarities;
}
