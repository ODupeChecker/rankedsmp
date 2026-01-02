package com.example.rankedsmp.util;

import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class JoinDisplayManager {
    private final ConfigManager configManager;
    private final RankManager rankManager;
    private boolean placeholderApiAvailable;

    public JoinDisplayManager(ConfigManager configManager, RankManager rankManager) {
        this.configManager = configManager;
        this.rankManager = rankManager;
        this.placeholderApiAvailable = isPlaceholderApiAvailable();
    }

    public void showJoinDisplay(Player player) {
        if (player == null || !configManager.isJoinDisplayEnabled()) {
            return;
        }
        int rank = rankManager.getRankOrUnranked(player.getUniqueId());
        boolean ranked = rank > 0;
        String titleText = applyPlaceholders(player, configManager.getJoinDisplayTitle(), rank);
        String subtitleTemplate = ranked
                ? configManager.getJoinDisplaySubtitleRanked()
                : configManager.getJoinDisplaySubtitleUnranked();
        String subtitleText = applyPlaceholders(player, subtitleTemplate, rank);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(configManager.getJoinDisplayFadeIn() * 50L),
                Duration.ofMillis(configManager.getJoinDisplayStay() * 50L),
                Duration.ofMillis(configManager.getJoinDisplayFadeOut() * 50L)
        );
        Component title = TextUtils.color(titleText);
        Component subtitle = TextUtils.color(subtitleText);
        player.showTitle(Title.title(title, subtitle, times));

        if (configManager.isJoinDisplaySendChat()) {
            String chat = applyPlaceholders(player, configManager.getJoinDisplayChatMessage(), rank);
            player.sendMessage(TextUtils.color(chat));
        }
    }

    private String applyPlaceholders(Player player, String text, int rank) {
        if (text == null) {
            return "";
        }
        String replaced = text.replace("%rank%", rank > 0 ? String.valueOf(rank) : configManager.getUnrankedLabel());
        if (placeholderApiAvailable) {
            try {
                replaced = PlaceholderAPI.setPlaceholders(player, replaced);
            } catch (NoClassDefFoundError ex) {
                placeholderApiAvailable = false;
            }
        }
        return replaced;
    }

    private boolean isPlaceholderApiAvailable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return false;
        }
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
