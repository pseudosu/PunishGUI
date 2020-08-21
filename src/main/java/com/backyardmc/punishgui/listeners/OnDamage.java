package com.backyardmc.punishgui.listeners;

import com.backyardmc.punishgui.BYPunishment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OnDamage implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (BYPunishment.frozenPlayers.contains(player.getUniqueId()))
                e.setCancelled(true);
        }
    }
}
