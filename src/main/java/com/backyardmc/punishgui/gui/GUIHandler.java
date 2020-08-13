package com.backyardmc.punishgui.gui;

import com.backyardmc.punishgui.BYPunishment;
import com.backyardmc.punishgui.util.Punishment;
import com.backyardmc.punishgui.util.Util;
import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.buttons.GUIButton;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Objects;

public class GUIHandler {

    /**
     * @param sender person to open the GUI for
     */
    public static void openGUI(CommandSender sender) {
        PaginatedGUI playerListMenu = new PaginatedGUI("Online Players");

        for (Player p : Bukkit.getOnlinePlayers()) {
            //if (p == sender)
            //   continue;
            if (!p.hasPermission("bypunish.bypass") || p.isOp()) {
                String realName = "&7" + p.getName();
                String nickName = "&7Display Name: &6" + p.getDisplayName();

                ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwner(p.getName());
                skullMeta.setDisplayName(Util.colorText(realName));
                skullMeta.setLore(Arrays.asList(Util.colorText(nickName)));
                playerHead.setItemMeta(skullMeta);

                GUIButton playerButton = new GUIButton(playerHead);
                playerButton.setListener(event -> {
                    event.setCancelled(true);
                    PaginatedGUI punishmentListMenu = new PaginatedGUI("Punishments");
                    for (Punishment punishment : BYPunishment.punishments) {
                        String punishmentName = "&c" + punishment.getName();
                        String punishmentID = "&7ID: &c" + punishment.getId();

                        GUIButton punishmentButton = new GUIButton(
                                ItemBuilder.start(Material.BOOK).name(punishmentName)
                                        .lore(Arrays.asList(punishmentID)).build()
                        );
                        punishmentButton.setListener(eventPunishment -> {
                            event.setCancelled(true);
                            String playerToPunish = ChatColor.stripColor(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().getDisplayName());
                            ((Player) sender).performCommand("punish " + punishment.getId() + " " + playerToPunish);
                            ((Player) sender).closeInventory();
                        });
                        punishmentListMenu.addButton(punishmentButton);
                    }
                    ((Player) sender).openInventory(punishmentListMenu.getInventory());
                });
                playerListMenu.addButton(playerButton);
            }
        }
        if (playerListMenu.getInventory() == null) {
            sender.sendMessage(Util.colorText("&cError: There are not enough players online that can be punished to open the GUI. "));
        } else
            ((Player) sender).openInventory(playerListMenu.getInventory());
    }
}
