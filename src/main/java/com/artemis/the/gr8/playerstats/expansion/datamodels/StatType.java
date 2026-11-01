package com.artemis.the.gr8.playerstats.expansion.datamodels;

import com.artemis.the.gr8.playerstats.api.StatRequest;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Originally authored by Artemis_the_gr8 (https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
 * Adapted and integrated directly into PlayerStats Reloaded.
 */
public record StatType(Statistic statistic, @Nullable Material material, @Nullable EntityType entityType, boolean isWildcard) {

    public StatType(Statistic statistic, @Nullable Material material, @Nullable EntityType entityType) {
        this(statistic, material, entityType, false);
    }

    public static StatType fromRequest(StatRequest<?> request) {
        StatRequest.Settings settings = request.getSettings();
        Statistic stat = settings.getStatistic();
        if (settings.isWildcard()) {
            return new StatType(stat, null, null, true);
        }
        return switch (stat.getType()) {
            case UNTYPED -> new StatType(stat, null, null, false);
            case BLOCK -> new StatType(stat, settings.getBlock(), null, false);
            case ITEM -> new StatType(stat, settings.getItem(), null, false);
            case ENTITY -> new StatType(stat, null, settings.getEntity(), false);
        };
    }

    public String getSubStatName() {
        if (isWildcard) {
            return "*";
        }
        return switch (statistic().getType()) {
            case BLOCK, ITEM -> {
                Material material = material();
                if (material != null) {
                    yield material.toString();
                } else {
                    yield null;
                }
            }
            case ENTITY -> {
                EntityType entityType = entityType();
                if (entityType != null) {
                    yield entityType.toString();
                } else {
                    yield null;
                }
            }
            default -> null;
        };
    }
}

