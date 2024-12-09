package me.fliqq.cratehaven.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@AllArgsConstructor
@Getter
public class Rarity {
    final String id;
    final long proba;
    final NamedTextColor color;
}
