package com.backyardmc.punishgui.commands;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.network.Network;
import com.backyardmc.punishgui.util.Punishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CmdLookup implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bypunish.lookup")) {
                if (args.length == 0) {
                    Util.sendMessage("&cIncorrect Syntax! Usage: /&7plookup &c[&7player name&c]", player);
                    return false;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                String targetUUID = target.getUniqueId().toString();

                Network network = BYPunishment.getNetwork();
                network.activateConnection();
                int totalPageNum = 0;

                String getPunishmentCount = "select count(*) from byp_userdata where uuid = ? and is_active = 1";
                try {
                    PreparedStatement ps1 = network.getConnection().prepareStatement(getPunishmentCount);
                    ps1.setString(1, targetUUID);
                    ResultSet resultSet = ps1.executeQuery();

                    while (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        totalPageNum = (int) Math.ceil(count / 5.0);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Util.sendMessage("&7Active Punishment Information for &b" + target.getName(), player);

                PreparedStatement ps;
                try {
                    if (args.length == 1) {
                        ps = network.getConnection().prepareStatement("select * from byp_userdata where uuid = ? and is_active = 1 order by id DESC LIMIT 5");
                    } else {
                        try {
                            int page = Integer.parseInt(args[1]);
                            if (page == 1) {
                                Util.sendMessage("&7------ Page &b" + page + "&7 of &b" + totalPageNum + "&7 ------", player);
                                ps = network.getConnection().prepareStatement("select * from byp_userdata where uuid = ? and is_active = 1 order by id DESC LIMIT 5");
                            } else {
                                if (page > totalPageNum) {
                                    Util.sendMessage("&cError: No results found for page number &7" + page, player);
                                    network.closeConnection();
                                    return false;
                                }
                                if (page <= 0) {
                                    Util.sendMessage("&cError: Page number &7" + page + "&c is invalid", player);
                                }
                                Util.sendMessage("&7------ Page &b" + page + "&7 of &b" + totalPageNum + "&7 ------", player);

                                int offset = (page - 1) * 5;
                                ps = network.getConnection().prepareStatement("select * from byp_userdata where uuid = ? and is_active = 1 order by id DESC LIMIT " + offset + ",5");
                            }
                        } catch (NumberFormatException e) {
                            Util.sendMessage("&cError: &7" + args[1] + "&c is not a valid page number", player);
                            network.closeConnection();
                            return false;
                        }
                    }
                    ps.setString(1, targetUUID);
                    ResultSet res = ps.executeQuery();
                    boolean hasNext = false;
                    boolean isFirst = true;
                    while (res.next()) {
                        hasNext = true;
                        if (args.length == 1 && isFirst) {
                            isFirst = false;
                            Util.sendMessage("&7------ Page &b1&7 of &b" + totalPageNum + "&7 ------", player);
                        }
                        Punishment punishment = Util.getPunishmentFromID(res.getInt("punishment_id"));
                        int warningLevel = res.getInt("warning_level");
                        int pid = res.getInt("punishment_id");
                        if (res.getString("end_date") == null) {
                            assert punishment != null;
                            Util.sendMessage("&7Punishment ID: &b" + pid + "&7(&b" + punishment.getName() + "&7) action taken: &b" + punishment.getPunishmentType(warningLevel), player);
                        } else {
                            long date = Long.parseLong(res.getString("end_date"));
                            long millisAgo = Math.abs(date - System.currentTimeMillis());

                            long daysAgo = Util.getDaysInMillis(millisAgo);
                            long hoursAgo = Util.getHoursInMillis(millisAgo) % 24;
                            long minutesAgo = Util.getMinutesInMillis(millisAgo) % 60;
                            long secondsSago = Util.getSecondsInMillis(millisAgo) % 60;

                            String timeStamp = "&b" + daysAgo + "&7d" + "&b" + hoursAgo + "&7h" + "&b" + minutesAgo + "&7m" + "&b" + secondsSago + "&7s";
                            assert punishment != null;
                            Util.sendMessage("&7Punishment ID: &b" + pid + "&7(&b" + punishment.getName() + "&7) action taken: &b" + punishment.getPunishmentType(warningLevel) + "&7, time remaining: &b" + timeStamp, player);
                        }
                    }
                    network.closeConnection();
                    if (!hasNext) {
                        Util.sendMessage("&cError: There was no results for that user. The page number is either too large or they have not been punished yet.", player);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Util.sendMessage("&cError: You do not have permission to perform this command.", player);
            }
        }
        return false;
    }
}
