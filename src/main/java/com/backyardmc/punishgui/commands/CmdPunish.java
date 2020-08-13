package com.backyardmc.punishgui.commands;

import com.backyardmc.punishgui.gui.GUIHandler;
import com.backyardmc.punishgui.network.PunishmentManager;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Objects;

public class CmdPunish implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player commandSender = (Player) sender;

            if (!sender.hasPermission("bypunish.punish")) {
                Util.sendMessage("&cError: You do not have permission to perform this command.", commandSender);
                return false;
            }
            if (args.length < 2) {
                GUIHandler.openGUI(sender);
                return false;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            int punishID;
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                Util.sendMessage("&cError: Invalid Player: " + args[1], commandSender);
                return false;
            }
            try {
                punishID = Integer.parseInt(args[0]);
                if (!Util.getAllPunishmentIDs().contains(punishID + "")) {
                    Util.sendMessage("&cError: Invalid Punishment ID: " + args[0], commandSender);
                    return false;
                }
                PunishmentManager.punish(Objects.requireNonNull(Util.getPunishmentFromID(punishID)), target, commandSender);
                return true;
            } catch (NumberFormatException e) {
                Util.sendMessage("&cError: Invalid Punishment ID: " + args[0], commandSender);
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}