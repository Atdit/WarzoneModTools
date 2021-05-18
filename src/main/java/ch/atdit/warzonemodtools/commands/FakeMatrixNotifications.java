package ch.atdit.warzonemodtools.commands;

import ch.atdit.warzonemodtools.util.Color;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import static ch.atdit.warzonemodtools.util.Common.*;

public class FakeMatrixNotifications extends CommandBase {
    private void sendMessage(String message) {
        p().sendChatMessage(message);
    }

    @Override
    public String getCommandName() {
        return "fmn";
    }

    @Override
    public String getCommandUsage(ICommandSender ics) {
        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender ics, String[] args) throws CommandException {
        if (args.length == 0) {
            addCCM(new ChatComponentText(Color.RED + "[FMN] " + Color.RESET + "No username given."));
            return;
        }

        final String name = args[0];

        boolean randomDelay = args.length >= 2;

        addCCM(new ChatComponentText(Color.GREEN + "[FMN] " + Color.RESET + "Sending out fake matrix notifications..."));
        Thread thread = new Thread(() -> {
            sendMessage("/matrix notify " + name + " may be using combat hacks &6(" + randomInt(new int[]{8, 10}) + "VL)");

            delay(randomDelay ? randomInt(200, 600) : 300);
            sendMessage("/matrix notify " + name + " may be using combat hacks &4(" + randomInt(new int[]{15, 18}) + "VL)");

            if (randomInt(0, 1) == 1) {
                delay(randomDelay ? randomInt(200, 600) : 200);
                sendMessage("/matrix notify " + name + " may be using combat hacks &6(25VL)");
            }

            delay(randomDelay ? randomInt(200, 600) : 400);
            sendMessage("/matrix notify " + name + " would've been kicked for using combat related hacks!");

            if (randomInt(0, 1) == 1) {
                delay(randomDelay ? randomInt(200, 600) : 350);
                sendMessage("/matrix notify " + name + " would've been kicked for using combat related hacks!");
            }
        });
        thread.start();

    }
}
