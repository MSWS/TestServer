package org.mswsplex.servermanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;

public class ForcefieldCommand implements CommandExecutor {
	private ServerManager plugin;

	/**
	 * Permission: manage.command.forcefield
	 * 
	 * @param plugin
	 */
	public ForcefieldCommand(ServerManager plugin) {
		this.plugin = plugin;
		PluginCommand cmd = plugin.getCommand("forcefield");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.forcefield");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	// /forcefield <Player> [Size]
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player)
			player = (Player) sender;
		if (args.length == 0 && player == null) {
			MSG.tell(sender, "You must specify a player as console");
			return true;
		}
		float size = 0;
		if (args.length > 0 && Bukkit.getPlayer(args[0]) != null
				&& sender.hasPermission("manage.command.forcefield.others")) {
			player = (Player) Bukkit.getPlayer(args[0]);
		}

		if (args.length == 0 || (args.length == 1 && Bukkit.getPlayer(args[0]) != null)) {
			if (player == sender) {
				if (PlayerManager.getInfo(player, "forcefield") == null) {
					MSG.tell(sender, MSG.getString("Forcefield.Info.Inactive.Self", "forcefield is inactive"));
				} else {
					MSG.tell(sender, MSG
							.getString("Forcefield.Info.Active.Self", "your forcefield is at %size% block%s%")
							.replace("%size%", PlayerManager.getString(player, "forcefield")).replace("%s%",
									Float.parseFloat(PlayerManager.getString(player, "forcefield")) == 1 ? "" : "s"));
				}
			} else {
				if (PlayerManager.getInfo(player, "forcefield") == null) {
					MSG.tell(sender,
							MSG.getString("Forcefield.Info.Inactive.Others", "%player%'%s% forcefield is inactive")
									.replace("%player%", player.getName())
									.replace("%s%", player.getName().toLowerCase().endsWith("s") ? "" : "s"));
				} else {
					MSG.tell(sender, MSG
							.getString("Forcefield.Info.Active.Others",
									"%player%'%ns% forcefield is at %size% block%s%")
							.replace("%size%", PlayerManager.getString(player, "forcefield"))
							.replace("%s%",
									Float.parseFloat(PlayerManager.getString(player, "forcefield")) == 1 ? "" : "s")
							.replace("%player%", player.getName())
							.replace("%ns%", player.getName().toLowerCase().endsWith("s") ? "" : "s"));
				}
			}
			return true;
		}

		String numberArg = args[0];
		if (args.length > 1 && sender.hasPermission("manage.command.forcefield.others")) {
			numberArg = args[1];
		}

		try {
			size = Float.parseFloat(numberArg);
		} catch (Exception e) {
			MSG.tell(sender, MSG.getString("Unknown.Number", "unknown number"));
			return true;
		}

		if (size >= plugin.getConfig().getInt("Max.ForcefieldRadius.HardLimit")) {
			MSG.tell(sender,
					MSG.getString("Unable.ForcefieldLimit", "size is too big %size% blocks")
							.replace("%size%", size + "")
							.replace("%max%", plugin.getConfig().getInt("Max.ForcefieldRadius.HardLimit") + ""));
			return true;
		}
		if (size >= plugin.getConfig().getInt("Max.ForcefieldRadius.Confirm") && (sender instanceof Player)
				&& !PlayerManager.getBoolean(((Player) sender), "confirmed")) {
			MSG.tell(sender, MSG.getString("Warning.LargeSize", "size is %size%").replace("%size%", size + ""));

			MSG.tell(sender, MSG.getString("Warning.Confirm", "type /confirm to confirm this action"));
			String all = label + " ";
			for (String res : args)
				all = all + res + " ";
			PlayerManager.setInfo((Player) sender, "confirmCommand", all);
			return true;
		}

		if (size <= 0) {
			if (PlayerManager.getInfo(player, "forcefield") == null) {
				if (sender == player) {
					MSG.tell(sender, MSG.getString("Forcefield.Missing.Self", "you do not have an active forcefield"));
				} else {
					MSG.tell(sender,
							MSG.getString("Forcefield.Missing.Others", "%target% does not have an active forcefield")
									.replace("%target%", player.getName())
									.replace("%s%", player.getName().toLowerCase().endsWith("s") ? "" : "s"));
				}
			} else {
				if (sender == player) {
					MSG.tell(sender, MSG.getString("Forcefield.Disabled.Self", "you removed your forcefield"));
				} else {
					MSG.tell(sender,
							MSG.getString("Forcefield.Disabled.Sender", "you removed %target%'%s% forcefield")
									.replace("%target%", player.getName())
									.replace("%s%", player.getName().toLowerCase().endsWith("s") ? "" : "s"));
					MSG.tell(player, MSG.getString("Forcefield.Disabled.Receiver", "%sender% remove dyour forcefield")
							.replace("%sender%", sender.getName()));
				}
				PlayerManager.setInfo(player, "forcefield", null);
			}
			return true;
		}

		PlayerManager.setInfo(player, "forcefield", (float) size);
		if (sender == player) {
			MSG.tell(sender, MSG.getString("Forcefield.Enabled.Self", "your forcefield is now set to %size% block%s%")
					.replace("%size%", size + "").replace("%s%", size == 1 ? "" : "s"));
		} else {
			MSG.tell(sender,
					MSG.getString("Forcefield.Enabled.Sender", "you set %target%'%ns% forcefield to %size% block%s%")
							.replace("%size%", size + "").replace("%s%", size == 1 ? "" : "s")
							.replace("%target%", player.getName())
							.replace("%ns%", player.getName().toLowerCase().endsWith("s") ? "" : "s"));
			MSG.tell(player,
					MSG.getString("Forcefield.Enabled.Receiver", "%sender% set your forcefield to %size% block %s%")
							.replace("%sender%", sender.getName()).replace("%size%", size + "")
							.replace("%s%", size == 1 ? "" : "s"));
		}
		return true;
	}
}
