package com.artemis.the.gr8.playerstats.expansion;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Originally authored by Artemis_the_gr8 (https://github.com/Artemis-the-gr8/PlayerStatsExpansion)
 * Adapted and integrated directly into PlayerStats Reloaded.
 */
public class ExpansionLogger {

    private final static Logger myLogger = Logger.getLogger("PlayerStatsExpansion");
    private final static ArrayList<String> loggedMessages = new ArrayList<>();
    private static Instant storedMessagesLastClearedTimestamp = Instant.now();

    public static void logInfo(String msg) {
        myLogger.info(msg);
    }

    public static void logWarning(String msg) {
        if (storedMessagesNeedClearing()) {
            clearStoredMessages();
        }
        if (!loggedMessages.contains(msg)) {
            loggedMessages.add(msg);
            myLogger.warning(msg);
        }
    }

    public static void clear() {
        loggedMessages.clear();
    }

    private static boolean storedMessagesNeedClearing() {
        return storedMessagesLastClearedTimestamp.until(Instant.now(), ChronoUnit.SECONDS) > 30;
    }

    private static void clearStoredMessages() {
        storedMessagesLastClearedTimestamp = Instant.now();
        clear();
    }
}
