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
import java.util.UUID;

public class CmdHistory implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bypunish.history")) {
                if (args.length == 0) {
                    Util.sendMessage("&cIncorrect Syntax! Usage: /&7staffhistory &c[&7player name&c] &7{&cpage number&7}", player);
                    return false;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                Util.sendMessage("&7Punishment History for &b" + target.getName(), player);
                String staffUUID = target.getUniqueId().toString();

                Network network = BYPunishment.getNetwork();
                network.activateConnection();
                int totalPageNum = 0;

                String getPunishmentCount = "select count(*) from byp_staffdata where staff_uuid = ?";
                try {
                    PreparedStatement ps1 = network.getConnection().prepareStatement(getPunishmentCount);
                    ps1.setString(1, staffUUID);
                    ResultSet resultSet = ps1.executeQuery();

                    while (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        totalPageNum = (int) Math.ceil(count / 5.0);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    PreparedStatement ps;
                    if (args.length == 1) {
                        ps = network.getConnection().prepareStatement("select * from byp_staffdata where staff_uuid = ? order by id DESC LIMIT 5");
                    } else {
                        try {
                            int page = Integer.parseInt(args[1]);
                            if (page == 1) {
                                Util.sendMessage("&7------ Page &b" + page + "&7 of &b" + totalPageNum + "&7 ------", player);
                                ps = network.getConnection().prepareStatement("select * from byp_staffdata where staff_uuid = ? order by id DESC LIMIT 5");
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

                                ps = network.getConnection().prepareStatement("select * from byp_staffdata where staff_uuid = ? order by id DESC LIMIT " + offset + ",5");
                            }
                        } catch (NumberFormatException e) {
                            Util.sendMessage("&cError: &7" + args[1] + "&c is not a valid page number", player);
                            network.closeConnection();
                            return false;
                        }
                    }
                    ps.setString(1, staffUUID);
                    ResultSet res = ps.executeQuery();
                    boolean hasNext = false;
                    boolean isFirst = true;
                    while (res.next()) {
                        if (args.length == 1 && isFirst) {
                            isFirst = false;
                            Util.sendMessage("&7------ Page &b1" + "&7 of &b" + totalPageNum + "&7 ------", player);
                        }
                        hasNext = true;
                        String punishedUUID = res.getString("punished_uuid");
                        long date = Long.parseLong(res.getString("date"));
                        long millisAgo = Math.abs(date - System.currentTimeMillis());

                        long daysAgo = Util.getDaysInMillis(millisAgo);
                        long hoursAgo = Util.getHoursInMillis(millisAgo) % 24;
                        long minutesAgo = Util.getMinutesInMillis(millisAgo) % 60;
                        long secondsSago = Util.getSecondsInMillis(millisAgo) % 60;

                        OfflinePlayer punished = Bukkit.getOfflinePlayer(UUID.fromString(punishedUUID));

                        String timeStamp = "&7" + daysAgo + "&bd" + "&7" + hoursAgo + "&bh" + "&7" + minutesAgo + "&bm" + "&7" + secondsSago + "&bs";

                        Punishment punishment = Util.getPunishmentFromID(res.getInt("punishment_id"));

                        assert punishment != null;
                        Util.sendMessage("&7" + timeStamp + " ago: &b" + target.getName() + "&7 punished &b" + punished.getName() + "&7 for &b" + punishment.getName(), player);
                    }
                    network.closeConnection();
                    if (!hasNext) {
                        Util.sendMessage("&cError: There was no results for that user. The page number is either too large or they have not punished anyone yet.", player);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    network.closeConnection();
                }
                network.closeConnection();
            } else {
                Util.sendMessage("&cError: You do not have permission to perform this command.", player);
            }
        }
        return false;
    }
}
