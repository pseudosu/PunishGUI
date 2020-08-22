package com.backyardmc.punishgui;

import com.backyardmc.punishgui.commands.*;
import com.backyardmc.punishgui.listeners.*;
import com.backyardmc.punishgui.network.Network;
import com.backyardmc.punishgui.util.Config;
import com.backyardmc.punishgui.util.Punishment;
import com.cloutteam.samjakob.gui.buttons.InventoryListenerGUI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public final class BYPunishment extends JavaPlugin {

    private static Config config;
    private static Network network;

    public static ArrayList<Punishment> punishments = new ArrayList<>();
    public static ArrayList<ItemStack> onlinePlayersItemStacks = new ArrayList<>();
    public static ArrayList<UUID> frozenPlayers = new ArrayList<>();
    private static JDA api;

    @Override
    public void onEnable() {

        PluginManager pm = Bukkit.getServer().getPluginManager();
        Objects.requireNonNull(getCommand("punish")).setExecutor(new CmdPunish());
        Objects.requireNonNull(getCommand("punish")).setTabCompleter(new PunishTabCompleter());
        Objects.requireNonNull(getCommand("staffhistory")).setExecutor(new CmdHistory());
        Objects.requireNonNull(getCommand("plookup")).setExecutor(new CmdLookup());
        Objects.requireNonNull(getCommand("pardon")).setExecutor(new CmdPardon());
        Objects.requireNonNull(getCommand("pardon")).setTabCompleter(new PardonTabCompleter());
        Objects.requireNonNull(getCommand("freeze")).setExecutor(new CmdFreeze());
        Objects.requireNonNull(getCommand("byreload")).setExecutor(new CmdReload(this));

        pm.registerEvents(new InventoryListenerGUI(), this);
        pm.registerEvents(new OnJoin(), this);
        pm.registerEvents(new OnChat(), this);
        pm.registerEvents(new OnCommand(), this);
        pm.registerEvents(new OnMove(), this);
        pm.registerEvents(new OnDamage(), this);

        // Plugin startup logic
        config = new Config(this);
        config.init();

        network = new Network();
        network.init();
        List<String> punishments = getConfig().getStringList("configuration.settings.punishments");

        for (String s : punishments) {
            String[] values = s.split(", ");

            int id = Integer.parseInt(values[0]);
            String name = values[1];
            String first_offense;
            String second_offense;
            String third_offense;
            String fourth_offense;
            if (values.length == 3) {
                first_offense = values[2];
                BYPunishment.punishments.add(new Punishment(id, name, first_offense, 1));
            } else if (values.length == 4) {
                first_offense = values[2];
                second_offense = values[3];
                BYPunishment.punishments.add(new Punishment(id, name, first_offense, second_offense, 2));
            } else if (values.length == 5) {
                first_offense = values[2];
                second_offense = values[3];
                third_offense = values[4];
                BYPunishment.punishments.add(new Punishment(id, name, first_offense, second_offense, third_offense, 3));
            } else if (values.length == 6) {
                first_offense = values[2];
                second_offense = values[3];
                third_offense = values[4];
                fourth_offense = values[5];
                BYPunishment.punishments.add(new Punishment(id, name, first_offense, second_offense, third_offense, fourth_offense, 4));
            }
        }

        try {
            api = JDABuilder.createDefault(getConfigManager().getString("discord.token")).build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().log(Level.INFO, "You can invite the bot with the following URL: " + api.getInviteUrl());

    }

    @Override
    public void onDisable() {
        api.shutdown();
        api = null;
        network.closeConnection();
    }

    public static Config getConfigManager() {
        return config;
    }

    public static Network getNetwork() {
        return network;
    }

    public static JDA getApi() {
        return api;
    }
}
