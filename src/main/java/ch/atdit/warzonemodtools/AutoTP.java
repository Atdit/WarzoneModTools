package ch.atdit.warzonemodtools;

import java.util.List;

import static ch.atdit.warzonemodtools.util.Common.*;

public class AutoTP {
    public static String currentSuspect = null;
    public static List<String> midMessages = null;

    public static void tp(String player) {
        sendMessage("/tp " + player);
    }
}
