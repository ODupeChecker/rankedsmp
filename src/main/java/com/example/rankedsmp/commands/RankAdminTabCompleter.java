package com.example.rankedsmp.commands;

import com.example.rankedsmp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankAdminTabCompleter implements TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("reload", "set", "swap", "give", "reset", "resetall");
    private final ConfigManager configManager;

    public RankAdminTabCompleter(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "set" -> {
                if (args.length == 2) {
                    return matchPlayers(args[1]);
                }
                if (args.length == 3) {
                    return matchRanks(args[2]);
                }
            }
            case "swap" -> {
                if (args.length == 2) {
                    return matchPlayers(args[1]);
                }
                if (args.length == 3) {
                    return matchPlayers(args[2]);
                }
            }
            case "give", "reset" -> {
                if (args.length == 2) {
                    return matchPlayers(args[1]);
                }
            }
            default -> {
            }
        }
        return Collections.emptyList();
    }

    private List<String> matchPlayers(String input) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(input, players, completions);
        Collections.sort(completions);
        return completions;
    }

    private List<String> matchRanks(String input) {
        List<String> ranks = new ArrayList<>();
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            ranks.add(String.valueOf(rank));
        }
        ranks.add("unranked");
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(input, ranks, completions);
        Collections.sort(completions);
        return completions;
    }
}
