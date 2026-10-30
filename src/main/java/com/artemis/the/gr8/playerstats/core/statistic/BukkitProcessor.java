package com.artemis.the.gr8.playerstats.core.statistic;

import com.artemis.the.gr8.playerstats.api.StatRequest;
import com.artemis.the.gr8.playerstats.api.StatResult;
import com.artemis.the.gr8.playerstats.core.config.ConfigHandler;
import com.artemis.the.gr8.playerstats.core.msg.OutputManager;
import com.artemis.the.gr8.playerstats.core.msg.msgutils.FormattingFunction;
import com.artemis.the.gr8.playerstats.core.multithreading.ThreadManager;
import com.artemis.the.gr8.playerstats.core.sharing.ShareManager;
import com.artemis.the.gr8.playerstats.core.utils.MyLogger;
import com.artemis.the.gr8.playerstats.core.utils.OfflinePlayerHandler;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

class BukkitProcessor extends RequestProcessor {

    private static final long CACHE_EXPIRY_MILLIS = 300_000L;
    private final ConcurrentHashMap<String, CachedStats> statsCache;
    private final OutputManager outputManager;
    private final ConfigHandler config;
    private final ShareManager shareManager;
    private final OfflinePlayerHandler offlinePlayerHandler;

    private static class CachedStats {
        private volatile long timestamp;
        private final ConcurrentHashMap<String, Integer> stats;
        private volatile List<Map.Entry<String, Integer>> sortedEntries;

        CachedStats(long timestamp, ConcurrentHashMap<String, Integer> stats) {
            this.timestamp = timestamp;
            this.stats = stats;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MILLIS;
        }

        void touch() {
            this.timestamp = System.currentTimeMillis();
        }

        ConcurrentHashMap<String, Integer> stats() {
            return stats;
        }

        List<Map.Entry<String, Integer>> getSortedEntries() {
            List<Map.Entry<String, Integer>> local = sortedEntries;
            if (local == null) {
                synchronized (this) {
                    local = sortedEntries;
                    if (local == null) {
                        local = stats.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .toList();
                        sortedEntries = local;
                    }
                }
            }
            return local;
        }
    }

    public BukkitProcessor(OutputManager outputManager) {
        this.outputManager = outputManager;

        config = ConfigHandler.getInstance();
        shareManager = ShareManager.getInstance();
        offlinePlayerHandler = OfflinePlayerHandler.getInstance();
        statsCache = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull StatResult<Integer> processPlayerRequest(StatRequest<?> playerStatRequest) {
        StatRequest.Settings requestSettings = playerStatRequest.getSettings();
        int stat = getPlayerStat(requestSettings);
        FormattingFunction formattingFunction = outputManager.formatPlayerStat(requestSettings, stat);
        TextComponent formattedResult = processFunction(requestSettings.getCommandSender(), formattingFunction);
        String resultAsString = outputManager.textComponentToString(formattedResult);

        return new StatResult<>(stat, formattedResult, resultAsString);
    }

    @Override
    public @NotNull StatResult<Long> processServerRequest(StatRequest<?> serverStatRequest) {
        StatRequest.Settings requestSettings = serverStatRequest.getSettings();
        long stat = getServerStat(requestSettings);
        FormattingFunction formattingFunction = outputManager.formatServerStat(requestSettings, stat);
        TextComponent formattedResult = processFunction(requestSettings.getCommandSender(), formattingFunction);
        String resultAsString = outputManager.textComponentToString(formattedResult);

        return new StatResult<>(stat, formattedResult, resultAsString);
    }

    @Override
    public @NotNull StatResult<LinkedHashMap<String, Integer>> processTopRequest(StatRequest<?> topStatRequest) {
        StatRequest.Settings requestSettings = topStatRequest.getSettings();
        LinkedHashMap<String, Integer> stats = getTopStats(requestSettings);
        FormattingFunction formattingFunction = outputManager.formatTopStats(requestSettings, stats);
        TextComponent formattedResult = processFunction(requestSettings.getCommandSender(), formattingFunction);
        String resultAsString = outputManager.textComponentToString(formattedResult);

        return new StatResult<>(stats, formattedResult, resultAsString);
    }

    private int getPlayerStat(@NotNull StatRequest.Settings requestSettings) {
        OfflinePlayer player;
        if (offlinePlayerHandler.isExcludedPlayer(requestSettings.getPlayerName()) &&
                config.allowPlayerLookupsForExcludedPlayers()) {
            player = offlinePlayerHandler.getExcludedOfflinePlayer(requestSettings.getPlayerName());
        } else {
            player = offlinePlayerHandler.getIncludedOfflinePlayer(requestSettings.getPlayerName());
        }
        return switch (requestSettings.getStatistic().getType()) {
            case UNTYPED -> player.getStatistic(requestSettings.getStatistic());
            case ENTITY -> player.getStatistic(requestSettings.getStatistic(), requestSettings.getEntity());
            case BLOCK -> player.getStatistic(requestSettings.getStatistic(), requestSettings.getBlock());
            case ITEM -> player.getStatistic(requestSettings.getStatistic(), requestSettings.getItem());
        };
    }

    private long getServerStat(StatRequest.Settings requestSettings) {
        List<Integer> numbers = getAllStatsAsync(requestSettings)
                .values()
                .parallelStream()
                .toList();
        return numbers.parallelStream().mapToLong(Integer::longValue).sum();
    }

    private LinkedHashMap<String, Integer> getTopStats(StatRequest.Settings requestSettings) {
        CachedStats cached = getCachedStats(requestSettings);
        cached.touch();
        List<Map.Entry<String, Integer>> sorted = cached.getSortedEntries();

        int pageSize = Math.max(1, requestSettings.getTopListSize());
        int totalPlayers = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalPlayers / pageSize));

