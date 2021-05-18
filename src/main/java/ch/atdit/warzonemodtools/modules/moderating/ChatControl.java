package ch.atdit.warzonemodtools.modules.moderating;

import ch.atdit.warzonemodtools.util.Color;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import static ch.atdit.warzonemodtools.util.Common.*;

public class ChatControl extends CommandBase {
    @Override
    public String getCommandName() {
        return "c";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) return;

        if (args.length == 0) {
            muteChat();
        } else if (args.length == 1) {
            switch (args[0]) {
                case "a":
                    muteChat();
                    clearChat();
                    break;
                case "c":
                    clearChat();
                    break;
                case "m":
                    muteChat();
                    break;
            }
        }
    }

    private void muteChat() {
        sendMessage("/chat mute");
        addCCM(new ChatComponentText(Color.GREEN + "[ChatControl] " + Color.RESET + "Toggled chat mute."));
    }

    private void clearChat() {
        sendMessage("/chat clear");
        addCCM(new ChatComponentText(Color.GREEN + "[ChatControl] " + Color.RESET + "Cleared chat."));
    }
}
