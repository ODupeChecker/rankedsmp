package com.example.rankedsmp.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {
    private final JavaPlugin plugin;
    private int rankCount;
    private int maxPlayersPerRank;
    private boolean rankSwappingEnabled;
    private boolean potionBonusesEnabled;
    private boolean healthBonusesEnabled;
    private List<RankRange> rankRanges = new ArrayList<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        rankCount = Math.max(1, config.getInt("rank-count", 20));
        maxPlayersPerRank = Math.max(1, config.getInt("max-players-per-rank", 5));
        rankSwappingEnabled = config.getBoolean("features.rank-swapping", true);
        potionBonusesEnabled = config.getBoolean("features.potion-bonuses", true);
        healthBonusesEnabled = config.getBoolean("features.health-bonuses", true);
        rankRanges = loadRankRanges(config);
    }

    private List<RankRange> loadRankRanges(FileConfiguration config) {
        List<RankRange> ranges = new ArrayList<>();
        List<java.util.Map<?, ?>> list = config.getMapList("rank-ranges");
        for (java.util.Map<?, ?> entry : list) {
            ConfigurationSection section = config.createSection("temp", (java.util.Map<String, Object>) entry);
            ranges.add(RankRange.fromSection(section));
            config.set("temp", null);
        }
        return ranges;
    }

    public int getRankCount() {
        return rankCount;
    }

    public int getMaxPlayersPerRank() {
        return maxPlayersPerRank;
    }

    public boolean isRankSwappingEnabled() {
        return rankSwappingEnabled;
    }

    public boolean isPotionBonusesEnabled() {
        return potionBonusesEnabled;
    }

    public boolean isHealthBonusesEnabled() {
        return healthBonusesEnabled;
    }

    public int getExtraHeartsForRank(int rank) {
        if (rank <= 0) {
            return 0;
        }
        for (RankRange range : rankRanges) {
            if (range.matches(rank)) {
                return Math.max(0, range.extraHearts());
            }
        }
        return 0;
    }

    public int getPotionMinutesForRank(int rank) {
        if (rank <= 0) {
            return 0;
        }
        for (RankRange range : rankRanges) {
            if (range.matches(rank)) {
                return Math.max(0, range.potionMinutes());
            }
        }
        return 0;
    }

    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    public List<RankRange> getRankRanges() {
        return Collections.unmodifiableList(rankRanges);
    }

    public record RankRange(int min, int max, int potionMinutes, int extraHearts) {
        public boolean matches(int rank) {
            return rank >= min && rank <= max;
        }

        public static RankRange fromSection(ConfigurationSection section) {
            int min = section.getInt("min", 1);
            int max = section.getInt("max", min);
            int potionMinutes = section.getInt("potion-minutes", 0);
            int extraHearts = section.getInt("extra-hearts", 0);
            return new RankRange(min, max, potionMinutes, extraHearts);
        }
    }
}
