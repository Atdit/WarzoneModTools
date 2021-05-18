package ch.atdit.warzonemodtools.util;

import ch.atdit.warzonemodtools.WMT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class Common {
    public static Logger logger;
    private static final DecimalFormat twoDecimalFormat = new DecimalFormat("#0.00");
    private static final DecimalFormat threeDecimalFormat = new DecimalFormat("#0.000");

    public static void info(String text) {
        logger.info(text);
    }

    public static void error(String text) {
        logger.error(text);
    }

    public static void warn(String text) {
        logger.warn(text);
    }

    public static void debug(String text) {
        logger.debug(text);
        if (WMT.debugMode) addCCM(new ChatComponentText(Color.GREEN + "[WMT] " + Color.RESET + text));
    }

    public static void addMessageGreen(String message) {
        addCCM(new ChatComponentText(Color.GREEN + "[WMT] " + Color.RESET + message));
    }

    public static void addMessageRed(String message) {
        addCCM(new ChatComponentText(Color.RED + "[WMT] " + Color.RESET + message));
    }

    public static void addCCM(ChatComponentText text) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(text);
    }
    public static void addMessage(String message) {
        addCCM(new ChatComponentText(message));
    }
    public static void sendMessage(String message) {
        if (message.length() == 0) {
            debug("sendMessage() input size is 0");
            return;
        }
        Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
    }

    public static String twoDecimals(double number) {
        return twoDecimalFormat.format(number);
    }

    public static String threeDecimals(double number) {
        return threeDecimalFormat.format(number);
    }

    public static EntityPlayerSP p() {
        return Minecraft.getMinecraft().thePlayer;
    }

    public static String c(Color color) {
        if (color == null) return "[InvalidColor!]";
        return "\u00A7" + color.formattingCode;
    }

    public static String escapeColors(String text) {
        while (text.contains("\u00A7")) {
            int pos = text.indexOf("\u00A7");
            while (pos + 2 <= text.length() && text.charAt(pos + 1) == '\u00A7') {
                pos++;
            }
            int end = pos + 2;
            if (pos == text.length() - 1) end--;

            text = text.substring(0, pos) + text.substring(end);
        }

        return text;
    }

    public static int randomInt(int[] array) {
        return array[randomInt(0, array.length - 1)];
    }

    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static void delay(long millis) {
        long before = System.currentTimeMillis();
        while (System.currentTimeMillis() - before < millis) {
            try {
                //noinspection BusyWait
                Thread.sleep(50);
            } catch (Exception ignored) {}
        }
    }

    public static void shutdown(String shutdownMessage) {
        info(shutdownMessage);
        shutdown();
    }

    public static void shutdown() {
        Minecraft.getMinecraft().shutdown();
    }
}
