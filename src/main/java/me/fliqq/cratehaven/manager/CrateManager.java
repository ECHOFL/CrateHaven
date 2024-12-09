package me.fliqq.cratehaven.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import me.fliqq.cratehaven.CrateHaven;
import me.fliqq.cratehaven.object.Crate;
import me.fliqq.cratehaven.object.Key;
import me.fliqq.cratehaven.object.Rarity;
import me.fliqq.cratehaven.object.Reward;
import me.fliqq.cratehaven.object.Reward.RewardType;
import me.fliqq.cratehaven.util.MessageUtil;
import me.fliqq.cratehaven.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CrateManager {
    private final CrateHaven plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<Crate, List<Reward>> crates;

    public CrateManager(CrateHaven plugin){
        this.plugin=plugin;
        this.file=new File(plugin.getDataFolder(), "rewards.yml");
        this.crates = new HashMap<>();
        loadConfig();
        loadFile();
    }    

    private void loadConfig(){
        if(!file.exists()){
            plugin.saveResource("rewards.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
    private void loadFile(){
        ConfigurationSection cratesSection = config.getConfigurationSection("crates");
        if(cratesSection == null){
            throw new IllegalStateException("No crates defined in rewards.yml");
        }
        for(String crateId : cratesSection.getKeys(false)){
            ConfigurationSection crateConfig = cratesSection.getConfigurationSection(crateId);
            if(crateConfig == null) continue;
            loadCrate(crateConfig, crateId);
        }
    }
    private void loadCrate(ConfigurationSection crateConfig, String crateId) {
        if (crateConfig != null) {
            Component displayName = MessageUtil.parse(crateConfig.getString("display_name"));
            Key key = loadKey(crateConfig);
            Pair<List<Rarity>, Map<Rarity, List<Reward>>> raritiesAndRewards = loadRarities(crateConfig);
            if (raritiesAndRewards != null) {
                List<Rarity> rarities = raritiesAndRewards.getFirst();
                Map<Rarity, List<Reward>> rewardsMap = raritiesAndRewards.getSecond();
                Crate crate = new Crate(crateId, displayName, key, rarities);
                
                List<Reward> allRewards = rewardsMap.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                crates.put(crate, allRewards);
            }
        }
    }
    


    private Key loadKey(ConfigurationSection crateConfig){
        ConfigurationSection keyConfig = crateConfig.getConfigurationSection("key_item");
        if(keyConfig ==null) return null;
        Material material = Material.getMaterial(keyConfig.getString("material"));
        boolean enchantGlow = keyConfig.getBoolean("enchant_glow", false);
        Component displayName = MessageUtil.parse(keyConfig.getString("display_name"));
        List<String> lore = keyConfig.getStringList("lore");
        return new Key(material, displayName, enchantGlow, lore);
    }
    
    private Pair<List<Rarity>, Map<Rarity, List<Reward>>> loadRarities(ConfigurationSection crateConfig) {
        List<Rarity> rarities = new ArrayList<>();
        Map<Rarity, List<Reward>> rewardsMap = new HashMap<>();
        ConfigurationSection raritiesConfig = crateConfig.getConfigurationSection("rarity_tiers");
        if (raritiesConfig == null) return null;
        for (String rarityId : raritiesConfig.getKeys(false)) {
            ConfigurationSection rarityConfig = raritiesConfig.getConfigurationSection(rarityId);
            if (rarityConfig == null) continue;
            long probability = rarityConfig.getLong("probability", 0);
            String colorString = rarityConfig.getString("color", "WHITE");
            NamedTextColor color = getNamedTextColor(colorString);
            if (color == null) plugin.getLogger().warning("Invalid color: " + colorString);
            final Rarity rarity = new Rarity(rarityId, probability, color);
            rarities.add(rarity);
    
            List<Reward> rewards = loadRarityRewards(crateConfig, rarity);
            rewardsMap.put(rarity, rewards);
        }
        return new Pair<>(rarities, rewardsMap);
    }
    

    private List<Reward> loadRarityRewards(ConfigurationSection crateConfig, Rarity rarity) {
        List<Reward> rewards = new ArrayList<>();
        ConfigurationSection rewardConfig = crateConfig.getConfigurationSection("rewards");
        if (rewardConfig == null) {
            throw new IllegalStateException("No rewards defined for the crate: " + crateConfig.getCurrentPath());
        }
        
        List<Map<?, ?>> rarityRewards = rewardConfig.getMapList(rarity.getId());
        if (rarityRewards != null && !rarityRewards.isEmpty()) {
            for (Map<?, ?> rewardMap : rarityRewards) {
                RewardType type = RewardType.valueOf(((String) rewardMap.get("type")).toUpperCase());
                
                switch (type) {
                    case ITEM:
                        rewards.add(loadItemReward(rewardMap, rarity));
                        break;
                    case COMMAND:
                        rewards.add(loadCommandReward(rewardMap, rarity));
                        break;
                    case PERMISSION:
                        rewards.add(loadPermissionReward(rewardMap, rarity));
                        break;
                    default:
                        plugin.getLogger().warning("Unknown reward type: " + type);
                }
            }
        }
        
        return rewards;
    }

    @SuppressWarnings("unchecked")
    private Reward loadItemReward(Map<?, ?> rewardMap, Rarity rarity) {
        Material material = Material.valueOf(((String) rewardMap.get("material")).toUpperCase());
        int amount = 1; 
        
        Object amountObj = rewardMap.get("amount");
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).intValue();
        }
    
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        Component displayName = null;
    
        if (meta != null) {
            if (rewardMap.containsKey("display_name")) {
                displayName = MessageUtil.parse((String) rewardMap.get("display_name"));
                meta.displayName(displayName);
            }
            
            if (rewardMap.containsKey("lore")) {
                List<Component> lore = ((List<String>) rewardMap.get("lore")).stream()
                    .map(MessageUtil::parse)
                    .collect(Collectors.toList());
                meta.lore(lore);
            }
                        
            if (rewardMap.containsKey("enchants")) {
                List<Map<String, Object>> enchants = (List<Map<String, Object>>) rewardMap.get("enchants");
                for (Map<String, Object> enchantMap : enchants) {
                    String enchantType = (String) enchantMap.get("type");
                    int level = ((Number) enchantMap.get("level")).intValue();
                    
                    try {
                        Enchantment enchantment = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.ENCHANTMENT)
                            .getOrThrow(TypedKey.create(RegistryKey.ENCHANTMENT, net.kyori.adventure.key.Key.key(enchantType)));
                        
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not load enchantment for key '" + enchantType + "'");
                    }
                }
            }
            
            if (rewardMap.containsKey("hide_enchant") && (boolean) rewardMap.get("hide_enchant")) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            item.setItemMeta(meta);
        }
        
        if (displayName == null) {
            displayName = Component.text(material.name());
        }
        
        return new Reward(RewardType.ITEM, displayName, rarity, item, null, null);
    }
    

    private Reward loadCommandReward(Map<?, ?> rewardMap, Rarity rarity) {
        String command = (String) rewardMap.get("command");
        String displayNameStr = (String) rewardMap.get("display_name");
        if (displayNameStr == null) {
            displayNameStr = "Command Reward";
        }
        Component displayName = MessageUtil.parse(displayNameStr);
        return new Reward(RewardType.COMMAND, displayName, rarity, null, command, null);
    }
    
    private Reward loadPermissionReward(Map<?, ?> rewardMap, Rarity rarity) {
        String permission = (String) rewardMap.get("permission");
        String displayNameStr = (String) rewardMap.get("display_name");
        if (displayNameStr == null) {
            displayNameStr = "Permission Reward";
        }
        Component displayName = MessageUtil.parse(displayNameStr);
        return new Reward(RewardType.PERMISSION, displayName, rarity, null, null, permission);
    }
    

    private NamedTextColor getNamedTextColor(String colorName) {
        try {
            return NamedTextColor.NAMES.value(colorName.toLowerCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            plugin.getLogger().warning("Invalid color specified in config file.");
            return NamedTextColor.WHITE;
        }
    }
    public Map<Crate, List<Reward>> getCrates(){
        return crates;
    }

    public boolean isCrate(Block block){
        
        return false;
    }

    public Crate getCrateByBlock(Block block) {
        BlockState state = block.getState();
        if (state instanceof EnderChest) {
            EnderChest enderChest = (EnderChest) state;
            NamespacedKey key = new NamespacedKey("cratehaven", "crate_id"); 
            for(Crate crate : crates.keySet()){
                if(crate.getId() == enderChest.getPersistentDataContainer().get(key, PersistentDataType.STRING)){
                    return crate;
                }
            }
        }
        return null;
    }
}
