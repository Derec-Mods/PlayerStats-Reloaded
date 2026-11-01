package com.artemis.the.gr8.playerstats.expansion.cache;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Originally authored by Artemis_the_gr8 (https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
 * Adapted and integrated directly into PlayerStats Reloaded.
 */
public class JoinAndQuitListener implements Listener {

    private static StatCache cache;

    public JoinAndQuitListener(StatCache statCache) {
        cache = statCache;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        cache.addOnlinePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        cache.removeOnlinePlayer(event.getPlayer());
    }
}
