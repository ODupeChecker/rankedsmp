package com.example.rankedsmp;

import com.example.rankedsmp.commands.RankAdminCommand;
import com.example.rankedsmp.commands.RankAdminTabCompleter;
import com.example.rankedsmp.commands.RankCommand;
import com.example.rankedsmp.commands.RankCommandTabCompleter;
import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.listeners.ChatListener;
import com.example.rankedsmp.listeners.DeathListener;
import com.example.rankedsmp.listeners.JoinListener;
import com.example.rankedsmp.listeners.PotionListener;
import com.example.rankedsmp.placeholder.RankPlaceholder;
import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.JoinDisplayManager;
import com.example.rankedsmp.util.PlayerDisplayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RankedSMP extends JavaPlugin {
    private ConfigManager configManager;
    private RankManager rankManager;
    private JoinDisplayManager joinDisplayManager;
    private RankPlaceholder placeholder;

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
        Bukkit.getPluginManager().registerEvents(new ChatListener(rankManager), this);

        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(rankManager, configManager));
            getCommand("rank").setTabCompleter(new RankCommandTabCompleter());
        }
        if (getCommand("rankadmin") != null) {
            getCommand("rankadmin").setExecutor(new RankAdminCommand(this, rankManager, configManager));
            getCommand("rankadmin").setTabCompleter(new RankAdminTabCompleter(configManager));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = new RankPlaceholder(rankManager);
            placeholder.register();
        }

        updateAllPlayerDisplays();
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

    public void updatePlayerDisplay(Player player) {
        if (player == null) {
            return;
        }
        String displayName = PlayerDisplayUtils.formatNameWithRank(player, rankManager);
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
        updatePlaceholders(player);
    }

    public void updateAllPlayerDisplays() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerDisplay(player);
        }
    }
}
