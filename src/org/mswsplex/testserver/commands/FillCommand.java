package org.mswsplex.testserver.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.Cuboid;
import org.mswsplex.testserver.utils.MSG;
import org.mswsplex.testserver.utils.Utils;

public class FillCommand implements CommandExecutor, TabCompleter {

	private Main plugin;

	public FillCommand(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommand("fill").setExecutor(this);
		this.plugin.getCommand("fill").setTabCompleter(this);
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			MSG.tell(sender, "You must be a player.");
			return true;
		}
		if (args.length < 7) {
			MSG.tell(sender,
					"&cUsage: /fill <x1> <y1> <z1> <x2> <y2> <z2> <TileName> [dataValue] [oldBlockHandling] [dataTag]");
			return true;
		}

		Player player = (Player) sender;
		Location p = player.getLocation();
		Location l1, l2;
		double x1, x2, y1, y2, z1, z2;

		try {

			x1 = getTilde(args[0], p, 0);
			y1 = getTilde(args[1], p, 1);
			z1 = getTilde(args[2], p, 2);
			x2 = getTilde(args[3], p, 0);
			y2 = getTilde(args[4], p, 1);
			z2 = getTilde(args[5], p, 2);

			l1 = new Location(player.getWorld(), x1, y1, z1);
			l2 = new Location(player.getWorld(), x2, y2, z2);

//			l1 = new Location(player.getWorld(), args[0].equals("~") ? p.getX() : Double.parseDouble(args[0]),
//					args[1].equals("~") ? p.getY() : Double.parseDouble(args[1]),
//					args[2].equals("~") ? p.getZ() : Double.parseDouble(args[2]));
//			l2 = new Location(player.getWorld(), args[3].equals("~") ? p.getX() : Double.parseDouble(args[3]),
//					args[4].equals("~") ? p.getY() : Double.parseDouble(args[4]),
//					args[5].equals("~") ? p.getZ() : Double.parseDouble(args[5]));
		} catch (IllegalArgumentException e) {
			MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
			return true;
		}

		if (!Utils.isMaterial(args[6].toUpperCase().replace("MINECRAFT:", ""))) {
			MSG.tell(sender, MSG.getString("Unknown.Blocks.Message", "unkown block%s%: %blocks%").replace("%s%", "")
					.replace("%blocks%", args[6]));
			return true;
		}

		Material mat = Material.valueOf(args[6].toUpperCase().replace("MINECRAFT:", "")), replace = null;

		byte damage = 0, replaceDamage = 0;

		if (args.length > 7)
			try {
				damage = Byte.parseByte(args[7]);
			} catch (IllegalArgumentException e) {
				MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
				return true;
			}

		if (args.length > 8) {
			if (!Utils.isMaterial(args[8].toUpperCase().replace("MINECRAFT:", ""))) {
				MSG.tell(sender, MSG.getString("Unknown.Blocks.Message", "unkown block%s%: %blocks%").replace("%s%", "")
						.replace("%blocks%", args[8]));
				return true;
			}
			replace = Material.valueOf(args[8].toUpperCase().replace("MINECRAFT:", ""));
		}

		if (args.length > 9) {
			try {
				replaceDamage = Byte.parseByte(args[9]);
			} catch (IllegalArgumentException e) {
				MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
				return true;
			}
		}

		Cuboid cube = new Cuboid(l1, l2);

		int amo = 0;

		for (Block block : cube) {
			if (replace != null) {
				if (replace != block.getType())
					continue;
				if (block.getData() != replaceDamage)
					continue;
			}
			if (block.getType() == mat && block.getData() == damage)
				continue;
			amo++;
		}
		if (amo >= plugin.getConfig().getInt("Max.Fill.HardLimit")) {
			MSG.tell(sender,
					MSG.getString("Unable.FillLimit", "warning this will affect %blocks% blocks")
							.replace("%blocks%", amo + "")
							.replace("%max%", plugin.getConfig().getInt("Max.Fill.HardLimit") + ""));
			return true;
		}
		if (amo >= plugin.getConfig().getInt("Max.Fill.Confirm") && (sender instanceof Player)
				&& !PlayerManager.getBoolean(((Player) sender), "confirmed")) {
			MSG.tell(sender, MSG.getString("Warning.LargeChange", "warning this will affect %blocks% blocks")
					.replace("%blocks%", amo + ""));

			MSG.tell(sender, MSG.getString("Warning.Confirm", "type /confirm to confirm this action"));
			String all = label + " ";
			for (String res : args)
				all = all + res + " ";
			PlayerManager.setInfo((Player) sender, "confirmCommand", all);
			return true;
		}

		for (Block block : cube) {
			if (replace != null) {
				if (replace != block.getType())
					continue;
				if (block.getData() != replaceDamage)
					continue;
			}
			if (block.getType() == mat && block.getData() == damage)
				continue;
			block.setType(mat);
			block.setData(damage);
		}
		MSG.tell(sender, MSG.getString("World.Filled", "%amo% block%s% filled").replace("%amo%", amo + "")
				.replace("%s%", amo == 1 ? "" : "s"));
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		if (!(sender instanceof Player))
			return result;
		Player player = (Player) sender;

		Block block = player.getTargetBlock((Set<Material>) null, 100);

		if (args.length == 1 || args.length == 4)
			result.add(block.getX() + "");
		if (args.length == 2 || args.length == 5)
			result.add(block.getY() + "");
		if (args.length == 3 || args.length == 6)
			result.add(block.getZ() + "");
		if (args.length == 7 || args.length == 9) {
			for (Material mat : Material.values()) {
				if (!mat.isBlock())
					continue;
				if (mat.toString().toLowerCase().startsWith(args[args.length - 1].toLowerCase())
						&& result.size() < 10) {
					result.add(mat.toString().replace(" ", "_").toLowerCase());
				}
			}
		}
		return result;
	}

	private double getTilde(String entry, Location loc, int xyz) {
		double val = 0;
		if (entry.startsWith("~")) {
			val = xyz == 0 ? loc.getX() : xyz == 1 ? loc.getY() : loc.getZ();
		}
		if (!entry.startsWith("~") || entry.length() > 1)
			val += Float.parseFloat(entry.startsWith("~") ? entry.substring(1) : entry);
		return val;
	}

}
