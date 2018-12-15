package org.mswsplex.servermanager.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.Utils;

public class ManageCommand implements CommandExecutor, TabCompleter {
	private ServerManager plugin;

	private int lag;

	/**
	 * Permission: manage.command Subperms: manage.command.[perm]
	 * 
	 * @param plugin
	 */
	public ManageCommand(ServerManager plugin) {
		this.plugin = plugin;
		PluginCommand cmd = plugin.getCommand("manage");
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
		cmd.setPermission("manage.command");
		cmd.setPermissionMessage(MSG.noPermMessage());
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if (lag > 0)
				try {
					Thread.sleep(lag);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}, 0, 1);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.sendHelp(sender, 0, "default");
			return true;
		}
		Player player;
		if (!sender.hasPermission("manage.command." + args[0])) {
			MSG.noPerm(sender);
		}
		switch (args[0].toLowerCase()) {
		case "reload":
			plugin.configYml = new File(plugin.getDataFolder(), "config.yml");
			plugin.config = YamlConfiguration.loadConfiguration(plugin.configYml);
			plugin.langYml = new File(plugin.getDataFolder(), "lang.yml");
			plugin.lang = YamlConfiguration.loadConfiguration(plugin.langYml);
			plugin.guiYml = new File(plugin.getDataFolder(), "guis.yml");
			plugin.gui = YamlConfiguration.loadConfiguration(plugin.guiYml);
			MSG.tell(sender, MSG.getString("Reloaded", "There was an error reloading."));
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
			MSG.tell(sender, MSG.getString("Reset", "There was an error resetting."));
			break;
		case "worlds":
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player.");
				return true;
			}
			player = (Player) sender;
			PlayerManager.setInfo(player, "page", 0);
			player.openInventory(Utils.getWorldViewerGUI(player));
			PlayerManager.setInfo(player, "openInventory", "worldViewer");
			break;
		case "entities":
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player.");
				return true;
			}
			player = (Player) sender;
			World world = player.getWorld();
			if (args.length > 1)
				world = Bukkit.getWorld(args[1]);
			if (world == null) {
				MSG.tell(sender, MSG.getString("Unknown.World", "Unknown world"));
				return true;
			}
			PlayerManager.setInfo(player, "page", 0);
			player.openInventory(Utils.getEntityViewerGUI(player, world));
			PlayerManager.setInfo(player, "openInventory", "entityViewer");
			PlayerManager.setInfo(player, "managingWorld", world.getName());
			break;
		case "lag":
			if (args.length == 1) {
				if (lag > 0) {
					MSG.tell(sender, MSG.getString("Lag.Active", "lag is at %lagColor%%amo%ms")
							.replace("%lagColor%", lagColor(lag)).replace("%amo%", lag + ""));
				} else {
					MSG.tell(sender, MSG.getString("Lag.Inactive", "There is no lag at the moment"));
				}
				return true;
			}
			int amo;
			try { // /test lag 1
				amo = Integer.parseInt(args[1]);
			} catch (IllegalArgumentException e) {
				MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
				return true;
			}
			if (amo <= 0) {
				MSG.tell(sender, MSG.getString("Lag.Disabled", "you disabled the artificial lag"));
				lag = 0;
				return true;
			}

			if (amo > plugin.config.getInt("Max.LagAmount.HardLimit")) {
				MSG.tell(sender, MSG.getString("Unable.Lag", "max reached: &e%amo%/%max%&c").replace("%amo%", amo + "")
						.replace("%max%", plugin.config.getInt("Max.LagAmount.HardLimit") + ""));
				return true;
			}

			if (amo > plugin.config.getInt("Max.LagAmount.Confirm") && (sender instanceof Player)
					&& !PlayerManager.getBoolean(((Player) sender), "confirm")) {
				MSG.tell(sender, MSG.getString("Warning.Lag", "this will be very laggy(%lagColor%%amo%ms&c)")
						.replace("%lagColor%", lagColor(amo)).replace("%amo%", amo + ""));
				MSG.tell(sender, MSG.getString("Warning.Confirm", "type /confirm to confirm this action"));
				String all = label + " ";
				for (String res : args)
					all = all + res + " ";
				PlayerManager.setInfo((Player) sender, "confirmCommand", all);
				return true;
			}
			MSG.tell(sender, MSG.getString("Lag.Enabled", "you set lag to %lagColor%%amo%ms")
					.replace("%lagColor%", lagColor(amo)).replace("%amo%", amo + ""));
			lag = amo;
			break;
		case "unloaded":
			MSG.tell(sender, Bukkit.getWorldContainer().toPath() + "");
			for (String res : Utils.getUnloadedWorlds(false))
				MSG.tell(sender, res);
			break;
		default:
			return false;
		}
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		if (args.length <= 1) {
			for (String res : new String[] { "worlds", "entities", "lag", "reload", "reset" }) {
				if (sender.hasPermission("manage.command." + res)
						&& res.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(res);
				}
			}
		}
		if (args.length > 1 && args.length < 3) {
			if (args[0].equalsIgnoreCase("entities") && sender.hasPermission("manage.command.entities")) {
				for (World w : Bukkit.getWorlds()) {
					if (w.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(w.getName());
				}
			}
		}
		return result;
	}

	private String lagColor(int amo) {
		String col = "";
		int big = 0;
		try {
			for (String level : plugin.getConfig().getConfigurationSection("LagColors").getKeys(false)) {
				int l = Integer.parseInt(level);
				if (amo >= l && l >= big) {
					big = l;
					col = plugin.getConfig().getString("LagColors." + level);
				}
			}
			return col;
		} catch (Exception e) {
			MSG.log("[WARNING] Configuration is outdated, please type /manage reset to reset the config");
		}
		if (amo > 150) {
			return "&4";
		} else if (amo > 100) {
			return "&c";
		} else if (amo > 80) {
			return "&6";
		} else if (amo > 50) {
			return "&e";
		} else {
			return "&a";
		}
	}
}
