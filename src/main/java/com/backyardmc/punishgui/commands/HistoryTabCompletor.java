package com.backyardmc.punishgui.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HistoryTabCompletor implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("phistory") && args.length == 0) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                playerNames.add(p.getName());
            }
            return playerNames;
        }
        return null;
    }
}
