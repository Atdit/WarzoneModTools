package ch.atdit.warzonemodtools;

import ch.atdit.warzonemodtools.modules.moderating.SuspectSpectator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;

import static ch.atdit.warzonemodtools.util.Common.*;

public class Autoforce extends CommandBase {

    @Override
    public String getCommandName() {
        return "af";
    }

    @Override
    public String getCommandUsage(ICommandSender ics) {
        return "<player|set> [delay]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender ics, String[] args) throws CommandException {
        if (!(ics instanceof EntityPlayer)) return;

        if (args.length == 0 && SuspectSpectator.isRunning) {
            args = new String[]{SuspectSpectator.suspect};
        }

        if (args.length == 0 || args.length > 2) return;

        if (args.length == 2 && args[0].equals("team")) {
            WMT.team = args[1];
        }

        if (args[0].equals("clear") || args[0].equals("c")) {
            WMT.toForce = new ArrayList<>();
            WMT.inactiveToForce = new ArrayList<>();
            return;
        }

        if (args[0].equals("disable") || args[0].equals("d")) {
            WMT.toForceEnabled = false;
            return;
        }

        if (args[0].equals("enable") || args[0].equals("e")) {
            WMT.toForceEnabled = true;
            return;
        }

        ArrayList<String> toForce = WMT.toForce;

        if (toForce.contains(args[0].toLowerCase())) {
            toForce.remove(args[0].toLowerCase());
        } else {
            toForce.add(args[0].toLowerCase());
            sendMessage("/team force " + args[0] + " " + WMT.team);
        }

        WMT.toForce = toForce;
    }
}
