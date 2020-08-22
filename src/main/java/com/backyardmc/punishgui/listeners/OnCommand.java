package com.backyardmc.punishgui.listeners;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class OnCommand implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCmd(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPermission("bypunish.override") && !event.getPlayer().isOp())
            return;
        String[] args = event.getMessage().toLowerCase().split(" ");
        List<String> blockedCommands = BYPunishment.getConfigManager().getStringArray("commands.muted.disallow");
        List<String> blockedFreezeCommands = BYPunishment.getConfigManager().getStringArray("commands.frozen.disallow");

        for (String command : blockedCommands) {
            if (args[0].equalsIgnoreCase("/" + command)) {
                event.setCancelled(Util.checkMute(event.getPlayer().getUniqueId().toString(), event.getPlayer()));
                return;
            }
        }
        if (BYPunishment.frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            for (String command : blockedFreezeCommands) {
                if (args[0].equalsIgnoreCase("/" + command))
                    event.setCancelled(true);
                return;
            }
        }
    }
}
