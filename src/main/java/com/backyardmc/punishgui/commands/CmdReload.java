package com.backyardmc.punishgui.commands;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;

public class CmdReload implements CommandExecutor {

    private Plugin p;

    public CmdReload(Plugin p) {
        this.p = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bypunish.reload"))
            sender.sendMessage(Util.colorText("&cError: You do not have permission to perform this command."));

        p.reloadConfig();
        try {
            BYPunishment.getNetwork().init();
            sender.sendMessage(Util.colorText("&aConfiguration reloaded!"));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(Util.colorText("&cError: Could not connect to database. Please make sure you provided the proper details for the MySQL database."));
        }
        return false;
    }
}
