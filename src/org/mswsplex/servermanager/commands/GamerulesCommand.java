package org.mswsplex.servermanager.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.Utils;

public class GamerulesCommand implements CommandExecutor, TabCompleter {

	/**
	 * Permission: manage.command.gamerules
	 * 
	 * @param plugin
	 */
	public GamerulesCommand(ServerManager plugin) {
		PluginCommand cmd = plugin.getCommand("gamerules");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.gamerules");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		World world = player.getWorld();
		if (args.length > 0) {
			world = Bukkit.getWorld(args[0]);
		}
		if (world == null) {
			MSG.tell(player, MSG.getString("Unable.Gamerule", "Unable to retrieve gamerules, reason: %reason%")
					.replace("%reason%", "World Unloaded"));
			return true;
		}
		player.openInventory(Utils.getGameruleGUI(player, world));
		PlayerManager.setInfo(player, "openInventory", "gameruleViewer");
		PlayerManager.setInfo(player, "managingWorld", world.getName());
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		if (args.length > 1)
			return result;
		for (World world : Bukkit.getWorlds()) {
			if (world.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(world.getName());
		}
		return result;
	}
}
