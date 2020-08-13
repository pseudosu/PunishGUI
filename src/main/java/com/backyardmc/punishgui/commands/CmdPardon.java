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

public class CmdPardon implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("bypunish.pardon")) {
            if (args.length < 2) {
                if (sender instanceof Player)
                    Util.sendMessage("&cIncorrect Syntax! Usage: /&7pardon &c[&7player name&c] [&7punishment id&c] &c{&7-d&c}", (Player) sender);
                else
                    Bukkit.getConsoleSender().sendMessage(Util.colorText("&cIncorrect Syntax! Usage: /&7pardon &c[&7player name&c] [&7punishment id&c]"));

                return false;
            }

            try {
                int pid = Integer.parseInt(args[1]);
                OfflinePlayer toPardon = Bukkit.getOfflinePlayer(args[0]);
                Punishment punishment = Util.getPunishmentFromID(pid);
                if (punishment == null) {
                    sender.sendMessage(Util.colorText("&cError: &7" + args[1] + "&c is not a valid punishment ID."));
                    return false;
                }
                Network network = BYPunishment.getNetwork();
                network.activateConnection();

                String dataQuery = "select * from byp_userdata where uuid = ? and punishment_id = ?";
                PreparedStatement ps = network.getConnection().prepareStatement(dataQuery);
                ps.setString(1, toPardon.getUniqueId().toString());
                ps.setInt(2, pid);
                ResultSet res = ps.executeQuery();

                if (res.next()) {
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("-d")) {
                            network.executeUpdate("delete from byp_userdata where uuid = '" + toPardon.getUniqueId().toString() + "' and punishment_id = " + pid);
                            if (toPardon.isOnline()) {
                                Player player = (Player) toPardon;
                                Util.sendMessage("&7&lNOTICE&r&c: Your infraction for &7" + punishment.getName() + "&c has been forgiven, all warnings have been removed.", player);
                            }
                            sender.sendMessage(Util.colorText("&7Player &b" + toPardon.getName() + "&7 has been completely pardoned for &b" + punishment.getName() + "&7 (all infractions and warnings removed for this warning category)"));
                            network.closeConnection();
                            return true;
                        } else {
                            sender.sendMessage(Util.colorText("&cError: Invalid parameter &7" + args[2] + " &c\"&7-d&c\" expected."));
                            network.closeConnection();
                            return false;
                        }
                    }

                    network.executeUpdate("update byp_userdata set is_active = 0 where uuid = '" + toPardon.getUniqueId().toString() + "' and punishment_id = " + pid);
                    if (toPardon.isOnline()) {
                        Player player = (Player) toPardon;
                        Util.sendMessage("&7&lNOTICE&r&c: You have been pardoned for &7" + punishment.getName() + "&c, however your warning level still persists.", player);
                    }
                    sender.sendMessage(Util.colorText("&cPlayer &7" + toPardon.getName() + "&c has been pardoned. However their warning level still persists."));
                    network.closeConnection();
                    return true;
                } else {
                    sender.sendMessage(Util.colorText("&cError: No punishment found for player: &7" + args[0] + "&c for punishment ID: &7" + args[1]));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Util.colorText("&cError: &7" + args[1] + "&c is not a valid number."));
                return false;

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }else{
            Util.sendMessage("&cError: You do not have permission to perform this command.", (Player)sender);
        }
        return false;
    }
}
