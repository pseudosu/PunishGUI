package com.backyardmc.punishgui.listeners;

import com.backyardmc.punishgui.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class OnChat implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("bypunish.override") && !event.getPlayer().isOp())
            return;
        event.setCancelled(Util.checkMute(event.getPlayer().getUniqueId().toString(), event.getPlayer()));
    }
}
