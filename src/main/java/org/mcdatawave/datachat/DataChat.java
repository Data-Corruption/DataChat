package org.mcdatawave.datachat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DataChat extends JavaPlugin {

    private Discord discord;
    private Boolean hadToken = true;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Bukkit.getLogger().info("[DataChat] loaded");

        String bottoken = getConfig().getString("BotToken");

        if (bottoken == null) {
            hadToken = false;
            getLogger().info(" Bot token not entered in config, cancelling disc bot login and disabling plugin.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;}

        Wrapper.plugin = this;
        discord = new Discord(this);
    }

    @Override
    public void onDisable() {
        if (hadToken) {
            discord.shutdownMessage();
        }
    }
}

class Wrapper {
    static Plugin plugin;
}
