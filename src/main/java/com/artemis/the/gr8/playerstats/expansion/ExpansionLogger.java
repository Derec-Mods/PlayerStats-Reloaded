package com.artemis.the.gr8.playerstats.expansion;

import com.artemis.the.gr8.playerstats.core.config.ConfigHandler;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Originally authored by Artemis_the_gr8 (https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
 * Adapted and integrated directly into PlayerStats Reloaded.
 */
public class ExpansionLogger {

    private final static Logger myLogger = Logger.getLogger("PlayerStatsExpansion");
    private final static ArrayList<String> loggedMessages = new ArrayList<>();

    public static void logInfo(String msg) {
        myLogger.info(msg);
    }

    public static void logWarning(String msg) {
        try {
            if (ConfigHandler.getInstance().getDebugLevel() < 2) {
                return;
            }
        } catch (Exception ignored) {
        }
        if (!loggedMessages.contains(msg)) {
            loggedMessages.add(msg);
            myLogger.warning(msg);
        }
    }

    public static void clear() {
        loggedMessages.clear();
    }
}

