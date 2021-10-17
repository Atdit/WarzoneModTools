package ch.atdit.warzonemodtools;

import ch.atdit.warzonemodtools.modules.moderating.AutoTP;
import ch.atdit.warzonemodtools.modules.moderating.SuspectSpectator;
import ch.atdit.warzonemodtools.modules.moderating.AltsChecker;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class ChatHandler {
    public static long latestGameEnd = System.currentTimeMillis();

    private static boolean wasKilled(List<String> words) {
        for (String midMessage : AutoTP.midMessages) {
            try {
                if (String.join(" ", words.subList(1, midMessage.split(" ").length + 1)).equals(midMessage)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        List<String> words = Arrays.asList(message.split(" "));

        int size = words.size();

        if (size == 0) {
            return;
        }

        try {
            if (!WMT.isOnline()) return;
        } catch (Exception e) {
            return;
        }

        List<String> endMessages = new ArrayList<>();
        endMessages.add("  Play next game?"); // Spectator
        endMessages.add("  Congratulations!"); // Win
        endMessages.add("  Better luck next time!"); // Tie, Lose

        // SuspectSpectator
        if (endMessages.contains(message)) {
            SuspectSpectator.previousTeam = null;
            latestGameEnd = System.currentTimeMillis();
        }

        // AltsChecker
        if (words.size() == 3 && words.get(1).equals("known") && words.get(2).equals("alts:")) {
            String checkedPlayer = words.get(0).split("'")[0];
            if (AltsChecker.queue.size() > 0) {
                AltsChecker.altChecked(checkedPlayer);
                SuspectSpectator.checkedAlts.add(checkedPlayer);
            }

        }

        // AltsChecker
        if (words.size() == 5 && String.join(" ", words.subList(1, words.size())).equals("has no known alts.")) {
            String checkedPlayer = words.get(0).split("'")[0];
            if (AltsChecker.queue.size() > 0) {
                AltsChecker.altChecked(checkedPlayer);
                SuspectSpectator.checkedAlts.add(checkedPlayer);
            }
        }

        // SuspectSpectator
        if (words.size() >= 3 && String.join(" ", words.subList(0, 1)).equalsIgnoreCase("You joined") && !words.get(2).equalsIgnoreCase("spectators")) {
            SuspectSpectator.previousTeam = words.get(2); // ToDo Check if this actually does anything
        }

        // SuspectSpectator
        if (AutoTP.currentSuspect != null && WMT.autotp) {
            if (words.get(0).equals(AutoTP.currentSuspect)) { // If killed player is suspect
                // If mid message matches
                if (!SuspectSpectator.dead) {
                    SuspectSpectator.dead = wasKilled(words);
                }
            }
        }

        if (message.contains(":")) return;

        /* NO MORE PLAYER MESSAGES */
        /* NO MORE PLAYER MESSAGES */

        if (WMT.autoforce || WMT.suspectspectator) { // Format: ${RANK} ${PLAYER} joined.
            if (!message.endsWith(".")) return;

            if (words.get(size - 1).equals("joined.")) {
                String player = words.get(size - 2);

                if (WMT.autoforce) {
                    if (WMT.inactiveToForce.contains(player.toLowerCase())) {
                        ArrayList<String> toForce = WMT.toForce;
                        toForce.add(player.toLowerCase());
                        WMT.inactiveToForce.remove(player);
                        WMT.toForce = toForce;
                    }
                }
            }

            if (words.get(size - 1).equals("left.")) {
                String player = words.get(size - 2);

                if (WMT.suspectspectator) {
                    if (SuspectSpectator.suspect != null && SuspectSpectator.suspect.equals(player)) {
                        SuspectSpectator.stopSuspectSpectator();
                    }
                }

                if (WMT.autoforce) {
                    ArrayList<String> toForce = WMT.toForce;
                    if (toForce.contains(player.toLowerCase())) {
                        ArrayList<String> inactiveToForce = WMT.inactiveToForce;
                        inactiveToForce.add(player);
                        toForce.remove(player.toLowerCase());
                        WMT.inactiveToForce = inactiveToForce;
                        WMT.toForce = toForce;
                    }
                }
            }
        }
    }
}
