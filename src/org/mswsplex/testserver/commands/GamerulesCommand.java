package org.mswsplex.testserver.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.MSG;
import org.mswsplex.testserver.utils.Utils;

public class GamerulesCommand implements CommandExecutor,TabCompleter {
	private Main plugin;

	public GamerulesCommand(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommand("gamerules").setExecutor(this);
		this.plugin.getCommand("gamerules").setTabCompleter(this);
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
		if(args.length>1)
			return result;
		for(World world:Bukkit.getWorlds()) {
			if(world.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				result.add(world.getName());
		}
		return result;
	}
}
