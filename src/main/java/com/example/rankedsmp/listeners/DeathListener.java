package com.example.rankedsmp.listeners;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final RankedSMP plugin;
    private final RankManager rankManager;
    private final ConfigManager configManager;

    public DeathListener(RankedSMP plugin, RankManager rankManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isRankSwappingEnabled()) {
            return;
        }
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        int victimRank = rankManager.getRank(victim.getUniqueId());
        int killerRank = rankManager.getRank(killer.getUniqueId());
        if (victimRank <= 0 || killerRank <= 0) {
            return;
        }
        if (killerRank > victimRank) {
            rankManager.swapRanks(killer.getUniqueId(), victim.getUniqueId());
            rankManager.applyHealthBonus(killer);
            rankManager.applyHealthBonus(victim);
            plugin.updatePlaceholders(killer);
            plugin.updatePlaceholders(victim);
        }
    }
}
