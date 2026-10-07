package com.artemis.the.gr8.playerstats.core.msg.msgutils;

import org.bukkit.map.MinecraftFont;

/**
 * A small utility class that helps calculate how many dots
 * to use to get the numbers of a top-statistic aligned.
 */
public final class FontUtils {

    public static final int DEFAULT_ALIGN_WIDTH = 130;

    private FontUtils() {
    }

    public static int getNumberOfDotsToAlign(String displayText) {
        return getNumberOfDotsToAlign(displayText, DEFAULT_ALIGN_WIDTH);
    }

    public static int getNumberOfDotsToAlign(String displayText, int alignWidth) {
        return (int) Math.round((alignWidth - MinecraftFont.Font.getWidth(displayText))/2.0);
    }

    public static int getNumberOfDotsToAlignForConsole(String displayText) {
        return getNumberOfDotsToAlignForConsole(displayText, DEFAULT_ALIGN_WIDTH);
    }

    public static int getNumberOfDotsToAlignForConsole(String displayText, int alignWidth) {
        return (int) Math.round((alignWidth - MinecraftFont.Font.getWidth(displayText))/6.0) + 7;
    }

    public static int getNumberOfDotsToAlignForBoldText(String displayText) {
        return getNumberOfDotsToAlignForBoldText(displayText, DEFAULT_ALIGN_WIDTH);
    }

    public static int getNumberOfDotsToAlignForBoldText(String displayText, int alignWidth) {
        return (int) Math.round((alignWidth - (MinecraftFont.Font.getWidth(displayText) * 1.5))/2.0);
    }
}