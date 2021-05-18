package ch.atdit.warzonemodtools.modules.moderating;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.HashMap;

import static ch.atdit.warzonemodtools.util.Common.*;

public class Help extends CommandBase {
    public static Gamemode gamemode = Gamemode.OTHER;

    private static final HashMap<String, String> help = new HashMap<>();

    public Help() {
        help.clear();
        help.put("dtm", "DTM: Each team has a monument (wool or obsidian). Break the enemy's one before they break yours.");
        help.put("ctw", "CTW: Capture the enemy's wools and bring it back to your spawn.");
        help.put("ctf", "CTF: Each team has a banner acting as a flag. Capture and bring the flag back to your base to win.");
        help.put("kotf", "KOTF: Hold the flag to gain points. Win by having the most points when the timer ends.");
        help.put("koth", "KOTH: Your team has to capture the hills to get points. The team with most points wins.");
        help.put("infected", "INFECTED: If you get infected, kill the humans, otherwise run and hide.");
        help.put("tdm", "TDM: The team which gets the most kills wins.");
        help.put("blitz", "BLITZ: You have a certain amount of lives, kill the other teams before they kill you.");
        help.put("ffa", "FFA: Free for all minigame. Get the most kills in the least amount of time to win the match.");
    }

    @Override
    public String getCommandName() {
        return "h";
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
        String gamemodeString;

        if (args.length == 0) {
            if (gamemode == Gamemode.OTHER) return;
            gamemodeString = gamemode.name().toLowerCase();
        } else {
            gamemodeString = args[0].toLowerCase();
        }

        if (!help.containsKey(gamemodeString)) return;

        String explanation = help.get(gamemodeString);

        sendMessage(explanation);
    }
}
