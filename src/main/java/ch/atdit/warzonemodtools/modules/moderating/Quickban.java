package ch.atdit.warzonemodtools.modules.moderating;

import ch.atdit.warzonemodtools.WMT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

import static ch.atdit.warzonemodtools.util.Common.*;

public class Quickban extends CommandBase {

    public enum Type {
        HACKING,
        OTHER
    }

    public static boolean enabled = true;
    public static HashMap<Type, String> prefixes = new HashMap<>();
    public static HashMap<String, Ban> reasons = new HashMap<>();
    public static String length;
    public static String orSimilar;
    public static String excessiveViolations;

    static ArrayList<String> sentMessages = new ArrayList<>();
    static String ignoreDoubleMessage = null;

    public static boolean forceToSpectators = false;

    @Override
    public String getCommandName() {
        return "qb";
    }

    @Override
    public String getCommandUsage(ICommandSender ics) {
        return "<player|reason(s)> [reason(s)]";
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { // TODO FIX THIS LOL
        List<String> players = new ArrayList<>();

        addMessage("[DEBUG]: addTabCompletionOptions() called with args: <" + String.join(", ", args) + ">");

        Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : networkPlayerInfos) {
            players.add(playerInfo.getGameProfile().getName());
        }

        return players;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender ics, String[] args) throws CommandException {
        if (!(ics instanceof EntityPlayer)) return;

        if (args.length == 0) return;

        int i = 0;
        String toBan;

        List<String> players = new ArrayList<>();

        Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : networkPlayerInfos) {
            players.add(playerInfo.getGameProfile().getName());
        }

        if (SuspectSpectator.isRunning && !players.contains(args[0])) { // SuspectSpectator is running and first argument is NOT a player
            toBan = SuspectSpectator.suspect;
        } else { // Proceed with normal Quickban
            toBan = args[0];
            i++;

            String argsString = String.join(" ", args); // TODO: Move this piece of code and add name to string

            if (sentMessages.contains(argsString)) {
                if (ignoreDoubleMessage == null) {
                    addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[Quickban] " + EnumChatFormatting.RESET + "Command was already sent! Retry to confirm."));
                    ignoreDoubleMessage = argsString;
                    return;
                } else if (ignoreDoubleMessage.equals(argsString)) {
                    addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[Quickban] " + EnumChatFormatting.RESET + "Processing already sent command anyway!"));
                    ignoreDoubleMessage = null;
                }
            } else {
                sentMessages.add(argsString);
            }
        }

        StringBuilder command = new StringBuilder("/ban "); // TODO Option to modify prefix (/ban in this case)
        command.append(toBan);
        command.append(" ");
        command.append(length);
        command.append(" ");

        List<String> split = Arrays.asList(args);

        int size = split.size();
        int subtrahend = 1;
        boolean os = false;
        boolean silent = false;
        boolean ev = false;
        Ban lastBan = null;

        boolean willReturn = false;
        ArrayList<String> notFound = new ArrayList<>();

        ArrayList<String> banReasons = new ArrayList<>();

        for (; i < size; i++) {
            String abbreviation = split.get(i).toLowerCase();
            if (reasons.containsKey(abbreviation) || (abbreviation.equals("os") || abbreviation.equals("-s")) || abbreviation.equals("ev")) {
                switch (split.get(i)) {
                    case "os":
                        subtrahend++;
                        os = true;
                        break;
                    case "-s":
                        subtrahend++;
                        silent = true;
                        break;
                    case "ev":
                        subtrahend++;
                        ev = true;
                        break;
                    default:
                        Ban ban = reasons.get(abbreviation);

                        if (lastBan == null) {
                            lastBan = ban;

                            command.append(prefixes.get(ban.getType()));
                        } else {
                            if (lastBan.getType() != ban.getType()) { // Reasons with different lengths/prefixes, return
                                addCCM(new ChatComponentText(EnumChatFormatting.RED + "The following reason types don't match: " +
                                        EnumChatFormatting.GRAY + lastBan.getReason() + EnumChatFormatting.GRAY + " (" +
                                        EnumChatFormatting.GRAY + lastBan.getType().toString() + EnumChatFormatting.GRAY + ") and " +
                                        EnumChatFormatting.GRAY + ban.getReason() + EnumChatFormatting.GRAY + " (" +
                                        EnumChatFormatting.GRAY + ban.getType().toString() + EnumChatFormatting.GRAY + ")"));
                                return;
                            }
                        }

                        banReasons.add(ban.getReason());
                }
            } else {
                willReturn = true;
                notFound.add(abbreviation);
            }
        }

        command.append(String.join(", ", banReasons));

        if (willReturn) {
            addCCM(new ChatComponentText(EnumChatFormatting.RED + "[Quickban] " + EnumChatFormatting.RESET + "The following abbreviations were not found: " + String.join(", ", notFound)));
            return;
        }

        if (os) {
            command.append(" ");
            command.append(orSimilar);
        }

        if (ev) {
            command.append(" ");
            command.append(excessiveViolations);
        }

        if (silent) {
            command.append(" -s");
        }

        if (WMT.debugMode) {
            addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[Quickban] " + EnumChatFormatting.RESET + "Processed ban."));
        }

        if (forceToSpectators) sendMessage("/team force " + toBan + " spectators");

        sendMessage(command.toString()); // Send ban command

        if (SuspectSpectator.isRunning) {
            SuspectSpectator.stopSuspectSpectator();
        }
    }
}
