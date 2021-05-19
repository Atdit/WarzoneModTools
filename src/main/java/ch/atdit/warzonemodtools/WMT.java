package ch.atdit.warzonemodtools;

import ch.atdit.warzonemodtools.commands.FakeMatrixNotifications;
import ch.atdit.warzonemodtools.modules.moderating.*;
import ch.atdit.warzonemodtools.util.Common;
import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.ICommand;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ch.atdit.warzonemodtools.modules.moderating.Quickban.Type;
import static ch.atdit.warzonemodtools.util.Common.*;

@Mod(modid = WMT.MODID, version = WMT.VERSION, acceptedMinecraftVersions = "[1.8, 1.8.9]")
public class WMT {
    /* General Mod Variables */
    public static final String MODID = "wzmt";
    public static final String VERSION = "1.0.1";
    public static final String configFolder = "config/warzonemodtools";
    private static final long buildVersion = 1;

    private static WMT instance;
    public static WMT instance() {
        return instance;
    }

    public static boolean debugMode = false;

    // ToDo: Module inheritance

    /* Module booleans */
    public static boolean autoforce = true;
    public static boolean autotp = true;
    public static boolean toForceEnabled = true;
    public static boolean quickban = true;
    public static boolean suspectspectator = true;

    /* AutoForce */
    static ArrayList<String> toForce = new ArrayList<>();
    static ArrayList<String> inactiveToForce = new ArrayList<>();
    static int delay = 3;
    static String team = "s";

    public static List<String> warzoneIPs = new ArrayList<>();

    private static final File configFolderFile = new File(configFolder);
    private static final File configFile = new File(configFolder + "/config.json");

