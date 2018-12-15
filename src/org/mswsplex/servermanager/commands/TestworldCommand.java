package org.mswsplex.servermanager.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.managers.CustomChunkGenerator;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.Utils;

public class TestworldCommand implements CommandExecutor, TabCompleter {
	ServerManager plugin;

	/**
	 * Permission: manage.command.testworld
	 * 
	 * @param plugin
	 */
	public TestworldCommand(ServerManager plugin) {
		this.plugin = plugin;
		PluginCommand cmd = plugin.getCommand("testworld");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.testworld");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.tell(sender, "/testworld [Worldname] <Type> - Creates or teleports a world");
			return true;
		}

		String name = args[0].equals("RANDOM") ? MSG.genUUID(8) : args[0];

		World world = Bukkit.getWorld(name);

		if (world != null) {
			if (sender instanceof Player) {
				((Player) sender).teleport(world.getSpawnLocation());
				return true;
			} else {
				MSG.tell(sender, "You must be a player");
			}
			return true;
		}

		if (args.length <= 1) {
			MSG.tell(sender, "/testworld [Worldname] <Type> - Creates or teleports a world");
			return true;
		}

		Material[] layers = null;
		String unknown = "";
		int amo = 0;

		switch (args[1].toLowerCase()) {
		case "flat":
		case "superflat":
		case "normal":
		case "overworld":
		case "regular":
		case "nether":
		case "end":
		case "the_end":
		case "amplified":
			break;
		default:
			ConfigurationSection custom = plugin.config.getConfigurationSection("CustomWorldTypes");
			layers = new Material[100];
			boolean skip = false;
			if (custom != null) {
				for (String type : custom.getKeys(false)) {
					if (args[1].equalsIgnoreCase(type)) {
						int pos = 0;
						for (String mat : custom.getStringList(type)) {
							layers[pos] = Material.valueOf(mat);
							pos++;
						}
						skip = true;
						break;
					}
				}
			}
			if (!skip) {
				int pos = 0;
				for (String res : args[1].split(",")) {
					if (Utils.isMaterial(res.toUpperCase())) {
						layers[pos] = Material.valueOf(res.toUpperCase());
						pos++;
					} else {
						unknown = unknown + res + MSG.getString("Unknown.Blocks.Separator", ", ");
					}
				}

				int arg = 0;
				for (String res : args) {
					if (arg <= 1) {
						arg++;
						continue;
					}
					if (Utils.isMaterial(res.toUpperCase())) {
						layers[pos] = Material.valueOf(res.toUpperCase());
						pos++;
					} else {
						unknown = unknown + res + MSG.getString("Unknown.Blocks.Separator", ", ");
					}
					arg++;
				}
			}

			for (Material mat : layers) {
				if (mat == null)
					continue;
				amo++;
			}

			if (!unknown.isEmpty() && (sender instanceof Player)
					&& !PlayerManager.getBoolean(((Player) sender), "confirmed")) {
				unknown = unknown.substring(0,
						unknown.length() - MSG.getString("Unknown.Blocks.Separator", ", ").length());
				MSG.tell(sender, MSG.getString("Unknown.Blocks.Message", "unknown: %blocks%")
						.replace("%blocks%", unknown).replace("%s%", unknown.contains(",") ? "s" : ""));
				MSG.tell(sender, MSG.getString("Warning.Confirm", "type /confirm to confirm this action"));
				String all = label + " ";
				for (String res : args)
					all = all + res + " ";
				PlayerManager.setInfo((Player) sender, "confirmCommand", all);
				return true;
			}
			if (layers.length == 0)
				return true;
			break;
		}
		if (amo == 0)
			MSG.tell(sender, MSG.getString("World.Generating.Other", "generating %name% with %type% %s%")
					.replace("%name%", name).replace("%type%", MSG.camelCase(args[1])));
		switch (args[1].toLowerCase()) {
		case "flat":
		case "superflat":
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name).type(org.bukkit.WorldType.FLAT));
			break;
		case "amplified":
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name).type(org.bukkit.WorldType.AMPLIFIED));
			break;
		case "normal":
		case "regular":
		case "overworld":
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name));
			break;
		case "nether":
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name).environment(Environment.NETHER));
			break;
		case "end":
		case "the_end":
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name).environment(Environment.THE_END));
			break;
		default:
			MSG.tell(sender, MSG.getString("World.Generating.Layers", "generating %name% with %layers% %s%")
					.replace("%name%", name).replace("%layers%", amo + "").replace("%s%", amo == 1 ? "" : "s"));
			MSG.tell(sender, MSG.getString("Warning.Time", "this may take some time"));
			world = Bukkit.createWorld(WorldCreator.name(name).generator(new CustomChunkGenerator(layers)));
			break;
		}

		world.setGameRuleValue("doMobSpawning", "false");
		world.setGameRuleValue("doMobLoot", "false");
		if (sender instanceof Player)
			((Player) sender).teleport(world.getSpawnLocation());
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		if (args.length <= 1) {
			if ("random".startsWith(args[0].toLowerCase())) {
				result.add("RANDOM");
			}
			for (World world : Bukkit.getWorlds()) {
				if (world.getName().toLowerCase().startsWith(args[0].toLowerCase()) && result.size() < 10)
					result.add(world.getName());
			}
		} else {
			if (args.length == 2) {
				for (String res : new String[] { "Overworld", "Nether", "End", "Superflat", "Amplified" }) {
					if (res.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(res);
				}
				ConfigurationSection custom = plugin.config.getConfigurationSection("CustomWorldTypes");
				if (custom != null) {
					for (String type : custom.getKeys(false)) {
						if (type.toLowerCase().startsWith(args[1].toLowerCase()) && result.size() < 10) {
							result.add(type);
						}
					}
				}
			}
			for (Material mat : Material.values()) {
				if (!mat.isBlock())
					continue;
				if (mat.toString().toLowerCase().startsWith(args[args.length - 1].toLowerCase())
						&& result.size() < 10) {
					result.add(MSG.camelCase(mat.toString()).replace(" ", "_"));
				}
				if ((args[args.length - 1].contains(",") && mat.toString().toLowerCase()
						.startsWith(args[args.length - 1].split(",")[args[args.length - 1].split(",").length - 1]))
						&& result.size() < 10) {
					String prefix = "";
					int pos = 0;
					for (String res : args[args.length - 1].split(",")) {
						if (pos == args[args.length - 1].split(",").length - 1)
							continue;
						prefix = prefix + res + ",";
						pos++;
					}
					MSG.tell(sender, "prefix: " + prefix);
					result.add(prefix + MSG.camelCase(mat.toString()).replace(" ", "_"));
				}
			}
		}
		return result;
	}
}
