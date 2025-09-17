package org.gangsta.customUnknownCMD;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.java.JavaPlugin;
import org.gangsta.customUnknownCMD.commands.UnknownCMDCommand;
import org.gangsta.customUnknownCMD.util.ColorUtil;

import java.io.File;

public class CustomUnknownCMD extends JavaPlugin implements Listener {
    FileConfiguration config;
    File cfile;
    private String Message;

    public void onEnable() {
        this.config = getConfig();
        this.config.options().copyDefaults(true);

        if (!this.config.contains("message")) {
            this.config.set("message", "&cUnknown Command!");
        }

        saveConfig();
        this.cfile = new File(getDataFolder(), "config.yml");

        loadConfigValues();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        UnknownCMDCommand commandExecutor = new UnknownCMDCommand(this);
        getCommand("unknowncmd").setExecutor(commandExecutor);
        getCommand("unknowncmd").setTabCompleter(commandExecutor);

    }

    public void loadConfigValues() {
        reloadConfig();
        this.config = getConfig();
        this.Message = ColorUtil.colorize(this.config.getString("message", "&cUnknown Command!"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (!event.isCancelled()) {
            String command = event.getMessage().split(" ")[0];
            HelpTopic htopic = Bukkit.getServer().getHelpMap().getHelpTopic(command);
            if (htopic == null) {
                p.sendMessage(this.Message);
                event.setCancelled(true);
            }
        }
    }
}