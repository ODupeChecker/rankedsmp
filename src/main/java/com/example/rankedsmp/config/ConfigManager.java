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
    private boolean joinDisplayEnabled;
    private String joinDisplayTitle;
    private String joinDisplaySubtitleRanked;
    private String joinDisplaySubtitleUnranked;
    private int joinDisplayFadeIn;
    private int joinDisplayStay;
    private int joinDisplayFadeOut;
    private boolean joinDisplaySendChat;
    private String joinDisplayChatMessage;
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
        joinDisplayEnabled = config.getBoolean("join-display.enabled", true);
        joinDisplayTitle = config.getString("join-display.title", "§6§lWELCOME");
        joinDisplaySubtitleRanked = config.getString("join-display.subtitle-ranked", "§eYour Rank: §6%rank%");
        joinDisplaySubtitleUnranked = config.getString("join-display.subtitle-unranked", "§7You are Unranked");
        joinDisplayFadeIn = Math.max(0, config.getInt("join-display.fade-in", 10));
        joinDisplayStay = Math.max(0, config.getInt("join-display.stay", 60));
        joinDisplayFadeOut = Math.max(0, config.getInt("join-display.fade-out", 10));
        joinDisplaySendChat = config.getBoolean("join-display.send-chat-message", false);
        joinDisplayChatMessage = config.getString("join-display.chat-message", "§aWelcome! Your rank is §e%rank%");
        rankRanges = loadRankRanges(config);
    }

    private List<RankRange> loadRankRanges(FileConfiguration config) {
        List<RankRange> ranges = new ArrayList<>();
        List<java.util.Map<?, ?>> list = config.getMapList("rank-ranges");
        for (java.util.Map<?, ?> entry : list) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) entry;
            ConfigurationSection section = config.createSection("temp", map);
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

    public boolean isJoinDisplayEnabled() {
        return joinDisplayEnabled;
    }

    public String getJoinDisplayTitle() {
        return joinDisplayTitle;
    }

    public String getJoinDisplaySubtitleRanked() {
        return joinDisplaySubtitleRanked;
    }

    public String getJoinDisplaySubtitleUnranked() {
        return joinDisplaySubtitleUnranked;
    }

    public int getJoinDisplayFadeIn() {
        return joinDisplayFadeIn;
    }

    public int getJoinDisplayStay() {
        return joinDisplayStay;
    }

    public int getJoinDisplayFadeOut() {
        return joinDisplayFadeOut;
    }

    public boolean isJoinDisplaySendChat() {
        return joinDisplaySendChat;
    }

    public String getJoinDisplayChatMessage() {
        return joinDisplayChatMessage;
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
