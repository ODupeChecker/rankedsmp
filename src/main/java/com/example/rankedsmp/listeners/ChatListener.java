package com.example.rankedsmp.listeners;

import com.example.rankedsmp.rank.RankManager;
import com.example.rankedsmp.util.PlayerDisplayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final RankManager rankManager;

    public ChatListener(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String displayName = PlayerDisplayUtils.formatNameWithRank(player, rankManager);
        event.setFormat(displayName + ": %2$s");
    }
}
