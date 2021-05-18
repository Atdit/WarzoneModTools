package ch.atdit.warzonemodtools.modules.moderating;

import ch.atdit.warzonemodtools.AutoTP;
import ch.atdit.warzonemodtools.WMT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ch.atdit.warzonemodtools.util.Common.*;

public class SuspectSpectator extends CommandBase {
    public static String suspect = null;

    public static boolean isRunning = false;

    public static List<String> checkedAlts = new ArrayList<>();

    public static boolean aimbot = false;

    public static boolean dead = false;
    public static boolean playerIsNull = false;

    public static float aimbotTriggerDistance = 20F;
    public static boolean faceYaw = true;
    public static boolean facePitch = true;

    public static String previousTeam = null;

    private boolean isOnline(String player) {
        Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : networkPlayerInfos) {
            if (playerInfo.getGameProfile().getName().equals(player)) return true;
        }

        return false;
    }

    private String getPlayer(String player) {
        Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();

        for (NetworkPlayerInfo playerInfo : networkPlayerInfos) {

            if (playerInfo.getGameProfile().getName().startsWith(player)) {
                return playerInfo.getGameProfile().getName();
            }
        }

        return null;
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.PlayerTickEvent event) {
        if (WMT.suspectspectator && suspect != null) {
            EntityPlayer player;

            try {
                player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(suspect);
            } catch (Exception ignored) {
                return;
            }

            if (player == null) {
                if (dead) {
                    debug("Player == null, setting playerIsNull to true...");
                    playerIsNull = true;
                }
                return;
            } else {
                if (dead) {
                    if (!player.isSpectator()) {
                        if (WMT.debugMode) addMessage(player.getDisplayNameString() + " isn't spectator anymore, did he respawn?");

                        if (WMT.autotp && p().getDistanceToEntity(player) > 15) {
                            AutoTP.tp(suspect);
                        }
                        dead = false;
                        playerIsNull = false;
                        return;
                    }
                }
            }

            if (aimbot && p().getDistanceToEntity(player) <= aimbotTriggerDistance) {
                faceEntity(player);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void faceEntity(Entity entity) {
        final double diffX = entity.posX - p().posX;
        final double diffZ = entity.posZ - p().posZ;
        double diffY;

        diffY = (entity.posY + entity.posY) / 2.0D - p().posY;

        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        if (faceYaw)
            p().rotationYaw = p().rotationYaw + MathHelper.wrapAngleTo180_float(yaw - p().rotationYaw);
        if (facePitch)
            p().rotationPitch = p().rotationPitch + MathHelper.wrapAngleTo180_float(pitch - p().rotationPitch) + 1.0F;
    }

    @Override
    public String getCommandName() {
        return "ss";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "<player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) return;

        if (args.length >= 1) {
            if (isRunning) {
                stopSuspectSpectator();
            }

            startSuspectSpectator(args);
        } else {
            if (!isRunning) {
                addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[SuspectSpectator] " +
                        EnumChatFormatting.RESET + "Not running."));
            } else {
                stopSuspectSpectator();
            }
        }
    }

    private void startSuspectSpectator(String[] args) {
        String name = args[0];
        boolean aimbot = true;

        if (args.length >= 2) {
            if (args[1].equals("f") || args[1].equals("false")) {
                aimbot = false;
            }
        }

        SuspectSpectator.aimbot = aimbot;

        if (name.equals(suspect)) {
            stopSuspectSpectator();
            return;
        }

        if (!isOnline(name)) {
            String newName = getPlayer(name); // Find player by checking if someone is online with same name start
            if (newName != null) {
                name = newName;
            } else {
                name = getPlayer(name.toLowerCase()); // Find player by checking if someone is online with same name (lower) start // TODO: Test this with alt
                if (name == null) {
                    addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[SuspectSpectator] " + EnumChatFormatting.RESET + "Player not found."));
                    return;
                }
            }
        }

        isRunning = true;
        suspect = name;
        sendMessage("/join spectators");
        sendMessage("/tp " + name);
        sendMessage("/ping " + name);
        if (!checkedAlts.contains(name)) {
            checkedAlts.add(name);
            sendMessage("/alts " + name);
        }
        AutoTP.currentSuspect = name;
        addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[SuspectSpectator] " + EnumChatFormatting.RESET + "Suspect set to: " + name));
    }

    public static void stopSuspectSpectator() {
        isRunning = false;
        suspect = null;
        AutoTP.currentSuspect = null;
        addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[SuspectSpectator] " + EnumChatFormatting.RESET + "Suspect removed."));

        if (previousTeam != null && !previousTeam.equalsIgnoreCase("spectators")) {
            addCCM(new ChatComponentText(EnumChatFormatting.GREEN + "[SuspectSpectator] " + EnumChatFormatting.RESET + "Rejoining " + previousTeam + "..."));
            sendMessage("/join " + previousTeam);
            previousTeam = null;
        }
    }
}
