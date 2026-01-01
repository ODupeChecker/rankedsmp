package com.example.rankedsmp.rank;

import com.example.rankedsmp.RankedSMP;
import com.example.rankedsmp.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
    private final Map<Integer, Integer> rankCounts = new HashMap<>();
    private int unrankedCount = 0;
    private final Random random = new Random();
    private final File dataFile;

    public RankManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataFile = new File(plugin.getDataFolder(), "ranks.yml");
        loadAll();
    }

    public void reload() {
        loadAll();
    }

    public boolean hasRank(UUID uuid) {
        return ranks.containsKey(uuid);
    }

    public Integer getRank(UUID uuid) {
        return ranks.get(uuid);
    }

    public int getRankOrUnranked(UUID uuid) {
        return ranks.getOrDefault(uuid, UNRANKED);
    }

    public boolean isRanked(UUID uuid) {
        return getRankOrUnranked(uuid) > 0;
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

    public int assignRandomRankIfAvailable(UUID uuid) {
        if (hasRank(uuid)) {
            return getRankOrUnranked(uuid);
        }
        List<Integer> available = new ArrayList<>();
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            if (getRankCount(rank) < configManager.getMaxPlayersPerRank()) {
                available.add(rank);
            }
        }
        if (available.isEmpty()) {
            setRankInternal(uuid, UNRANKED, true);
            return UNRANKED;
        }
        int rank = available.get(random.nextInt(available.size()));
        setRankInternal(uuid, rank, true);
        return rank;
    }

    public void setRank(UUID uuid, int rank) {
        setRankInternal(uuid, rank, true);
    }

    public boolean setRankIfAvailable(UUID uuid, int rank) {
        return setRankInternal(uuid, rank, false);
    }

    private boolean setRankInternal(UUID uuid, int rank, boolean bypassFullCheck) {
        if (rank <= 0) {
            setRankValue(uuid, UNRANKED);
            saveNow();
            updateLuckPermsPrefix(uuid, UNRANKED);
            return true;
        }
        if (rank > configManager.getRankCount()) {
            return false;
        }
        int currentRank = getRankOrUnranked(uuid);
        if (!bypassFullCheck && currentRank != rank && getRankCount(rank) >= configManager.getMaxPlayersPerRank()) {
            return false;
        }
        setRankValue(uuid, rank);
        saveNow();
        updateLuckPermsPrefix(uuid, rank);
        return true;
    }

    public void resetRank(UUID uuid) {
        setRankValue(uuid, UNRANKED);
        saveNow();
        updateLuckPermsPrefix(uuid, UNRANKED);
    }

    public void resetAll() {
        ranks.clear();
        rebuildCountsFromRanks();
        saveNow();
        updateLuckPermsForOnlinePlayers();
    }

    public boolean swapRanks(UUID first, UUID second) {
        int rankA = getRankOrUnranked(first);
        int rankB = getRankOrUnranked(second);
        if (rankA <= 0 || rankB <= 0 || rankA == rankB) {
            return false;
        }
        setRankValue(first, rankB);
        setRankValue(second, rankA);
        saveNow();
        updateLuckPermsPrefix(first, rankB);
        updateLuckPermsPrefix(second, rankA);
        return true;
    }

    public int getRankCount(int rank) {
        return rankCounts.getOrDefault(rank, 0);
    }

    public int getUnrankedCount() {
        return unrankedCount;
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
        int rank = getRankOrUnranked(player.getUniqueId());
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

    public void applyHealthBonusWithRetry(Player player) {
        if (player == null) {
            return;
        }
        applyHealthBonus(player);
        new BukkitRunnable() {
            private int attempts = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                applyHealthBonus(player);
                attempts++;
                if (attempts >= 5) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 20L);
    }

    public void applyHealthBonusToOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyHealthBonusWithRetry(player);
        }
    }

    public void loadAll() {
        loadData();
        rebuildCountsFromRanks();
    }

    public void rebuildCountsFromRanks() {
        rankCounts.clear();
        unrankedCount = 0;
        for (int rank = 1; rank <= configManager.getRankCount(); rank++) {
            rankCounts.put(rank, 0);
        }
        for (int rank : ranks.values()) {
            if (rank > 0) {
                rankCounts.put(rank, rankCounts.getOrDefault(rank, 0) + 1);
            } else {
                unrankedCount++;
            }
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

    public void saveNow() {
        saveData();
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

    private void setRankValue(UUID uuid, int rank) {
        Integer previous = ranks.put(uuid, rank);
        if (previous != null) {
            adjustCounts(previous, rank);
        } else {
            adjustCounts(null, rank);
        }
    }

    private void adjustCounts(Integer previousRank, int newRank) {
        if (previousRank != null) {
            if (previousRank > 0) {
                rankCounts.put(previousRank, Math.max(0, rankCounts.getOrDefault(previousRank, 0) - 1));
            } else {
                unrankedCount = Math.max(0, unrankedCount - 1);
            }
        }

        if (newRank > 0) {
            rankCounts.put(newRank, rankCounts.getOrDefault(newRank, 0) + 1);
        } else {
            unrankedCount++;
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
            int rank = getRankOrUnranked(player.getUniqueId());
            rankedSMP.updateLuckPermsPrefix(player.getUniqueId(), rank);
        }
    }
}
