package com.example.rankedsmp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankCommandTabCompleter implements TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("info", "top");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], players, completions);
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
}
