package ch.atdit.warzonemodtools.modules.moderating;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ch.atdit.warzonemodtools.util.Common.*;

public class AltsChecker extends CommandBase {
    public static List<String> altsChecked = new ArrayList<>();
    public static List<String> queue = new ArrayList<>();

    @Override
    public String getCommandName() {
        return "ac";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "<clear>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1 && args[0].equals("clear")) {
            queue.clear();
            return;
        }

        int size = queue.size();
        if (size > 0) {
            addCCM(new ChatComponentText(EnumChatFormatting.RED + "[AltsChecker] " + EnumChatFormatting.RESET + "Queue not empty! There " + (size == 1 ? "is " : "are ") + queue.size() + " player" + (size == 1 ? "" : "s") + " remaining."));
            return;
        }
        checkOnlinePlayers();
    }

    public static void altChecked(String player) {
        if (!altsChecked.contains(player)) {
            altsChecked.add(player);
        }

        queue.remove(player);

        if (queue.size() > 0) {
            checkPlayer(AltsChecker.queue.get(0));
        }
    }

    public static void checkPlayer(String player) {
        sendMessage("/alts " + player);
    }

    public static void checkOnlinePlayers() {
        queue.clear();

        List<String> onlinePlayers = new ArrayList<>();

        Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (Object playerInfo : networkPlayerInfos) {
            NetworkPlayerInfo networkPlayerInfo = (NetworkPlayerInfo) playerInfo;
            onlinePlayers.add(networkPlayerInfo.getGameProfile().getName());
        }

        for (String player : onlinePlayers) {
            if (!altsChecked.contains(player)) {
                queue.add(player);
            }
        }
        addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[AltsChecker] " + EnumChatFormatting.RESET + "Checking " + queue.size() + (queue.size() == 1 ? " alt..." : " alts...")));

        if (queue.size() > 0) {
            checkPlayer(queue.get(0));
        }
    }
}
