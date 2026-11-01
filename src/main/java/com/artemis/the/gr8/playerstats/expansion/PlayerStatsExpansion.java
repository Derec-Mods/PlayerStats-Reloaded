package com.artemis.the.gr8.playerstats.expansion;

import com.artemis.the.gr8.playerstats.api.PlayerStats;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Originally authored by Artemis_the_gr8 (https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
 * Adapted and integrated directly into PlayerStats Reloaded.
 */
public class PlayerStatsExpansion extends PlaceholderExpansion implements Configurable, Cacheable {

    final static String VERSION = "2.7.4";
    final static String NEEDED_PLAYERSTATS_API_VERSION = "2";
    private static PlayerStatsExpansion instance;
    private static ExpansionConfig config;
    private PlaceholderProvider placeholderProvider;

    @Override
    public @NotNull String getIdentifier() {
        return "playerstats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Artemis_the_gr8";
    }

    @Override
    public @NotNull String getVersion() {
        return VERSION;
    }

    @Override
    public @NotNull String getRequiredPlugin() {
        return "PlayerStats";
    }

    @Override
    public @NotNull Map<String, Object> getDefaults() {
        Map<String, Object> configValues = new LinkedHashMap<>();
        configValues.put("update_interval_in_seconds.distance_statistics", 1);
        configValues.put("update_interval_in_seconds.time_statistics", 1);
        configValues.put("display.max_time_unit", "day");
        configValues.put("display.min_time_unit", "second");
        configValues.put("display.distance_unit", "blocks");
        configValues.put("display.damage_unit", "hearts");
        configValues.put("display.processing_message_color", "#ADE7FF");
        configValues.put("display.only_player_name_color", "");
        configValues.put("display.only_stat_number_color", "");
        configValues.put("display.only_position_number_color", "");

        return configValues;
    }

    @Override
    public void clear() {
        if (placeholderProvider != null) {
            placeholderProvider.clear();
        }
    }

    @Override
    public boolean canRegister() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlayerStats");
        return plugin != null;
    }

    @Override
    public boolean register() {
        if (!canRegister()) {
            return false;
        }

        PlayerStats playerStats;
        try {
            playerStats = PlayerStats.getAPI();
        } catch (IllegalStateException | NoClassDefFoundError e) {
            return false;
        }

        instance = this;
        placeholderProvider = new PlaceholderProvider(playerStats);
        return super.register();
    }

    public static @NotNull ExpansionConfig getExpansionConfig() {
        ExpansionConfig localVariable = config;
        if (localVariable != null) {
            return localVariable;
        }

        synchronized (PlayerStatsExpansion.class) {
            if (config == null) {
                config = new ExpansionConfig(instance);
            }
            return config;
        }
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String args) {
        if (placeholderProvider == null) {
            return null;
        }
        return placeholderProvider.onRequest(player, args);
    }
}
