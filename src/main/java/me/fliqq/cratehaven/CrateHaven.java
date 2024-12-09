package me.fliqq.cratehaven;

import org.bukkit.plugin.java.JavaPlugin;

import me.fliqq.cratehaven.command.GiveKeyCommand;
import me.fliqq.cratehaven.command.SetCrateCommand;
import me.fliqq.cratehaven.command.ShowCrateRewardsCommand;
import me.fliqq.cratehaven.listener.CrateListener;
import me.fliqq.cratehaven.manager.CrateManager;

public class CrateHaven extends JavaPlugin
{
    private CrateManager crateManager;

    @Override
    public void onEnable(){
        this.crateManager= new CrateManager(this);
        
        getCommand("setcrate").setExecutor(new SetCrateCommand(crateManager));
        getCommand("givekey").setExecutor(new GiveKeyCommand(crateManager));
        getCommand("showcraterewards").setExecutor(new ShowCrateRewardsCommand(crateManager));
        getServer().getPluginManager().registerEvents(new CrateListener(crateManager, this), this);
        messages();
    }
        
    private void messages() {
        getLogger().info("***********");
        getLogger().info("CrateHaven 1.0 enabled");
        getLogger().info("Plugin by Fliqqq");
        getLogger().info("***********");
    }
}