    public static String readString(File file) {
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeString(File file, String string) {
        try {
            FileUtils.writeStringToFile(file, string, "UTF-8");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONObject readJson(File file) {
        String jsonString = readString(file);
        if (jsonString == null) return null;
        return new JSONObject(jsonString);
    }

    public static boolean writeJson(File file, JSONObject jsonObject) {
        return writeString(file, jsonObject.toString(4));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOnline() throws Exception {
        String ip;

        ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;

        return WMT.warzoneIPs.contains(ip) || ip.endsWith("warz.one") || ip.endsWith("atdit.de");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Common.logger = LogManager.getLogger(MODID);

        info("Initializing Warzone Mod Tools...");

        instance = this;
        MinecraftForge.EVENT_BUS.register(this);

        warzoneIPs.add("play.warz.one"); // Warzone
        warzoneIPs.add("play.warzone.network"); // Warzone
        warzoneIPs.add("warzone.minehut.gg"); // Warzone
        warzoneIPs.add("minehut.com"); // Warzone
        warzoneIPs.add("jala.atdit.de"); // For Testing
        warzoneIPs.add("tgm.atdit.de"); // For Testing
        warzoneIPs.add("atdit.de"); // For Testing

        AutoTP.midMessages = new ArrayList<>(); // TODO: Add fall damage kill message
        AutoTP.midMessages.add("was thrown into the void by");
        AutoTP.midMessages.add("was shot into the void by");
        AutoTP.midMessages.add("was shot off a high place by");
        AutoTP.midMessages.add("was thrown off a high place by");
        AutoTP.midMessages.add("was shot by");
        AutoTP.midMessages.add("was killed by");
        AutoTP.midMessages.add("fell into the void");
        AutoTP.midMessages.add("died");

        if (!configFolderFile.exists()) {
            if (!configFolderFile.mkdir()) {
                shutdown("Shutting down Minecraft due to error while trying to create the config folder.");
            }
        }

        if (!configFile.exists()) {
            try {
                URL url = new URL("https://atdit.de:25/dl/warzonemodtools/config/" + buildVersion);
                FileUtils.copyURLToFile(url, configFile);
            } catch (Exception e) {
                e.printStackTrace();
                shutdown("Caught exception while trying to format URL or copying URL to file (build version = " + buildVersion + "), shutting down");
            }
        }

        JSONObject configObject = readJson(configFile);

        if (configObject == null) {
            shutdown("Config object is null!");
        }

        assert configObject != null;

        if (configObject.optInt("buildVersion", -1) != buildVersion) {
            shutdown("Shutting down due to config build version (" + configObject.getString("buildVersion") +
                    ") not matching mod build version (" + buildVersion + ")");
        }

        autoforce = configObject.getBoolean("autoforce");
        quickban = configObject.getBoolean("quickban");

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            try {
                if (!isOnline()) return;

                Gamemode gamemode;

                Ordering<NetworkPlayerInfo> players = null;
                IChatComponent header = null;
                IChatComponent footer = null;

                try {
                    //noinspection unchecked
                    players = (Ordering<NetworkPlayerInfo>) ReflectionHelper.findField(GuiPlayerTabOverlay.class, "field_175252_a").get(Minecraft.getMinecraft().ingameGUI.getTabList());
                } catch (Exception ignored) {}

                try {
                    header = (IChatComponent) ReflectionHelper.findField(GuiPlayerTabOverlay.class, "header", "field_175256_i").get(Minecraft.getMinecraft().ingameGUI.getTabList());
                } catch (Exception ignored) {}

                try {
                    footer = (IChatComponent) ReflectionHelper.findField(GuiPlayerTabOverlay.class, "footer", "field_175255_h").get(Minecraft.getMinecraft().ingameGUI.getTabList());
                } catch (Exception ignored) {}

                if (header == null) return;

                String[] splitHeader = header.getUnformattedText().split(" - ");
                String gamemodeString = splitHeader[0];
                String currentTimer = splitHeader[1];
                if (!currentTimer.contains(":")) currentTimer = "00:00";

                if (gamemodeString == null) {
                    gamemode = Gamemode.OTHER;
                } else if (gamemodeString.contains("DTM")) {
                    gamemode = Gamemode.DTM;
                } else if (gamemodeString.contains("CTW")) {
                    gamemode = Gamemode.CTW;
                } else if (gamemodeString.contains("FFA")) {
                    gamemode = Gamemode.FFA;
                } else if (gamemodeString.contains("CTF")) {
                    gamemode = Gamemode.CTF;
                } else if (gamemodeString.contains("KOTF")) {
                    gamemode = Gamemode.KOTF;
                } else if (gamemodeString.contains("KOTH")) {
                    gamemode = Gamemode.KOTH;
                } else if (gamemodeString.contains("Blitz")) {
                    gamemode = Gamemode.BLITZ;
                } else if (gamemodeString.contains("Infected")) {
                    gamemode = Gamemode.INFECTED;
                } else if (gamemodeString.contains("TDM")) {
                    gamemode = Gamemode.TDM;
                } else {
                    gamemode = Gamemode.OTHER;
                }

                Help.gamemode = gamemode;
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.SECONDS);

        try {
            registerCommand(new Autoforce());
            registerCommand(new Quickban());
            registerCommand(new SuspectSpectator());
            registerCommand(new ChatControl());
            registerCommand(new Toggle());
            registerCommand(new AltsChecker());
            registerCommand(new Help());

            registerCommand(new FakeMatrixNotifications());
        } catch (Exception e) {
            e.printStackTrace();
            shutdown("Shutting down due to fatal exception while trying to register commands.");
        }

        /* START MODULES */

        if (autoforce) {
            team = configObject.getString("autoforce_team");
            delay = configObject.getInt("autoforce_delay");

            ScheduledExecutorService autoforceExec = Executors.newSingleThreadScheduledExecutor();
            autoforceExec.scheduleAtFixedRate(() -> {
                if (toForceEnabled) {
                    for (String player : toForce) {
                        sendMessage("/team force " + player + " " + team);
                    }
                }
            }, 0, delay, TimeUnit.SECONDS);
        }

        if (quickban) {
            Quickban.length = configObject.getString("quickban_length");
            Quickban.orSimilar = configObject.getString("quickban_or_similar");
            Quickban.excessiveViolations = configObject.getString("quickban_excessive_violations");

            JSONObject jsonAbbreviations = configObject.getJSONObject("quickban_abbreviations");

            for (Iterator<String> iterator = jsonAbbreviations.keySet().iterator(); iterator.hasNext(); ) {
                Type type = Type.valueOf(iterator.next());
                JSONObject jsonReasons = jsonAbbreviations.getJSONObject(type.toString());

                for (Iterator<String> innerIterator = jsonReasons.keySet().iterator(); innerIterator.hasNext(); ) {
                    String abbreviation = innerIterator.next();
                    String reason = jsonReasons.getString(abbreviation);

                    Quickban.reasons.put(abbreviation, new Ban(type, reason));
                }
            }

            JSONObject jsonPrefixes = configObject.getJSONObject("quickban_prefixes");

            for (Iterator<String> iterator = jsonPrefixes.keySet().iterator(); iterator.hasNext(); ) {
                String type = iterator.next();
                String prefix = jsonPrefixes.getString(type);

                Quickban.prefixes.put(Type.valueOf(type), prefix);
            }
        }
    }

    @EventHandler
    public void postInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChatHandler());
        MinecraftForge.EVENT_BUS.register(new SuspectSpectator());
    }

    private void registerCommand(ICommand iCommand) {
        ClientCommandHandler.instance.registerCommand(iCommand);
    }
}
