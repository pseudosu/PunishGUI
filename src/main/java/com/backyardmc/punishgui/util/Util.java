package com.backyardmc.punishgui.util;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.network.Network;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Util {

    public static long convertAndAddTimeMillis(int toAdd, String timeIncrement) {
        long currentTimeMillis = System.currentTimeMillis();
        switch (timeIncrement) {
            case "h":
                long toAddMillisHour = TimeUnit.HOURS.toMillis(toAdd);
                return currentTimeMillis + toAddMillisHour;
            case "m":
                long toAddMillisMinutes = TimeUnit.MINUTES.toMillis(toAdd);
                return currentTimeMillis + toAddMillisMinutes;
            case "d":
                long toAddMillisDays = TimeUnit.DAYS.toMillis(toAdd);
                return currentTimeMillis + toAddMillisDays;
            default:
                return 0;
        }
    }

    public static List<String> getAllPunishmentIDs() {
        List<String> idList = new ArrayList<>();
        for (Punishment punishment : BYPunishment.punishments) {
            idList.add(punishment.getId() + "");
        }
        return idList;
    }

    public static Punishment getPunishmentFromID(int id) {
        for (Punishment punishment : BYPunishment.punishments) {
            if (punishment.getId() == id) {
                return punishment;
            }
        }
        return null;
    }

    public static void sendMessage(String message, Player p) {
        p.sendMessage(colorText(message));
    }

    public static void kickPlayer(Player player, String message) {
        player.kickPlayer(colorText(message));
    }

    public static ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    public static String getRemainingTimeAsString(long timeInMilliSeconds) {
        long seconds = timeInMilliSeconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return days + ":" + hours % 24 + ":" + minutes % 60 + ":" + seconds % 60;
    }

    public static long getSecondsInMillis(long timeInMilliSeconds) {
        return timeInMilliSeconds / 1000;
    }

    public static long getMinutesInMillis(long timeInMilliSeconds) {
        return getSecondsInMillis(timeInMilliSeconds) / 60;
    }

    public static long getHoursInMillis(long timeInMilliSeconds) {
        return getMinutesInMillis(timeInMilliSeconds) / 60;
    }

    public static long getDaysInMillis(long timeInMilliSeconds) {
        return getHoursInMillis(timeInMilliSeconds) / 24;
    }

    public static String[] getOffenseData(Punishment punishment, int warningLevel) {
        return punishment.getPunishmentFromWarningLevel(warningLevel).split(" ");
    }

    /**
     *
     * @param message message to format
     * @param punishment punishment instance
     * @param warning_level warning level
     * @param endDate the end date in milliseconds
     * @return
     */
    public static String formatText(String message, Punishment punishment, int warning_level, long endDate) {
        String fixedMessage = message;
        try {
            fixedMessage = fixedMessage.replaceAll("%TIME%", punishment.getPunishmentTime(warning_level) + "");
            fixedMessage = fixedMessage.replaceAll("%TIME_INCREMENT%", punishment.getPunishmentTimeIncrement(warning_level) + "");
        } catch (NumberFormatException ignored) {

        }
        fixedMessage = fixedMessage.replaceAll("%DAYS%", getDaysInMillis(endDate) + "");
        fixedMessage = fixedMessage.replaceAll("%HOURS%", getHoursInMillis(endDate) % 24 + "");
        fixedMessage = fixedMessage.replaceAll("%MINUTES%", getMinutesInMillis(endDate) % 60 + "");
        fixedMessage = fixedMessage.replaceAll("%SECONDS%", getSecondsInMillis(endDate) % 60 + "");
        fixedMessage = fixedMessage.replaceAll("%REASON%", punishment.getName());

        return colorText(fixedMessage);
    }

    /**
     *
     * @param uuid uuid of player
     * @param player player instance
     * @return true/false
     */
    public static boolean checkMute(String uuid, Player player) {
        Network network = BYPunishment.getNetwork();
        network.activateConnection();

        ResultSet res = network.executeQuery("select * from byp_userdata where uuid = '" + uuid + "' and is_active = 1");

        while (true) {
            try {
                if (!res.next()) break;


                int punishment_id = res.getInt("punishment_id");
                int warningLevel = res.getInt("warning_level");

                Punishment punishment = Util.getPunishmentFromID(punishment_id);
                String punishmentType = Objects.requireNonNull(punishment).getPunishmentType(warningLevel);

                if (punishmentType.equalsIgnoreCase("permamute")) {
                    Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.permamutemessage"), punishment, warningLevel, 0), player);
                    return true;
                }

                if (punishmentType.equalsIgnoreCase("mute")) {
                    long end_date = Long.parseLong(res.getString("end_date"));
                    long remainingTime = end_date - System.currentTimeMillis();
                    if (remainingTime <= 0) {
                        network.executeUpdate("update byp_userdata set is_active = 0 where uuid = '" + uuid + "' and punishment_id = '" + punishment_id + "'");
                        network.closeConnection();
                        return false;
                    }
                    Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.mutedmessage"), punishment, warningLevel, remainingTime), player);
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static void notifyStaff(String message, String serverName, Player staffMember) {
        JDA api = BYPunishment.getApi();
        TextChannel channel = api.getTextChannelById(BYPunishment.getConfigManager().getString("discord.staffchannel"));
//        ServerTextChannel channel = api.getChannelById(BYPunishment.getConfigManager().getString("discord.staffchannel")).get().asServerTextChannel().get();
//
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Staff Action")
                .addField("Server", serverName, true)
                .addField("Staff Member", staffMember.getName(),true)
                .addField("Action Taken", message, false)
                .setColor(Color.ORANGE);
//
//        channel.sendMessage(embed);
        assert channel != null;
        channel.sendMessage(embed.build()).queue();
    }

    public static String colorText(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
