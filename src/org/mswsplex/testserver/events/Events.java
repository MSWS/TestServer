package org.mswsplex.testserver.events;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;
import org.mswsplex.testserver.utils.MSG;
import org.mswsplex.testserver.utils.Utils;

public class Events implements Listener {
	private Main plugin;

	public Events(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		String openInventory = PlayerManager.getString(player, "openInventory");
		ItemStack item = event.getCurrentItem();

		if (openInventory == null)
			return;

		if (openInventory.equals("worldViewer")) {
			event.setCancelled(true);
			if (item == null || item.getType() == Material.AIR)
				return;
			World world = Bukkit.getWorld(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
			String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
//			if (world == null) {
//				player.closeInventory();
//				MSG.tell(player, "&cThere was an error processing your request.");
//				return;
//			}

			if (event.getClick() == ClickType.SHIFT_LEFT) {
				if (world.getPlayers().size() > 0) {
					MSG.tell(player, MSG.getString("Unable.Unload", "Unable to unload world, reason: %reason%")
							.replace("%reason%", "Players in world"));
					return;
				}
				MSG.tell(player, MSG.getString("World.Unloading", "unloading %world%").replace("%world%", name));
				Bukkit.unloadWorld(world, false);
				MSG.tell(player, MSG.getString("World.Unloaded", "unloaded %world%").replace("%world%", name));
			}
			if (event.getClick() == ClickType.LEFT) {
				if (world == null) {
					player.closeInventory();
					MSG.tell(player, MSG.getString("World.Loading", "loading %world%").replace("%world%", name));
					MSG.tell(player, MSG.getString("Warning.Time", "this may take some time"));
					world = Bukkit.createWorld(WorldCreator.name(name));
					MSG.tell(player, MSG.getString("World.Loaded", "loaded %world%").replace("%world%", name));
					player.openInventory(Utils.getWorldViewerGUI(player));
					PlayerManager.setInfo(player, "openInventory", "worldViewer");
					PlayerManager.setInfo(player, "page", 0);
					return;
				}
				if (player.getWorld().equals(world)) {
					MSG.tell(player, "&cYou are already in this world.");
					return;
				}
				player.closeInventory();
				player.teleport(world.getSpawnLocation());
				return;
			}
			if (event.getClick() == ClickType.SHIFT_RIGHT) {
				File worldFolder = new File(Bukkit.getWorldContainer().toPath() + File.separator + name);
				if (world != null) {
					if (world.getPlayers().size() > 0) {
						MSG.tell(player, MSG.getString("Unable.Delete", "Unable to delete world, reason: %reason%")
								.replace("%reason%", "Players in world"));
						return;
					}
					MSG.tell(player, MSG.getString("World.Unloading", "unloading %world%").replace("%world%", name));
					Bukkit.unloadWorld(world, false);
				}
				MSG.tell(player, MSG.getString("World.Deleting", "deleting %world%").replace("%world%", name));
				Utils.deleteWorld(worldFolder);
				MSG.tell(player, MSG.getString("World.Deleted", "deleted %world%").replace("%world%", name));
			}
			if (event.getClick() == ClickType.DROP) {
				if (world == null || world.getPlayers().size() == 0) {
					MSG.tell(player, MSG.getString("Unable.Kick", "Unable to kick players, reason: %reason%")
							.replace("%reason%", "No Players in World"));
					return;
				}
				if (world.equals(player.getWorld())) {
					MSG.tell(player, MSG.getString("Unable.Kick", "Unable to kick players, reason: %reason%")
							.replace("%reason%", "You must be in a different world"));
					return;
				}
				int amo = world.getPlayers().size();
				for (Player target : world.getPlayers())
					target.teleport(player.getWorld().getSpawnLocation());
				MSG.tell(player, MSG.getString("World.Kicked", "kicked %amo% player%s%").replace("%amo%", amo + "")
						.replace("%s%", amo == 1 ? "" : "s"));
				return;
			}
			player.openInventory(Utils.getWorldViewerGUI(player));
			PlayerManager.setInfo(player, "openInventory", "worldViewer");
			PlayerManager.setInfo(player, "page", 0);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		String openInventory = PlayerManager.getString(player, "openInventory");
		if (openInventory == null)
			return;
		if (openInventory.equals("worldViewer"))
			PlayerManager.setInfo(player, "openInventory", null);
	}

	@EventHandler
	public void onCommandSent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			PlayerManager.setInfo(player, "confirmed", null);
		}, 1);
	}
}
