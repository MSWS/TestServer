package org.mswsplex.servermanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;

public class ConfirmCommand implements CommandExecutor {

	/**
	 * Permission: manage.command.confirm
	 * 
	 * @param plugin
	 */
	public ConfirmCommand(ServerManager plugin) {
		PluginCommand cmd = plugin.getCommand("confirm");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.confirm");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		String cmd = PlayerManager.getString(player, "confirmCommand");
		if (PlayerManager.getString(player, "confirmCommand") == null) {
			MSG.tell(sender, MSG.getString("Unknown.Confirm", "not possible"));
			return true;
		}
		PlayerManager.setInfo(player, "confirmed", true);
		player.performCommand(cmd);
		return true;
	}
}
