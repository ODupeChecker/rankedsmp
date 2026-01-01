package com.example.rankedsmp.listeners;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.JoinDisplayManager;
import com.example.rankedsmp.util.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final RankedSMP plugin;
    private final RankManager rankManager;
    private final ConfigManager configManager;
    private final JoinDisplayManager joinDisplayManager;

    public JoinListener(RankedSMP plugin, RankManager rankManager, ConfigManager configManager,
                        JoinDisplayManager joinDisplayManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.configManager = configManager;
        this.joinDisplayManager = joinDisplayManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int rank = rankManager.getRankOrUnranked(player.getUniqueId());
        if (!rankManager.hasRank(player.getUniqueId())) {
            rank = rankManager.assignRandomRankIfAvailable(player.getUniqueId());
            if (rank > 0) {
                player.sendMessage(TextUtils.color(configManager.getMessage("rank-assigned")
                        .replace("%rank%", String.valueOf(rank))));
            } else {
                player.sendMessage(TextUtils.color(configManager.getMessage("rank-unranked")));
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                rankManager.applyHealthBonus(player);
            }
        });
        plugin.updatePlaceholders(player);
        plugin.updateLuckPermsPrefix(player, rank);
        joinDisplayManager.showJoinDisplay(player);
    }
}
