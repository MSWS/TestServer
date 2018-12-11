package org.mswsplex.testserver.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.msws.Main;

public class MSG {
	public static Main plugin;

	/**
	 * Returns the string with &'s being �
	 * 
	 * @param msg the message to replace
	 * @return returns colored msg
	 */
	public static String color(String msg) {
		if (msg == null || msg.isEmpty())
			return null;
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	/**
	 * Returns string with camel case, and with _'s replaced with spaces
	 * 
	 * @param string hello_how is everyone
	 * @return Hello How Is Everyone
	 */
	public static String camelCase(String string) {
		String prevChar = " ";
		String res = "";
		for (int i = 0; i < string.length(); i++) {
			if (i > 0)
				prevChar = string.charAt(i - 1) + "";
			if (!prevChar.matches("[a-zA-Z]")) {
				res = res + ((string.charAt(i) + "").toUpperCase());
			} else {
				res = res + ((string.charAt(i) + "").toLowerCase());
			}
		}
		return res.replace("_", " ");
	}

	/**
	 * Gets a string from lang.yml
	 * 
	 * @param id  key id of the string to get
	 * @param def default string in case lang.yml doesn't have the key
	 * @return
	 */
	public static String getString(String id, String def) {
		return plugin.lang.contains(id) ? plugin.lang.getString(id) : "[" + id + "] " + def;
	}

	/**
	 * Sends a message to a CommandSender, colored and with %prefix% replaced
	 * 
	 * @param sender CommandSender to send message to
	 * @param msg    Message to send
	 */
	public static void tell(CommandSender sender, String msg) {
		if (msg != null && !msg.isEmpty())
			sender.sendMessage(color(msg.replace("%prefix%", prefix())));
	}

	/**
	 * Sends a message to everyone in a world
	 * 
	 * @param world World to send message to
	 * @param msg   Message to send
	 */
	public static void tell(World world, String msg) {
		if (world != null && msg != null) {
			for (Player target : world.getPlayers()) {
				tell(target, msg);
			}
		}
	}

	/**
	 * Sends a message to all players with a specific permission
	 * 
	 * @param perm Permission to require
	 * @param msg  Message to send
	 */
	public static void tell(String perm, String msg) {
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.hasPermission(perm))
				tell(target, msg);
		}
	}

	/**
	 * Announces a message to all players
	 * 
	 * @param msg Message to announce
	 */
	public static void announce(String msg) {
		Bukkit.getOnlinePlayers().forEach((player) -> {
			tell(player, msg);
		});
	}

	/**
	 * Gets the prefix defined in config
	 * 
	 * @return the prefix
	 */
	public static String prefix() {
		return plugin.config.contains("Prefix") ? plugin.config.getString("Prefix") : "&9Plugin>&7";
	}

	/**
	 * Sends a no permission message to the target
	 * 
	 * @param sender CommandSender to send message to
	 */
	public static void noPerm(CommandSender sender) {
		tell(sender, getString("NoPermission", "Insufficient Permissions"));
	}

	/**
	 * Logs a message to console
	 * 
	 * @param msg Message to log
	 */
	public static void log(String msg) {
		Bukkit.getLogger().info(MSG.color(msg));
	}

	/**
	 * Logs a message to console with the specified level
	 * 
	 * @param level Level to log message with
	 * @param msg   Message to log
	 */
	public static void log(Level level, String msg) {
		Bukkit.getLogger().log(level, MSG.color(msg));
	}

	/**
	 * Colored boolean
	 * 
	 * @param bool true/false
	 * @return Green True or Red False
	 */
	public static String TorF(Boolean bool) {
		if (bool) {
			return "&aTrue&r";
		} else {
			return "&cFalse&r";
		}
	}

	/**
	 * Sends a help message with specified pages
	 * 
	 * @param sender  CommandSender to send message to
	 * @param page    Page of help messages
	 * @param command Command to send help to
	 */
	public static void sendHelp(CommandSender sender, int page, String command) {
		if (!plugin.lang.contains("Help." + command.toLowerCase())) {
			tell(sender, getString("UnknownCommand", "There is no help available for this command."));
			return;
		}
		int length = plugin.config.getInt("HelpLength");
		List<String> help = plugin.lang.getStringList("Help." + command.toLowerCase()), list = new ArrayList<String>();
		for (String res : help) {
			if (res.startsWith("perm:")) {
				String perm = "";
				res = res.substring(5, res.length());
				for (char a : res.toCharArray()) {
					if (a == ' ')
						break;
					perm = perm + a;
				}
				if (!sender.hasPermission("plugin." + perm)) // TODO
					continue;
				res = res.replace(perm + " ", "");
			}
			list.add(res);
		}
		if (help.size() > length)
			tell(sender, "Page: " + (page + 1) + " of " + (int) Math.ceil((list.size() / length) + 1));
		for (int i = page * length; i < list.size() && i < page * length + length; i++) {
			String res = list.get(i);
			tell(sender, res);
		}
		if (command.equals("default")) // TODO
			tell(sender, "&d&lPlugin &ev" + plugin.getDescription().getVersion() + " &7created by &bMSWS");
	}

	/**
	 * Returns a text progress bar
	 * 
	 * @param prog   0-total double value of progress
	 * @param total  Max amount that progress bar should represent
	 * @param length Length in chars for progress bar
	 * @return
	 */
	public static String progressBar(double prog, double total, int length) {
		return progressBar("&a\u258D", "&c\u258D", prog, total, length);
	}

	/**
	 * Returns a text progress bar with specified chars
	 * 
	 * @param progChar   Progress string to represent progress
	 * @param incomplete Incomplete string to represent amount left
	 * @param prog       0-total double value of progress
	 * @param total      Max amount that progress bar should represent
	 * @param length     Length in chars for progress bar
	 * @return
	 */
	public static String progressBar(String progChar, String incomplete, double prog, double total, int length) {
		String disp = "";
		double progress = Math.abs(prog / total);
		int len = length;
		for (double i = 0; i < len; i++) {
			if (i / len < progress) {
				disp = disp + progChar;
			} else {
				disp = disp + incomplete;
			}
		}
		return color(disp);
	}

	/**
	 * Returns a string for shortened decimal
	 * 
	 * @param decimal Decimal to shorten
	 * @param length  Amount of characters after the .
	 * @return Input: "5978.154123" (Length of 3) Output: "5978.154"
	 */
	public static String parseDecimal(String decimal, int length) {
		if (decimal.contains(".")) {
			if (decimal.split("\\.")[1].length() > 2) {
				decimal = decimal.split("\\.")[0] + "."
						+ decimal.split("\\.")[1].substring(0, Math.min(decimal.split("\\.")[1].length(), length));
			}
		}
		return decimal;
	}

	/**
	 * Returns a string for shortened decimal
	 * 
	 * @param decimal Decimal to shorten
	 * @param length  Amount of characters after the .
	 * @return Input: 5978.154123 (Length of 3) Output: "5978.154"
	 */
	public static String parseDecimal(double decimal, int length) {
		return parseDecimal(decimal + "", length);
	}

	public static String genUUID(int length) {
		String[] keys = new String[100];
		int pos = 0;
		for (int i = 0; i < 26; i++) {
			keys[i + pos] = ((char) (i + 65)) + "";
		}
		pos += 26;
		for (int i = 0; i < 10; i++) {
			keys[i + pos] = i + "";
		}
		pos += 10;
		String res = "";
		for (int i = 0; i < length; i++)
			res = res + keys[(int) Math.floor(Math.random() * pos)];
		return res;
	}
}
