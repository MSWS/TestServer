package org.mswsplex.testserver.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.MSG;

public class ConfirmCommand implements CommandExecutor {
	private Main plugin;

	public ConfirmCommand(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommand("confirm").setExecutor(this);
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
