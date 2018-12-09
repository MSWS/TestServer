package org.mswsplex.testserver.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.MSG;
import org.mswsplex.testserver.utils.Utils;

public class TestCommand implements CommandExecutor, TabCompleter {
	private Main plugin;

	public TestCommand(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("test").setExecutor(this);
		plugin.getCommand("test").setTabCompleter(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return true;
		}
		Player player;
		switch (args[0].toLowerCase()) {
		case "reload":
			plugin.configYml = new File(plugin.getDataFolder(), "config.yml");
			plugin.config = YamlConfiguration.loadConfiguration(plugin.configYml);
			plugin.langYml = new File(plugin.getDataFolder(), "lang.yml");
			plugin.lang = YamlConfiguration.loadConfiguration(plugin.langYml);
			plugin.guiYml = new File(plugin.getDataFolder(), "guis.yml");
			plugin.gui = YamlConfiguration.loadConfiguration(plugin.guiYml);
			MSG.tell(sender, MSG.getString("Reloaded", "Successfully reloaded."));
			break;
		case "reset":
			plugin.saveResource("config.yml", true);
			plugin.saveResource("lang.yml", true);
			plugin.saveResource("guis.yml", true);
			plugin.configYml = new File(plugin.getDataFolder(), "config.yml");
			plugin.langYml = new File(plugin.getDataFolder(), "lang.yml");
			plugin.config = YamlConfiguration.loadConfiguration(plugin.configYml);
			plugin.lang = YamlConfiguration.loadConfiguration(plugin.langYml);
			plugin.guiYml = new File(plugin.getDataFolder(), "guis.yml");
			plugin.gui = YamlConfiguration.loadConfiguration(plugin.guiYml);
			MSG.tell(sender, MSG.prefix() + " Succesfully reset.");
			break;
		case "worlds":
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player.");
				return true;
			}
			player = (Player) sender;
			PlayerManager.setInfo(player, "openInventory", "worldViewer");
			PlayerManager.setInfo(player, "page", 0);
			player.openInventory(Utils.getWorldViewerGUI(player));
			break;
		case "unloaded":
			MSG.tell(sender, Bukkit.getWorldContainer().toPath() + "");
			for(String res:Utils.getUnloadedWorlds(false))
				MSG.tell(sender, res);
			break;
		default:
			return false;
		}
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		for (String res : new String[] { "worlds" }) {
			if (args.length <= 1 && res.toLowerCase().startsWith(args[0].toLowerCase())) {
				result.add(res);
			}
		}
		return result;
	}
}
