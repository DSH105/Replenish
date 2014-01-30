package com.dsh105.replenish.util;

import com.dsh105.dshutils.logger.ConsoleLogger;
import com.dsh105.dshutils.logger.Logger;

import java.util.ArrayList;

public class ReplenishLogger extends ConsoleLogger {

    private static ArrayList<String> LOGGED_STACK_IDS = new ArrayList<String>();

    public static void logSavedStack(String id) {
        if (!LOGGED_STACK_IDS.contains(id)) {
            LOGGED_STACK_IDS.add(id);
            log(Logger.LogLevel.NORMAL, "Failed to find saved stack of ID " + id + ". Please check your configuration.");
        }
    }
}