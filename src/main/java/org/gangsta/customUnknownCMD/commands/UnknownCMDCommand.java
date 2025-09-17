package org.gangsta.customUnknownCMD.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.gangsta.customUnknownCMD.CustomUnknownCMD;
import org.gangsta.customUnknownCMD.util.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnknownCMDCommand implements CommandExecutor, TabCompleter {

    private final CustomUnknownCMD plugin;

    public UnknownCMDCommand(CustomUnknownCMD plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customunkncmd.admin")) {
            sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "set":
                handleSet(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.loadConfigValues();
        sender.sendMessage(ColorUtil.colorize("&aConfiguration reloaded successfully!"));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /unknowncmd set <message>"));
            return;
        }

        // Join all arguments after "set" to form the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }

        String newMessage = messageBuilder.toString();

        // Update config
        plugin.getConfig().set("message", newMessage);
        plugin.saveConfig();
        plugin.loadConfigValues();

        sender.sendMessage(ColorUtil.colorize("&aUnknown command message set to: " + ColorUtil.colorize(newMessage)));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&6=== &eCustomUnknownCMD Help &6==="));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd reload &7- Reload the configuration"));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd set <message> &7- Set the unknown command message"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("customunknowncmd.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String subcommand : Arrays.asList("reload", "set")) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }

            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            List<String> examples = Arrays.asList(
                    "&cUnknown command!",
                    "&#FFD700&lUnknown command!",
                    "&l&gradient:#FFD700:#FFA500:#FF8C00:#FF6347&Unknown command!"
            );

            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();

            for (String example : examples) {
                if (example.toLowerCase().startsWith(input)) {
                    completions.add(example);
                }
            }

            return completions;
        }

        return new ArrayList<>();
    }
}