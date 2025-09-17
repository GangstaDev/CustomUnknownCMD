package org.gangsta.customUnknownCMD;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
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
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class CustomUnknownCMD extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private File cfile;
    private String message;
    private SimpleCommandMap commandMap;
    private Set<String> exemptCommands;

    @Override
    public void onEnable() {
        this.config = getConfig();
        this.config.options().copyDefaults(true);

        if (!this.config.contains("message")) {
            this.config.set("message", "&cUnknown Command!");
        }

        // Add exempt commands that should bypass the unknown command check
        if (!this.config.contains("exempt-commands")) {
            this.config.set("exempt-commands", java.util.Arrays.asList("plugins", "pl", "version", "ver"));
        }

        saveConfig();
        this.cfile = new File(getDataFolder(), "config.yml");

        loadConfigValues();
        setupCommandMap();

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        UnknownCMDCommand commandExecutor = new UnknownCMDCommand(this);
        getCommand("unknowncmd").setExecutor(commandExecutor);
        getCommand("unknowncmd").setTabCompleter(commandExecutor);
    }

    private void setupCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            this.commandMap = (SimpleCommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            getLogger().warning("Could not access command map via reflection. Falling back to HelpMap only.");
            this.commandMap = null;
        }
    }

    public void loadConfigValues() {
        reloadConfig();
        this.config = getConfig();
        this.message = ColorUtil.colorize(this.config.getString("message", "&cUnknown Command!"));

        // Load exempt commands
        this.exemptCommands = new HashSet<>(this.config.getStringList("exempt-commands"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // Only handle if the event hasn't been cancelled by other plugins
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String fullCommand = event.getMessage();
        String[] parts = fullCommand.split(" ");
        String baseCommand = parts[0].toLowerCase();

        // Remove the leading slash
        String commandName = baseCommand.substring(1);

        // Check if command is exempt
        if (exemptCommands.contains(commandName)) {
            return;
        }

        // Multiple checks for command existence
        boolean commandExists = false;

        // Method 1: Check HelpMap
        HelpTopic helpTopic = Bukkit.getServer().getHelpMap().getHelpTopic(baseCommand);
        if (helpTopic != null) {
            commandExists = true;
        }

        // Method 2: Check CommandMap via reflection (more reliable)
        if (!commandExists && commandMap != null) {
            Command command = commandMap.getCommand(commandName);
            if (command != null) {
                commandExists = true;
            }
        }

        // Method 3: Check if any plugin handles this command
        if (!commandExists) {
            // Try to see if the command would be handled by dispatching it
            // This is a last resort check
            try {
                // Check if the command has any registered executors
                org.bukkit.command.PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);
                if (pluginCommand != null) {
                    commandExists = true;
                }
            } catch (Exception ignored) {
                // Ignore any errors in this fallback check
            }
        }

        // If no command exists, send custom message and cancel
        if (!commandExists) {
            player.sendMessage(this.message);
            event.setCancelled(true);
        }
    }
}