package com.backyardmc.punishgui.commands;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdFreeze implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("bypunish.freeze")) {
            if (args.length < 1) {
                sender.sendMessage(Util.colorText("&cError: Invalid Syntax. Usage: /freeze &7[&cplayer name&7]"));
                return false;
            }
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(Util.colorText("&cError: Invalid Player."));
                return false;
            }
            if (p.hasPermission("bypunish.override") && !p.isOp()) {
                sender.sendMessage(Util.colorText("&cError: You cannot freeze that player."));
                return false;
            }
            if (BYPunishment.frozenPlayers.contains(p.getUniqueId())) {
                sender.sendMessage(Util.colorText("&7" + p.getName() + " &ahas been unfrozen."));
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " subtitle {\"text\": \"Thank you for cooperating. You have been unfrozen.\", \"bold\": \"false\", \"color\": \"green\" }");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " title {\"text\":\" \", \"bold\":\"false\", \"color\":\"white\"}");
                p.sendMessage(Util.colorText("&aThank you for cooperating. You have been unfrozen."));
                BYPunishment.frozenPlayers.remove(p.getUniqueId());
            } else {
                sender.sendMessage(Util.colorText("&7" + p.getName() + " &chas been frozen."));
                //p.playSound(p.getLocation(), Sound.GHAST_DEATH, 3f, .5f);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " subtitle {\"text\": \"WARNING: YOU HAVE BEEN FROZEN BY A STAFF MEMBER. DO NOT LOG OFF OR YOU WILL BE BANNED.\", \"bold\": \"true\", \"color\": \"red\" }");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " title {\"text\":\" \", \"bold\":\"false\", \"color\":\"white\"}");
                for (int i = 0; i < 20; i++) {
                    p.sendMessage(Util.colorText("&cWARNING: YOU HAVE BEEN FROZEN BY A STAFF MEMBER. DO NOT LOG OFF OR YOU WILL BE BANNED."));
                }
                BYPunishment.frozenPlayers.add(p.getUniqueId());
            }
        }
        return false;
    }
}
