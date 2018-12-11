package org.mswsplex.testserver.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.MSG;

public class ForcefieldCommand implements CommandExecutor {
	private Main plugin;

	public ForcefieldCommand(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommand("forcefield").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		if (args.length == 0) {
			if (PlayerManager.getInfo(player, "forcefield") == null) {
				MSG.tell(sender, MSG.getString("Forcefield.Info.Disabled", "forcefield is disabled"));
			} else {
				MSG.tell(sender, MSG.getString("Forcefield.Info.Active", "your forcefield is at %size% block%s%")
						.replace("%size%", PlayerManager.getString(player, "forcefield")).replace("%s%",
								Float.parseFloat(PlayerManager.getString(player, "forcefield")) == 1 ? "" : "s"));
			}
			return true;
		}
		float size = 0;
		try {
			size = Float.parseFloat(args[0]);
		} catch (Exception e) {
			MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
			return true;
		}

		if (size <= 0) {
			if (PlayerManager.getInfo(player, "forcefield") == null) {
				MSG.tell(sender, MSG.getString("Forcefield.Missing", "unable to disable forcefield"));
			} else {
				PlayerManager.setInfo(player, "forcefield", null);
				MSG.tell(sender, MSG.getString("Forcefield.Disabled", "you removed your forcefield"));
			}
			return true;
		}

		PlayerManager.setInfo(player, "forcefield", (float) size);
		MSG.tell(sender, MSG.getString("Forcefield.Enabled", "your forcefield is now set to %size% block%s%")
				.replace("%size%", size + "").replace("%s%", size == 1 ? "" : "s"));

		return true;
	}
}
