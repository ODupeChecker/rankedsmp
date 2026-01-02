package com.example.rankedsmp.commands;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankAdminCommand implements CommandExecutor {
    private final RankedSMP plugin;
    private final RankManager rankManager;
    private final ConfigManager configManager;

    public RankAdminCommand(RankedSMP plugin, RankManager rankManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "set" -> handleSet(sender, args);
            case "swap" -> handleSwap(sender, args);
            case "give" -> handleGive(sender, args);
            case "reset" -> handleReset(sender, args);
            case "resetall" -> handleResetAll(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("rankedsmp.admin.reload")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        configManager.reload();
        rankManager.applyHealthBonusToOnline();
        sender.sendMessage(TextUtils.color(configManager.getMessage("admin-reload")));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rankedsmp.admin.set")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        if (args.length != 3) {
            sendHelp(sender);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("player-not-found")));
            return;
        }
        String rankArg = args[2];
        if (rankArg.equalsIgnoreCase("unranked")) {
            rankManager.resetRank(target.getUniqueId());
            updateOnline(target);
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-set")
                    .replace("%player%", target.getName() != null ? target.getName() : "Unknown")
                    .replace("%rank%", configManager.getUnrankedLabel())));
            return;
        }
        int rank;
        try {
            rank = Integer.parseInt(rankArg);
        } catch (NumberFormatException ex) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-invalid-rank")));
            return;
        }
        if (rank <= 0 || rank > configManager.getRankCount()) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-invalid-rank")));
            return;
        }
        boolean success = rankManager.setRankIfAvailable(target.getUniqueId(), rank);
        if (!success) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-set-full")
                    .replace("%rank%", String.valueOf(rank))));
            return;
        }
        updateOnline(target);
        sender.sendMessage(TextUtils.color(configManager.getMessage("admin-set")
                .replace("%player%", target.getName() != null ? target.getName() : "Unknown")
                .replace("%rank%", String.valueOf(rank))));
    }

    private void handleSwap(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rankedsmp.admin.swap")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        if (args.length != 3) {
            sendHelp(sender);
            return;
        }
        OfflinePlayer first = Bukkit.getOfflinePlayer(args[1]);
        OfflinePlayer second = Bukkit.getOfflinePlayer(args[2]);
        if (first == null || second == null) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("player-not-found")));
            return;
        }
        boolean swapped = rankManager.swapRanks(first.getUniqueId(), second.getUniqueId());
        if (swapped) {
            updateOnline(first);
            updateOnline(second);
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-swap")
                    .replace("%player1%", first.getName() != null ? first.getName() : "Unknown")
                    .replace("%player2%", second.getName() != null ? second.getName() : "Unknown")));
        } else {
            sender.sendMessage(TextUtils.color(configManager.getMessage("admin-invalid-rank")));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rankedsmp.admin.give")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        if (args.length != 2) {
            sendHelp(sender);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("player-not-found")));
            return;
        }
        int rank = rankManager.assignRandomRankIfAvailable(target.getUniqueId());
        updateOnline(target);
        sender.sendMessage(TextUtils.color(configManager.getMessage("admin-give")
                .replace("%player%", target.getName() != null ? target.getName() : "Unknown")
                .replace("%rank%", rank > 0 ? String.valueOf(rank) : configManager.getUnrankedLabel())));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rankedsmp.admin.reset")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        if (args.length != 2) {
            sendHelp(sender);
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.hasPlayedBefore())) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("player-not-found")));
            return;
        }
        rankManager.resetRank(target.getUniqueId());
        updateOnline(target);
        sender.sendMessage(TextUtils.color(configManager.getMessage("admin-reset")
                .replace("%player%", target.getName() != null ? target.getName() : "Unknown")));
    }

    private void handleResetAll(CommandSender sender) {
        if (!sender.hasPermission("rankedsmp.admin.resetall")) {
            sender.sendMessage(TextUtils.color(configManager.getMessage("no-permission")));
            return;
        }
        rankManager.resetAll();
        rankManager.applyHealthBonusToOnline();
        sender.sendMessage(TextUtils.color(configManager.getMessage("admin-resetall")));
    }

    private void updateOnline(OfflinePlayer player) {
        if (player.isOnline()) {
            Player online = player.getPlayer();
            if (online != null) {
                rankManager.applyHealthBonusWithRetry(online);
                plugin.updatePlaceholders(online);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtils.color("&eRank Admin Commands:"));
        sender.sendMessage(TextUtils.color("&7/rankadmin reload"));
        sender.sendMessage(TextUtils.color("&7/rankadmin set <player> <rank|unranked>"));
        sender.sendMessage(TextUtils.color("&7/rankadmin swap <player1> <player2>"));
        sender.sendMessage(TextUtils.color("&7/rankadmin give <player>"));
        sender.sendMessage(TextUtils.color("&7/rankadmin reset <player>"));
        sender.sendMessage(TextUtils.color("&7/rankadmin resetall"));
    }
}
