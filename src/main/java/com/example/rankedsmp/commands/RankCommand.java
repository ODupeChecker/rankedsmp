package com.example.rankedsmp.commands;

import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class RankCommand implements CommandExecutor {
    private final RankManager rankManager;
    private final ConfigManager configManager;

    public RankCommand(RankManager rankManager, ConfigManager configManager) {
        this.rankManager = rankManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length != 2) {
                sendHelp(sender);
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
                sender.sendMessage(TextUtils.color(configManager.getMessage("player-not-found")));
                return true;
            }
            int rank = rankManager.getRankOrUnranked(target.getUniqueId());
            int hearts = rank > 0 ? rankManager.getExtraHearts(rank) : 0;
            int potion = rank > 0 ? rankManager.getPotionMinutes(rank) : 0;
            String rankValue = rank > 0 ? String.valueOf(rank) : "Unranked";
            sender.sendMessage(TextUtils.color(configManager.getMessage("rank-info")
                    .replace("%player%", target.getName() != null ? target.getName() : "Unknown")
                    .replace("%rank%", rankValue)
                    .replace("%hearts%", String.valueOf(hearts))
                    .replace("%potion%", String.valueOf(potion))));
            return true;
        }
        if (args[0].equalsIgnoreCase("top")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("rank-top-header")));
            Map<Integer, Integer> counts = rankManager.getAllRankCounts();
            int max = configManager.getMaxPlayersPerRank();
            for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
                int count = counts.getOrDefault(rank, 0);
                sender.sendMessage(TextUtils.color(configManager.getMessage("rank-top-entry")
                        .replace("%rank%", String.valueOf(rank))
                        .replace("%count%", String.valueOf(count))
                        .replace("%max%", String.valueOf(max))));
            }
            sender.sendMessage(TextUtils.color(configManager.getMessage("rank-top-unranked")
                    .replace("%count%", String.valueOf(rankManager.getUnrankedCount()))));
            return true;
        }
        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtils.color("&eRank Commands:"));
        sender.sendMessage(TextUtils.color("&7/rank info <player> &f- View a player's rank info."));
        sender.sendMessage(TextUtils.color("&7/rank top &f- View rank population."));
    }
}
