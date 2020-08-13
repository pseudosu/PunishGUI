package com.backyardmc.punishgui.network;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.util.Punishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PunishmentManager {

    public static void punish(Punishment punishment, OfflinePlayer player, Player commandExecutor) throws SQLException {
        Network network = BYPunishment.getNetwork();
        network.activateConnection();

        int pid = punishment.getId();
        UUID uuid = player.getUniqueId();
        ResultSet resultSet = network.executeQuery("SELECT * FROM byp_userdata WHERE uuid = '" + uuid.toString() + "'");

        //is there an entry in the database already for this user?
        while (resultSet.next()) {
            //this function is called when a player already has an instance of this specific punishment
            //it handles incrementing the warning levels, and dishing out appropriate punishments
            if (resultSet.getInt("punishment_id") == pid) {
                hasExistingPunishment(punishment, player, commandExecutor, pid, uuid, resultSet, network);
                return;
            }
        }
        //this is called if the offense isn't time based, i.e perma ban, or perma mute.
        if (!punishment.isTimeBased(1)) {
            createNonTimeBasedPunishment(punishment, player, commandExecutor, pid, uuid, network);
        } else {
            //handle new temp bans and mutes
            createTemporaryPunishment(punishment, player, commandExecutor, pid, uuid, network);
            return;
        }
        network.closeConnection();
    }

    /**
     * @param network         network instance ot use
     * @param commandExecutor person that ran the command
     * @param player          person to punish
     * @param pid             punishment ID
     * @throws SQLException SQL errors
     */
    private static void logStaffAction(Network network, Player commandExecutor, OfflinePlayer player, int pid) throws SQLException {
        String updateQuery = "insert into byp_staffdata(punishment_id, staff_uuid, punished_uuid, date) values(?, ?, ? ,?)";


        PreparedStatement ps = network.getConnection().prepareStatement(updateQuery);
        ps.setInt(1, pid);
        ps.setString(2, commandExecutor.getUniqueId().toString());
        ps.setString(3, player.getUniqueId().toString());
        ps.setString(4, System.currentTimeMillis() + "");
        ps.executeUpdate();
    }

    /**
     * @param punishment        punishment instance
     * @param player            player to punish
     * @param commandExecutor   command sender
     * @param pid               punishment ID
     * @param uuid              unique id of player
     * @param network           network instance
     * @param new_warning_level the new warning level to update to
     * @throws SQLException sql errors
     */
    private static void escalateExistingPunishment(Punishment punishment, OfflinePlayer player, Player commandExecutor, int pid, UUID uuid, Network network, int new_warning_level) throws SQLException {

        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("permamute")) {
            if (!commandExecutor.hasPermission("bypunish.permamute")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("permaban")) {
            if (!commandExecutor.hasPermission("bypunish.permaban")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }

        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("warn")) {
            if (!commandExecutor.hasPermission("bypunish.warn")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("kick")) {
            if (!commandExecutor.hasPermission("bypunish.kick")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("mute")) {
            if (!commandExecutor.hasPermission("bypunish.mute")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(new_warning_level).equalsIgnoreCase("ban")) {
            if (!commandExecutor.hasPermission("bypunish.ban")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        network.executeUpdate("update byp_userdata set warning_level = " + new_warning_level + ", is_active = 1 where punishment_id = " + pid + " and uuid = '" + uuid.toString() + "'");
        String punishmentType = punishment.getPunishmentType(new_warning_level);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "bungeee sc [" + BYPunishment.getConfigManager().getString("strings.servername") + "]" + commandExecutor.getName() + " punished " + player.getName() + " for " + punishment.getName() + ". Action taken: " + punishmentType);

        if (punishment.isTimeBased(new_warning_level)) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);


            int time = punishment.getPunishmentTime(new_warning_level);
            String timeIncrement = punishment.getPunishmentTimeIncrement(new_warning_level);
            long endDate = Util.convertAndAddTimeMillis(time, timeIncrement) - System.currentTimeMillis();
            if (punishmentType.equalsIgnoreCase("warn")) {
                if (player.isOnline())
                    Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.warnmessage"), punishment, new_warning_level, endDate), (Player) player);
            }
            if (punishmentType.equalsIgnoreCase("mute")) {
                network.executeUpdate("update byp_userdata set end_date = "
                        + Util.convertAndAddTimeMillis(time, timeIncrement)
                        + " where uuid = '" + uuid.toString() + "' and punishment_id = " + pid);
                Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been temporarily muted for: &7" + time + timeIncrement + "&c.", commandExecutor);
                if (player.isOnline())
                    Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempmutemessage"), punishment, new_warning_level, endDate), (Player) player);
            }
            if (punishmentType.equalsIgnoreCase("ban")) {
                network.executeUpdate("update byp_userdata set end_date = "
                        + Util.convertAndAddTimeMillis(time, timeIncrement)
                        + " where uuid = '" + uuid.toString() + "' and punishment_id = " + pid);
                Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been banned for: &7" + time + timeIncrement + "&c.", commandExecutor);
                if (player.isOnline())
                    ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, new_warning_level, endDate));
                else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, new_warning_level, endDate));
                }
            }
        } else {

            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);

            if (punishmentType.equalsIgnoreCase("permaban")) {
                Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been permanently banned&c.", commandExecutor);
                network.executeUpdate("update byp_userdata set end_date = null where uuid ='" + uuid.toString() + "' and punishment_id = " + pid);

                if (player.isOnline())
                    ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.permabanmessage"), punishment, new_warning_level, 0));
                else
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.permabanmessage"), punishment, new_warning_level, 0));
            }
            if (punishmentType.equalsIgnoreCase("permamute")) {
                Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been permanently muted&c.", commandExecutor);
                network.executeUpdate("update byp_userdata set end_date = null where uuid ='" + uuid.toString() + "' and punishment_id = " + pid);
                if (player.isOnline())
                    Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.permamutemessage"), punishment, new_warning_level, 0), (Player) player);
            }
            if (punishmentType.equalsIgnoreCase("kick")) {
                Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been kicked&c.", commandExecutor);
                network.executeUpdate("update byp_userdata set end_date = null where uuid ='" + uuid.toString() + "' and punishment_id = " + pid);
                if (player.isOnline())
                    ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.kickmessage"), punishment, new_warning_level, 0));
                else
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.kickmessage"), punishment, new_warning_level, 0));
            }
        }
    }

    /**
     * @param punishment      punishment instance
     * @param player          target of punishment
     * @param commandExecutor person who ran the punish command
     * @param pid             punishment ID
     * @param uuid            uuid of player to punish
     * @param resultSet       resultset to use
     * @param network         network instance
     * @throws SQLException sql errors
     */
    private static void hasExistingPunishment(Punishment punishment, OfflinePlayer player, Player commandExecutor, int pid, UUID uuid, ResultSet resultSet, Network network) throws SQLException {
        if (resultSet.getInt("punishment_id") == pid) {
            int old_warning_level = resultSet.getInt("warning_level");
            int new_warning_level = old_warning_level + 1;

            //have they reached MAX warning level
            if (new_warning_level > punishment.getMaxWarningLevel()) {
                String punishmentType = punishment.getPunishmentType(old_warning_level);

                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("permamute")) {
                    if (!commandExecutor.hasPermission("bypunish.permamute")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }
                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("permaban")) {
                    if (!commandExecutor.hasPermission("bypunish.permaban")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }

                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("warn")) {
                    if (!commandExecutor.hasPermission("bypunish.warn")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }
                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("kick")) {
                    if (!commandExecutor.hasPermission("bypunish.kick")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }
                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("mute")) {
                    if (!commandExecutor.hasPermission("bypunish.mute")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }
                if (punishment.getPunishmentType(old_warning_level).equalsIgnoreCase("ban")) {
                    if (!commandExecutor.hasPermission("bypunish.ban")) {
                        Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                        return;
                    }
                }

                //handle perma bans.
                if (punishmentType.equalsIgnoreCase("permaban")) {
                    Util.sendMessage("&cError: The player &7" + player.getName() + "&c is already permanently banned.", commandExecutor);
                    return;
                }
                if (punishmentType.equalsIgnoreCase("permamute")) {
                    Util.sendMessage("&cError: The player &7" + player.getName() + "&c is already permanently muted.", commandExecutor);
                    return;
                }

                Util.sendMessage("&cError: Player &7" + player.getName() + "&c has reached the maximum warning level of &7"
                        + old_warning_level + "&c for punishment: &7" + punishment.getName()
                        + "&c.\nApplying &lmaximum&r&c punishment again.", commandExecutor);

                int toAdd = punishment.getPunishmentTime(old_warning_level);
                String timeIncrement = punishment.getPunishmentTimeIncrement(old_warning_level);
                long endDate = Util.convertAndAddTimeMillis(toAdd, timeIncrement) - System.currentTimeMillis();

                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "bungeee sc [" + BYPunishment.getConfigManager().getString("strings.servername") + "]" + commandExecutor.getName() + " punished " + player.getName() + " for " + punishment.getName() + ". Action taken: " + punishmentType);

                //handle temp mutes
                if (punishmentType.equalsIgnoreCase("mute")) {
                    network.executeUpdate("update byp_userdata set end_date = " + Util.convertAndAddTimeMillis(toAdd, timeIncrement) + ", is_active = 1 where punishment_id =" + pid + " and uuid = '" + uuid.toString() + "'");
                    logStaffAction(network, commandExecutor, player, pid);
                    String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
                    Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
                    //do mute logic if player is online
                    if (player.isOnline()) {
                        Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempmutemessage"), punishment, old_warning_level, endDate), (Player) player);
                        return;
                    }
                }
                //handle temp bans
                if (punishmentType.equalsIgnoreCase("ban")) {
                    network.executeUpdate("update byp_userdata set end_date = " + Util.convertAndAddTimeMillis(toAdd, timeIncrement) + ", is_active = 1 where punishment_id =" + pid + " and uuid = '" + uuid.toString() + "'");
                    logStaffAction(network, commandExecutor, player, pid);
                    String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
                    Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
                    if (player.isOnline()) {
                        ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, old_warning_level, endDate));
                    } else
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, old_warning_level, endDate));
                }
                return;
            }
            escalateExistingPunishment(punishment, player, commandExecutor, pid, uuid, network, new_warning_level);
        }
    }

    /**
     * @param punishment      punishment instance
     * @param player          target of punishment
     * @param commandExecutor person who ran the punish command
     * @param pid             punishment ID
     * @param uuid            uuid of player to punish
     * @param network         network instance
     * @throws SQLException sql errors
     */
    private static void createNonTimeBasedPunishment(Punishment punishment, OfflinePlayer player, Player commandExecutor, int pid, UUID uuid, Network network) throws SQLException {
        String punishmentType = punishment.getPunishmentType(1);

        if (punishment.getPunishmentType(1).equalsIgnoreCase("permamute")) {
            if (!commandExecutor.hasPermission("bypunish.permamute")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(1).equalsIgnoreCase("permaban")) {
            if (!commandExecutor.hasPermission("bypunish.permaban")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }

        if (punishment.getPunishmentType(1).equalsIgnoreCase("warn")) {
            if (!commandExecutor.hasPermission("bypunish.warn")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(1).equalsIgnoreCase("kick")) {
            if (!commandExecutor.hasPermission("bypunish.kick")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }

        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "bungeee sc [" + BYPunishment.getConfigManager().getString("strings.servername") + "]" + commandExecutor.getName() + " punished " + player.getName() + " for " + punishment.getName() + ". Action taken: " + punishmentType);
        if (punishmentType.equalsIgnoreCase("permamute")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been permanently muted.", commandExecutor);

            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level) values(" + pid + ", '" + uuid + "', 1, 1)");
            if (player.isOnline()) {
                Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.permamutemessage"), punishment, 1, 0), (Player) player);
                return;
            }
        }
        if (punishmentType.equalsIgnoreCase("permaban")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been permanently banned.", commandExecutor);
            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level) values(" + pid + ", '" + uuid + "', 1, 1)");
            if (player.isOnline()) {
                ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.permabanmessage"), punishment, 1, 0));
            } else
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.permabanmessage"), punishment, 1, 0));
        }

        if (punishmentType.equalsIgnoreCase("warn")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been warned.", commandExecutor);
            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level) values(" + pid + ", '" + uuid + "', 1, 1)");
            if (player.isOnline()) {
                Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.warnmessage"), punishment, 1, 0), (Player) player);
            }
        }
        if (punishmentType.equalsIgnoreCase("kick")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been kicked.", commandExecutor);
            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level) values(" + pid + ", '" + uuid + "', 1, 1)");
            if (player.isOnline())
                ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.kickmessage"), punishment, 1, 0));
            else
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.kickmessage"), punishment, 1, 0));
        }
    }

    /**
     * @param punishment      punishment instance
     * @param player          target of punishment
     * @param commandExecutor person who ran the punish command
     * @param pid             punishment ID
     * @param uuid            uuid of player to punish
     * @param network         network instance
     * @throws SQLException sql errors
     */
    private static void createTemporaryPunishment(Punishment punishment, OfflinePlayer player, Player commandExecutor, int pid, UUID uuid, Network network) throws SQLException {
        if (punishment.getPunishmentType(1).equalsIgnoreCase("mute")) {
            if (!commandExecutor.hasPermission("bypunish.mute")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        if (punishment.getPunishmentType(1).equalsIgnoreCase("ban")) {
            if (!commandExecutor.hasPermission("bypunish.ban")) {
                Util.sendMessage("&cError: You do not have permission to escalate this punishment to that level. Please get in contact with a higher-up.", commandExecutor);
                return;
            }
        }
        int toAdd = punishment.getPunishmentTime(1);
        String timeIncrement = punishment.getPunishmentTimeIncrement(1);
        String punishmentType = punishment.getPunishmentType(1);
        int punishmentTime = punishment.getPunishmentTime(1);
        long endDate = Util.convertAndAddTimeMillis(toAdd, timeIncrement);

        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "bungeee sc [" + BYPunishment.getConfigManager().getString("strings.servername") + "]" + commandExecutor.getName() + " punished " + player.getName() + " for " + punishment.getName() + ". Action taken: " + punishmentType);

        if (punishmentType.equalsIgnoreCase("mute")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been temporarily muted for: &7" + punishmentTime + timeIncrement + "&c.", commandExecutor);
            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level, end_date) values(" + pid + ", '" + uuid + "', 1, 1," + endDate + ")");
            if (player.isOnline()) {
                Util.sendMessage(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempmutemessage"), punishment, 1, endDate - System.currentTimeMillis()), (Player) player);
                return;
            }
        }
        if (punishmentType.equalsIgnoreCase("ban")) {
            logStaffAction(network, commandExecutor, player, pid);
            String messageToDiscord = "Player " + player.getName() + " has been punished for " + punishment.getName() + ", action taken: " + punishmentType;
            Util.notifyStaff(messageToDiscord, BYPunishment.getConfigManager().getString("strings.servername"), commandExecutor);
            Util.sendMessage("&cPlayer: &7" + player.getName() + "&c has been temporarily banned for:&7" + punishmentTime + timeIncrement + "&c.", commandExecutor);
            network.executeUpdate("insert ignore into byp_userdata(punishment_id, uuid, is_active, warning_level, end_date) values(" + pid + ", '" + uuid + "', 1, 1," + endDate + ")");
            if (player.isOnline()) {
                ((Player) player).kickPlayer(Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, 1, endDate - System.currentTimeMillis()));
            } else
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee kick " + player.getName() + " " + Util.formatText(BYPunishment.getConfigManager().getString("strings.tempbanmessage"), punishment, 1, endDate - System.currentTimeMillis()));

        }
    }
}