package com.backyardmc.punishgui.listeners;

import com.backyardmc.punishgui.BYPunishment;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class OnMove implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Location toLoc = event.getTo();
        Location fromLoc = event.getFrom();
        if (toLoc.getBlock() != fromLoc.getBlock()) {
            if (BYPunishment.frozenPlayers.contains(event.getPlayer().getUniqueId()))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (BYPunishment.frozenPlayers.contains(event.getPlayer().getUniqueId()))
            event.setCancelled(true);
    }
}
