package com.example.rankedsmp.listeners;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final RankedSMP plugin;
    private final RankManager rankManager;
    private final ConfigManager configManager;

    public JoinListener(RankedSMP plugin, RankManager rankManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!rankManager.hasRankEntry(player.getUniqueId())) {
            int rank = rankManager.assignRandomRank(player.getUniqueId());
            if (rank > 0) {
                player.sendMessage(TextUtils.color(configManager.getMessage("rank-assigned")
                        .replace("%rank%", String.valueOf(rank))));
            } else {
                player.sendMessage(TextUtils.color(configManager.getMessage("rank-unranked")));
            }
        }
        rankManager.applyHealthBonus(player);
        plugin.updatePlaceholders(player);
    }
}