        int maxPages = config.getTopListMaxPages();
        if (maxPages > 0) {
            totalPages = Math.min(totalPages, maxPages);
        }
        requestSettings.setTotalPages(totalPages);

        int requestedPage = requestSettings.getPageNumber();
        int page = Math.min(Math.max(1, requestedPage), totalPages);
        requestSettings.setPageNumber(page);

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalPlayers);

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>(pageSize);
        for (int i = fromIndex; i < toIndex; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private TextComponent processFunction(CommandSender sender, FormattingFunction function) {
        if (outputShouldBeStored(sender)) {
            int shareCode = shareManager.saveStatResult(sender.getName(), function.getResultWithSharerName(sender));
            return function.getResultWithShareButton(shareCode);
        }
        return function.getDefaultResult();
    }

    private boolean outputShouldBeStored(CommandSender sender) {
        return !(sender instanceof ConsoleCommandSender) &&
                shareManager.isEnabled() &&
                shareManager.senderHasPermission(sender);
    }

    private @NotNull ConcurrentHashMap<String, Integer> getAllStatsAsync(StatRequest.Settings requestSettings) {
        return getCachedStats(requestSettings).stats();
    }

    private @NotNull CachedStats getCachedStats(StatRequest.Settings requestSettings) {
        String cacheKey = getCacheKey(requestSettings);
        CachedStats cached = statsCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        long time = System.currentTimeMillis();

        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        ConcurrentHashMap<String, Integer> allStats;

        try {
            allStats = commonPool.invoke(ThreadManager.getStatAction(requestSettings));
        } catch (ConcurrentModificationException e) {
            MyLogger.logWarning("The requestSettings could not be executed due to a ConcurrentModificationException. " +
                    "This likely happened because Bukkit hasn't fully initialized all player-data yet. " +
                    "Try again and it should be fine!");
            throw new ConcurrentModificationException(e.toString());
        }

        MyLogger.actionFinished();
        ThreadManager.recordCalcTime(System.currentTimeMillis() - time);
        MyLogger.logMediumLevelTask("Calculated all stats", time);

        CachedStats newCache = new CachedStats(System.currentTimeMillis(), allStats);
        statsCache.put(cacheKey, newCache);
        return newCache;
    }

    private String getCacheKey(StatRequest.Settings settings) {
        String stat = settings.getStatistic().toString();
        String sub = settings.getSubStatEntryName();
        return sub == null ? stat : stat + ":" + sub;
    }
}
