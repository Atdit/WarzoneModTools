package ch.atdit.warzonemodtools;

import ch.atdit.warzonemodtools.modules.moderating.AutoTP;
import ch.atdit.warzonemodtools.modules.moderating.SuspectSpectator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import ch.atdit.warzonemodtools.util.Color;

import java.util.Arrays;
import java.util.List;

import static ch.atdit.warzonemodtools.util.Common.*;

public class Toggle extends CommandBase {
    @Override
    public String getCommandName() {
        return "tg";
    }

    @Override
    public String getCommandUsage(ICommandSender ics) {
        return "<feature> <identifier> <value> <value2> <etc>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender ics, String[] args) throws CommandException {
        if (!(ics instanceof EntityPlayer)) return;

        String setting = args.length >= 1 ? args[0] : "";
        String identifier = args.length >= 2 ? args[1] : "";
        String value = args.length >= 3 ? args[2] : "";

        List<String> arguments = Arrays.asList(args);

        if (args.length == 0) {
            addMessage("Settings: <atp|d|ss>");
        } else if (args.length == 1) {
            switch (setting) {
                case "atp": // AutoTP
                    addMessage("Identifiers: <mode|s>");
                    break;
                case "d": // Debug
                    if (WMT.debugMode) {
                        WMT.debugMode = false;
                        addMessageGreen("Disabled debug mode");
                    } else {
                        WMT.debugMode = true;
                        addMessageGreen("Enabled debug mode");
                    }
                    break;
                case "ss": // SuspectSpectator
                    addMessage("Identifiers: <mode|d|s|y|p>");
                    break;
            }
        } else if (args.length == 2) {
            switch (setting) {
                case "atp":
                    switch (identifier) {
                        case "mode":
                            addCCM(new ChatComponentText(Color.GREEN + "AutoTP settings: " + Color.RESET + "<on|off>"));
                            break;
                        case "s":
                            addCCM(new ChatComponentText(Color.GREEN + "AutoTP settings: " + Color.RESET + "<IGN|off>"));
                            break;
                    }
                    break;
                case "ss":
                    switch (identifier) {
                        case "mode":
                            addCCM(new ChatComponentText(Color.GREEN + "SuspectSpectator mode settings: " + Color.RESET + "<on|off>"));
                            break;
                        case "d":
                            addCCM(new ChatComponentText(Color.GREEN + "SuspectSpectator distance settings: " + Color.RESET + "<float>"));
                            break;
                        case "s":
                            addCCM(new ChatComponentText(Color.GREEN + "SuspectSpectator suspect settings: " + Color.RESET + "<IGN|off>"));
                            break;
                        case "y":
                            addCCM(new ChatComponentText(Color.GREEN + "SuspectSpectator yaw settings: " + Color.RESET + "<on|off>"));
                            break;
                        case "p":
                            addCCM(new ChatComponentText(Color.GREEN + "SuspectSpectator pitch settings: " + Color.RESET + "<on|off>"));
                            break;
                        case "tp":
                            if (AutoTP.currentSuspect != null) {
                                addCCM(new ChatComponentText(Color.GREEN + "Teleporting..."));
                                AutoTP.tp(AutoTP.currentSuspect);
                            } else {
                                addCCM(new ChatComponentText(Color.RED + "No current suspect."));
                            }
                            break;
                    }
                    break;
            }
        } else {
            switch (setting) {
                case "atp":
                    switch (identifier) {
                        case "mode":
                            if (value.equals("on")) {
                                WMT.autotp = true;
                                addCCM(new ChatComponentText(Color.GREEN + "Enabled " + Color.RESET + "AutoTP"));
                            } else if (value.equals("off")) {
                                WMT.autotp = false;
                                addCCM(new ChatComponentText(Color.RED + "Disabled " + Color.RESET + "AutoTP"));
                            } else {
                                addCCM(new ChatComponentText(Color.RED + "Invalid value: " + Color.RESET + value));
                            }
                            break;
                        case "s":
                            if (value.equals("off")) {
                                AutoTP.currentSuspect = null;
                                addCCM(new ChatComponentText(Color.GREEN + "Removed suspect"));
                            } else {
                                AutoTP.currentSuspect = value;
                                addCCM(new ChatComponentText(Color.GREEN + "Set suspect to: " + value));
                            }
                            break;
                    }
                    break;
                case "ss":
                    switch (identifier) {
                        case "mode":
                            if (value.equals("on")) {
                                WMT.suspectspectator = true;
                                addCCM(new ChatComponentText(Color.GREEN + "Enabled " + Color.RESET + "SuspectSpectator"));
                            } else if (value.equals("off")) {
                                WMT.suspectspectator = false;
                                addCCM(new ChatComponentText(Color.RED + "Disabled " + Color.RESET + "SuspectSpectator"));
                            } else {
                                addCCM(new ChatComponentText(Color.RED + "Invalid value: " + Color.RESET + value));
                            }
                            break;
                        case "d":
                            float distance;

                            try {
                                distance = Float.parseFloat(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                                addCCM(new ChatComponentText(Color.RED + "Not a float: " + Color.RESET + value));
                                return;
                            }

                            SuspectSpectator.aimbotTriggerDistance = distance;
                            addCCM(new ChatComponentText(Color.GREEN + "Set distance to: " + Color.RESET + value));
                            break;
                        case "s":
                            if (value.equals("off")) {
                                SuspectSpectator.suspect = null;
                                addCCM(new ChatComponentText(Color.GREEN + "Removed suspect"));
                            } else {
                                SuspectSpectator.suspect = value;
                                addCCM(new ChatComponentText(Color.GREEN + "Set suspect to: " + value));
                            }
                            break;
                        case "y":
                            if (value.equals("on")) {
                                SuspectSpectator.faceYaw = true;
                                addCCM(new ChatComponentText(Color.GREEN + "Enabled " + Color.RESET + "facing yaw"));
                            } else if (value.equals("off")) {
                                SuspectSpectator.faceYaw = false;
                                addCCM(new ChatComponentText(Color.RED + "Disabled " + Color.RESET + "facing yaw"));
                            } else {
                                addCCM(new ChatComponentText(Color.RED + "Invalid value: " + Color.RESET + value));
                            }
                            break;
                        case "p":
                            if (value.equals("on")) {
                                SuspectSpectator.facePitch = true;
                                addCCM(new ChatComponentText(Color.GREEN + "Enabled " + Color.RESET + "facing pitch"));
                            } else if (value.equals("off")) {
                                SuspectSpectator.facePitch = false;
                                addCCM(new ChatComponentText(Color.RED + "Disabled " + Color.RESET + "facing pitch"));
                            } else {
                                addCCM(new ChatComponentText(Color.RED + "Invalid value: " + Color.RESET + value));
                            }
                            break;
                    }
                    break;
            }
        }
    }
}
