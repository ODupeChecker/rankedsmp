package com.example.rankedsmp;

import com.example.rankedsmp.commands.RankAdminCommand;
import com.example.rankedsmp.commands.RankCommand;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.listeners.DeathListener;
import com.example.rankedsmp.listeners.JoinListener;
import com.example.rankedsmp.listeners.PotionListener;
import com.example.rankedsmp.integration.LuckPermsIntegration;
import com.example.rankedsmp.placeholder.RankPlaceholder;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.JoinDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class RankedSMP extends JavaPlugin {
    private ConfigManager configManager;
    private RankManager rankManager;
    private JoinDisplayManager joinDisplayManager;
    private RankPlaceholder placeholder;
    private LuckPermsIntegration luckPermsIntegration;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        configManager = new ConfigManager(this);
        rankManager = new RankManager(this, configManager);
        joinDisplayManager = new JoinDisplayManager(configManager, rankManager);

        Bukkit.getPluginManager().registerEvents(new JoinListener(this, rankManager, configManager, joinDisplayManager), this);
        Bukkit.getPluginManager().registerEvents(new PotionListener(rankManager, configManager), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this, rankManager, configManager), this);

        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(rankManager, configManager));
        }
        if (getCommand("rankadmin") != null) {
            getCommand("rankadmin").setExecutor(new RankAdminCommand(this, rankManager, configManager));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = new RankPlaceholder(rankManager, configManager.getUnrankedLabel());
            placeholder.register();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            luckPermsIntegration = new LuckPermsIntegration();
        }
    }

    @Override
    public void onDisable() {
        if (placeholder != null) {
            placeholder.unregister();
        }
    }

    public void updatePlaceholders(Player player) {
        if (placeholder == null || player == null) {
            return;
        }
        // PlaceholderAPI handles updates via periodic refresh on supported plugins.
    }

    public LuckPermsIntegration getLuckPermsIntegration() {
        return luckPermsIntegration;
    }

    public void updateLuckPermsPrefix(Player player, int rank) {
        if (luckPermsIntegration == null || player == null) {
            return;
        }
        String rankLabel = rank > 0 ? String.valueOf(rank) : configManager.getUnrankedLabel();
        luckPermsIntegration.updateUserPrefix(player.getUniqueId(), rankLabel);
    }

    public void updateLuckPermsPrefix(UUID uuid, int rank) {
        if (luckPermsIntegration == null) {
            return;
        }
        String rankLabel = rank > 0 ? String.valueOf(rank) : configManager.getUnrankedLabel();
        luckPermsIntegration.updateUserPrefix(uuid, rankLabel);
    }
}
