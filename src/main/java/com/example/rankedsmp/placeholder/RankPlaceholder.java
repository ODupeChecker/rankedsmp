package com.example.rankedsmp.placeholder;

import com.example.rankedsmp.rank.RankManager;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class RankPlaceholder extends PlaceholderExpansion {
    private final RankManager rankManager;
    private final String unrankedLabel;

    public RankPlaceholder(RankManager rankManager, String unrankedLabel) {
        this.rankManager = rankManager;
        this.unrankedLabel = unrankedLabel;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "rank";
    }

    @Override
    public String getAuthor() {
        return "OpenAI";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return unrankedLabel;
        }
        int rank = rankManager.getRank(player.getUniqueId());
        if (rank <= 0) {
            return unrankedLabel;
        }
        return String.valueOf(rank);
    }
}
