package me.fliqq.cratehaven;

import org.bukkit.plugin.java.JavaPlugin;

public class CrateHaven extends JavaPlugin
{
    @Override
    public void onEnable(){
        
        
        
        messages();
    }
        
    private void messages() {
        getLogger().info("***********");
        getLogger().info("CrateHaven 1.0 enabled");
        getLogger().info("Plugin by Fliqqq");
        getLogger().info("***********");
    }
}
