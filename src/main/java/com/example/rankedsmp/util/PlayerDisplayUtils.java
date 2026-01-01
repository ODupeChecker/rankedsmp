package com.example.rankedsmp.util;

import com.example.rankedsmp.rank.RankManager;
import org.bukkit.entity.Player;

public final class PlayerDisplayUtils {
    private PlayerDisplayUtils() {
    }

    public static String formatNameWithRank(Player player, RankManager rankManager) {
        int rank = rankManager.getRank(player.getUniqueId());
        return player.getName() + " [" + rank + "]";
    }
}
