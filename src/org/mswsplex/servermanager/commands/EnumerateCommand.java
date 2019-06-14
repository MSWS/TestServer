package org.mswsplex.servermanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;

public class EnumerateCommand implements CommandExecutor {

	/**
	 * Permission: manage.command.confirm
	 * 
	 * @param plugin
	 */
	public EnumerateCommand(ServerManager plugin) {
		PluginCommand cmd = plugin.getCommand("enumerate");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.enumerate");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			MSG.tell(sender, "Unknown Player.");
			return true;
		}

		Inventory inv = target.getOpenInventory().getTopInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (item == null || item.getType() == Material.AIR) {
				item = new ItemStack(Material.STAINED_GLASS_PANE, i, (short) 8);
				inv.setItem(i, item);
			}
			item.setAmount(i);
		}

		MSG.tell(sender, "Enumerated " + target.getName() + "'"
				+ (target.getName().toLowerCase().endsWith("s") ? "" : "s") + " inventory.");

		return true;
	}
}
