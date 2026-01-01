package com.example.rankedsmp.listeners;

import com.example.rankedsmp.config.ConfigManager;
import com.example.rankedsmp.rank.RankManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Set;

public class PotionListener implements Listener {
    private static final Set<PotionEffectType> ALLOWED = Set.of(
            PotionEffectType.SPEED,
            PotionEffectType.STRENGTH,
            PotionEffectType.FIRE_RESISTANCE
    );

    private final RankManager rankManager;
    private final ConfigManager configManager;

    public PotionListener(RankManager rankManager, ConfigManager configManager) {
        this.rankManager = rankManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!configManager.isPotionBonusesEnabled()) {
            return;
        }
        ProjectileSource source = event.getPotion().getShooter();
        if (!(source instanceof Player player)) {
            return;
        }
        if (event.getIntensity(player) <= 0.0) {
            return;
        }
        int rank = rankManager.getRankOrUnranked(player.getUniqueId());
        if (rank <= 0) {
            return;
        }
        int minutes = rankManager.getPotionMinutes(rank);
        if (minutes <= 0) {
            return;
        }
        int duration = minutes * 60 * 20;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (!ALLOWED.contains(effect.getType())) {
                continue;
            }
            PotionEffectType type = effect.getType();
            for (LivingEntity affected : event.getAffectedEntities()) {
                if (affected.getUniqueId().equals(player.getUniqueId())) {
                    affected.removePotionEffect(type);
                    affected.addPotionEffect(new PotionEffect(type, duration, effect.getAmplifier(), false, true));
                }
            }
        }
    }
}
