package com.backyardmc.punishgui.listeners;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.network.Network;
import com.backyardmc.punishgui.util.Punishment;
import com.backyardmc.punishgui.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class OnJoin implements Listener {

    @EventHandler
    public void generatePlayerItemStacksOnJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String playerName = Util.colorText("&a" + p.getName());
        String nickName = Util.colorText("Nickname: " + p.getDisplayName());
        ItemStack playerHead = Util.createGuiItem(Material.BOOK, playerName, nickName);
        BYPunishment.onlinePlayersItemStacks.add(playerHead);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkBanOnJoin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission("bypunish.override") && !event.getPlayer().isOp())
            return;
        Network network = BYPunishment.getNetwork();
        network.activateConnection();
        String uuid = event.getPlayer().getUniqueId().toString();

        ResultSet res = network.executeQuery("select * from byp_userdata where uuid = '" + uuid + "' and is_active = 1");

        while (true) {
            try {
                if (!res.next()) break;
                int punishment_id = res.getInt("punishment_id");
                int warningLevel = res.getInt("warning_level");

                Punishment punishment = Util.getPunishmentFromID(punishment_id);
                String punishmentType = Objects.requireNonNull(punishment).getPunishmentType(warningLevel);
                if (punishmentType.equalsIgnoreCase("permaban")) {
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Util.formatText(BYPunishment.getConfigManager().getString("strings.disconnectpermaban"), punishment, warningLevel, 0));
                    network.closeConnection();
                    return;
                }
                if (punishmentType.equalsIgnoreCase("ban")) {
                    long end_date = Long.parseLong(res.getString("end_date"));
                    long remainingTime = end_date - System.currentTimeMillis();
                    if (remainingTime <= 0) {
                        network.executeUpdate("update byp_userdata set is_active = 0 where uuid = '" + uuid + "' and punishment_id = '" + punishment_id + "'");
                        network.closeConnection();
                        return;
                    }
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Util.formatText(BYPunishment.getConfigManager().getString("strings.disconnecttempban"), punishment, warningLevel, remainingTime));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        network.closeConnection();
    }

    @EventHandler
    public void removeItemStackOnJoin(PlayerQuitEvent event) {
        BYPunishment.onlinePlayersItemStacks.removeIf(itemStack -> itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(event.getPlayer().getName()));
    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event){
        if(BYPunishment.frozenPlayers.contains(event.getPlayer().getUniqueId())){
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "bungeee ban " + event.getPlayer().getName() + " You have been banned: Logged out while frozen");
        }
    }
}
