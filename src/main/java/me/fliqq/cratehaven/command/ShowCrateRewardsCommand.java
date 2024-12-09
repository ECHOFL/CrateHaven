package me.fliqq.cratehaven.command;

import me.fliqq.cratehaven.manager.CrateManager;
import me.fliqq.cratehaven.object.Crate; 
import me.fliqq.cratehaven.object.Reward; 
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ShowCrateRewardsCommand implements CommandExecutor {
    private final CrateManager crateManager;

    public ShowCrateRewardsCommand(CrateManager crateManager) {
        this.crateManager=crateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        Map<Crate, List<Reward>> crateRewardsMap = crateManager.getCrates();

        if (crateRewardsMap.isEmpty()) {
            player.sendMessage("No crates found.");
            return true;
        }

        for (Map.Entry<Crate, List<Reward>> entry : crateRewardsMap.entrySet()) {
            Crate crate = entry.getKey();
            List<Reward> rewards = entry.getValue();

            player.sendMessage("Crate: " + crate.getDisplayName());
            for (Reward reward : rewards) {
                String rewardInfo = " - Reward Type: " + reward.getType().name();
                if (reward.getType() == Reward.RewardType.ITEM) {
                    rewardInfo += ", Item: " + reward.getItem().getType().name();
                    rewardInfo += ", Amount: " + reward.getItem().getAmount();
                } else if (reward.getType() == Reward.RewardType.COMMAND) {
                    rewardInfo += ", Command: " + reward.getCommand();
                } else if (reward.getType() == Reward.RewardType.PERMISSION) {
                    rewardInfo += ", Permission: " + reward.getPermission();
                }
                player.sendMessage(rewardInfo);
            }
        }

        return true;
    }
}

