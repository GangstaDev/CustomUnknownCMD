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

            case "addexemption":
                handleAddExemption(sender, args);
                break;

            case "removeexemption":
                handleRemoveExemption(sender, args);
                break;

            case "listexemptions":
                handleListExemptions(sender);
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

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }

        String newMessage = messageBuilder.toString();

        plugin.getConfig().set("message", newMessage);
        plugin.saveConfig();
        plugin.loadConfigValues();

        sender.sendMessage(ColorUtil.colorize("&aUnknown command message set to: " + ColorUtil.colorize(newMessage)));
    }

    private void handleAddExemption(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /unknowncmd addexemption <command>"));
            return;
        }

        String commandName = args[1].toLowerCase();

        List<String> exemptCommands = plugin.getConfig().getStringList("exempt-commands");

        if (exemptCommands.contains(commandName)) {
            sender.sendMessage(ColorUtil.colorize("&cCommand '" + commandName + "' is already exempt!"));
            return;
        }

        exemptCommands.add(commandName);
        plugin.getConfig().set("exempt-commands", exemptCommands);
        plugin.saveConfig();
        plugin.loadConfigValues();

        sender.sendMessage(ColorUtil.colorize("&aCommand '" + commandName + "' has been added to exemptions!"));
    }

    private void handleRemoveExemption(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&cUsage: /unknowncmd removeexemption <command>"));
            return;
        }

        String commandName = args[1].toLowerCase();

        // Get current exempt commands list
        List<String> exemptCommands = plugin.getConfig().getStringList("exempt-commands");

        if (!exemptCommands.contains(commandName)) {
            sender.sendMessage(ColorUtil.colorize("&cCommand '" + commandName + "' is not in exemptions!"));
            return;
        }

        // Remove the exemption
        exemptCommands.remove(commandName);
        plugin.getConfig().set("exempt-commands", exemptCommands);
        plugin.saveConfig();
        plugin.loadConfigValues();

        sender.sendMessage(ColorUtil.colorize("&aCommand '" + commandName + "' has been removed from exemptions!"));
    }

    private void handleListExemptions(CommandSender sender) {
        List<String> exemptCommands = plugin.getConfig().getStringList("exempt-commands");

        if (exemptCommands.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&eNo exempt commands configured."));
            return;
        }

        sender.sendMessage(ColorUtil.colorize("&6=== &eExempt Commands &6==="));
        for (String command : exemptCommands) {
            sender.sendMessage(ColorUtil.colorize("&7- &e" + command));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&6=== &eCustomUnknownCMD Help &6==="));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd reload &7- Reload the configuration"));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd set <message> &7- Set the unknown command message"));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd addexemption <command> &7- Add command to exemptions"));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd removeexemption <command> &7- Remove command from exemptions"));
        sender.sendMessage(ColorUtil.colorize("&e/unknowncmd listexemptions &7- List all exempt commands"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("customunknowncmd.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String subcommand : Arrays.asList("reload", "set", "addexemption", "removeexemption", "listexemptions")) {
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

        if (args.length == 2 && args[0].equalsIgnoreCase("removeexemption")) {
            List<String> exemptCommands = plugin.getConfig().getStringList("exempt-commands");
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();

            for (String exemptCommand : exemptCommands) {
                if (exemptCommand.toLowerCase().startsWith(input)) {
                    completions.add(exemptCommand);
                }
            }

            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("addexemption")) {
            List<String> suggestions = Arrays.asList("<command1>", "<command2>", "<command3>");
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(input)) {
                    completions.add(suggestion);
                }
            }

            return completions;
        }

        return new ArrayList<>();
    }
}