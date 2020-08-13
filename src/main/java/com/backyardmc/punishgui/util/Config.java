package com.backyardmc.punishgui.util;

import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    Plugin p;
    public final String CONFIG_PATH = "configuration.settings.";


    public Config(Plugin p) {
        this.p = p;
    }

    public void init() {
        p.getConfig().options().copyDefaults(true);
        p.getConfig().addDefault(CONFIG_PATH + "mysql.host", "localhost");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.database", "bypunishments");
        p.getConfig().addDefault(CONFIG_PATH + "http.port", "8080");
        p.getConfig().addDefault(CONFIG_PATH + "http.enabled", "true");
        p.getConfig().addDefault(CONFIG_PATH + "http.webroot", "/");

        p.getConfig().addDefault(CONFIG_PATH + "strings.permamutemessage", "&7&lINFRACTION:&c You have been &lpermanently&r&c muted.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.tempmutemessage", "&7&lINFRACTION:&c You have been muted for: &7%TIME%%TIME_INCREMENT%&c.");

        p.getConfig().addDefault(CONFIG_PATH + "strings.permabanmessage", "&7&lINFRACTION:&c You have been &lpermanently&r&c banned.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.tempbanmessage", "&7lINFRACTION:&c You have been banned for: &7%TIME%%TIME_INCREMENT%&c.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.disconnectpermaban", "&7&lINFRACTION:&c You have been &lpermanently&r&c banned.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.disconnecttempban", "&7&lINFRACTION:&c You are temporarily banned for: %DAYS%:%HOURS%:%MINUTES%:%SECONDS% for: %REASON%");


        p.getConfig().addDefault(CONFIG_PATH + "strings.kickmessage", "&7&lINFRACTION:&c You have been kicked.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.warnmessage", "&7&LINFRACTION:&c You have been warned for: &7%REASON%&c.");
        p.getConfig().addDefault(CONFIG_PATH + "strings.mutedmessage", "&7&lNOTICE&r&c: You are temporarily muted for:  %DAYS%:%HOURS%:%MINUTES%:%SECONDS% for %REASON%");

        p.getConfig().addDefault(CONFIG_PATH + "strings.servername", "My Minecraft Server");


        p.getConfig().addDefault(CONFIG_PATH + "mysql.username", "root");
        p.getConfig().addDefault(CONFIG_PATH + "mysql.password", "");
        p.getConfig().addDefault(CONFIG_PATH + "punishments", Collections.singletonList("0, spamming, warn, kick, 30 m mute, 12 h ban"));
        p.getConfig().addDefault(CONFIG_PATH + "commands.muted.disallow", Arrays.asList("msg", "message", "r", "reply", "w", "whisper", "t", "tell", "minecraft:tell"));
        p.getConfig().addDefault(CONFIG_PATH + "commands.frozen.disallow", Arrays.asList("tp", "teleport", "tpa", "home", "f home", "all"));


        p.getConfig().addDefault(CONFIG_PATH + "discord.token", "yourtokenhere");
        p.getConfig().addDefault(CONFIG_PATH + "discord.staffchannel", "staffchannelIDhere");
        p.getConfig().addDefault(CONFIG_PATH + "discord.serverid", "yourserveridhere");


        p.saveConfig();
    }

    /**
     * @param path string path to use
     * @return string
     */
    public String getString(String path) {
        return p.getConfig().getString(CONFIG_PATH + path);
    }

    /**
     * @param path string path to use
     * @return string array
     */
    public List<String> getStringArray(String path) {
        return p.getConfig().getStringList(CONFIG_PATH + path);
    }

    /**
     * @return mysql host
     */
    public String getMYSQL_HOST() {
        return getString("mysql.host");
    }

    /**
     * @return mysql database
     */
    public String getMYSQL_DATABASE() {
        return getString("mysql.database");
    }

    /**
     * @return mysql username
     */
    public String getMYSQL_USERNAME() {
        return getString("mysql.username");
    }

    /**
     * @return mysql password
     */
    public String getMYSQL_PASSWORD() {
        return getString("mysql.password");
    }

    public String getWebServerPort() {
        return getString("http.port");
    }

    public boolean isWebServerEnabled() {
        return p.getConfig().getBoolean("http.enabled");
    }

    public String getWebRoot() {
        return getString("http.webroot");
    }
}
