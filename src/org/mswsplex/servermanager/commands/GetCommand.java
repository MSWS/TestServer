package org.mswsplex.servermanager.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.customitems.CustomItem;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;

public class GetCommand implements CommandExecutor, TabCompleter {

	private ServerManager plugin;

	/**
	 * Permission: manage.command.confirm
	 * 
	 * @param plugin
	 */
	public GetCommand(ServerManager plugin) {
		this.plugin = plugin;
		PluginCommand cmd = plugin.getCommand("get");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.get");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;

		if (args.length < 1) {
			MSG.tell(sender, "/get [item]");
			return true;
		}

		CustomItem item = plugin.getItem(args[0]);
		if (item == null) {
			MSG.tell(sender, "Unknown item.");
			return true;
		}

		player.setItemInHand(item.getStack());

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<String>();

		if (args.length <= 1) {
			for (String key : plugin.getItems().keySet()) {
				if (key.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(key);
			}
		}

		return result;
	}
}
