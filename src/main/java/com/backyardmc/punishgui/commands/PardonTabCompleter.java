package com.backyardmc.punishgui.commands;

import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PardonTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            List<String> playerNames = new ArrayList<>();
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                playerNames.add(p.getName());
            }
            return playerNames;
        }
        if (args.length == 2) {
            return Util.getAllPunishmentIDs();
        }
        return null;
    }
}
