package com.example.rankedsmp.rank;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RankManager {
    public static final int UNRANKED = -1;

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Integer> ranks = new HashMap<>();
    private final Random random = new Random();
    private final File dataFile;

    public RankManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataFile = new File(plugin.getDataFolder(), "ranks.yml");
        loadData();
    }

    public void reload() {
        loadData();
    }

    public int getRank(UUID uuid) {
        return ranks.getOrDefault(uuid, UNRANKED);
    }

    public boolean hasRankEntry(UUID uuid) {
        return ranks.containsKey(uuid);
    }

    public boolean isRanked(UUID uuid) {
        return getRank(uuid) > 0;
    }

    public int getExtraHearts(int rank) {
        return configManager.getExtraHeartsForRank(rank);
    }

    public int getPotionMinutes(int rank) {
        return configManager.getPotionMinutesForRank(rank);
    }

    public boolean hasAvailableRankSlot() {
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            if (getRankCount(rank) < configManager.getMaxPlayersPerRank()) {
                return true;
            }
        }
        return false;
    }

    public int assignRandomRank(UUID uuid) {
        List<Integer> available = new ArrayList<>();
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            if (getRankCount(rank) < configManager.getMaxPlayersPerRank()) {
                available.add(rank);
            }
        }
        if (available.isEmpty()) {
            setRank(uuid, UNRANKED, true);
            return UNRANKED;
        }
        int rank = available.get(random.nextInt(available.size()));
        setRank(uuid, rank, true);
        return rank;
    }

    public boolean setRank(UUID uuid, int rank) {
        return setRank(uuid, rank, false);
    }

    private boolean setRank(UUID uuid, int rank, boolean bypassFullCheck) {
        if (rank <= 0) {
            ranks.put(uuid, UNRANKED);
            saveData();
            updateLuckPermsPrefix(uuid, UNRANKED);
            return true;
        }
        if (rank > configManager.getRankCount()) {
            return false;
        }
        int currentRank = getRank(uuid);
        if (!bypassFullCheck && currentRank != rank && getRankCount(rank) >= configManager.getMaxPlayersPerRank()) {
            return false;
        }
        ranks.put(uuid, rank);
        saveData();
        updateLuckPermsPrefix(uuid, rank);
        return true;
    }

    public void resetRank(UUID uuid) {
        ranks.put(uuid, UNRANKED);
        saveData();
        updateLuckPermsPrefix(uuid, UNRANKED);
    }

    public void resetAll() {
        ranks.clear();
        saveData();
        updateLuckPermsForOnlinePlayers();
    }

    public boolean swapRanks(UUID first, UUID second) {
        int rankA = getRank(first);
        int rankB = getRank(second);
        if (rankA <= 0 || rankB <= 0 || rankA == rankB) {
            return false;
        }
        ranks.put(first, rankB);
        ranks.put(second, rankA);
        saveData();
        updateLuckPermsPrefix(first, rankB);
        updateLuckPermsPrefix(second, rankA);
        return true;
    }

    public int getRankCount(int rank) {
        int count = 0;
        for (int value : ranks.values()) {
            if (value == rank) {
                count++;
            }
        }
        return count;
    }

    public int getUnrankedCount() {
        int count = 0;
        for (int value : ranks.values()) {
            if (value <= 0) {
                count++;
            }
        }
        return count;
    }

    public Map<Integer, Integer> getAllRankCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            counts.put(rank, getRankCount(rank));
        }
        return counts;
    }

    public void applyHealthBonus(Player player) {
        if (player == null) {
            return;
        }
        double base = 20.0;
        double extraHearts = 0.0;
        int rank = getRank(player.getUniqueId());
        if (configManager.isHealthBonusesEnabled() && rank > 0) {
            extraHearts = Math.max(0, configManager.getExtraHeartsForRank(rank)) * 2.0;
        }
        double maxHealth = base + extraHearts;
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        }
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    public void applyHealthBonusToOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyHealthBonus(player);
        }
    }

    private void loadData() {
        ranks.clear();
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (!config.isConfigurationSection("players")) {
            return;
        }
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            int rank = config.getInt("players." + key + ".rank", UNRANKED);
            if (rank > configManager.getRankCount() || rank < 0) {
                rank = UNRANKED;
            }
            ranks.put(uuid, rank);
        }
    }

    private void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : ranks.entrySet()) {
            config.set("players." + entry.getKey() + ".rank", entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save ranks.yml: " + ex.getMessage());
        }
    }

    private void updateLuckPermsPrefix(UUID uuid, int rank) {
        if (plugin instanceof RankedSMP rankedSMP) {
            rankedSMP.updateLuckPermsPrefix(uuid, rank);
        }
    }

    private void updateLuckPermsForOnlinePlayers() {
        if (!(plugin instanceof RankedSMP rankedSMP)) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            int rank = getRank(player.getUniqueId());
            rankedSMP.updateLuckPermsPrefix(player.getUniqueId(), rank);
        }
    }
}
