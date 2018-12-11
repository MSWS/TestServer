package org.mswsplex.testserver.events;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
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
		if (openInventory.equals("gameruleViewer")) {
			event.setCancelled(true);
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
			String rule = ChatColor.stripColor(item.getItemMeta().getDisplayName()),
					value = world.getGameRuleValue(rule);
			if (value.equalsIgnoreCase("true")) {
				world.setGameRuleValue(rule, "False");
			} else if (value.equalsIgnoreCase("false")) {
				world.setGameRuleValue(rule, "True");
			} else {
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
				return;
			}
			player.playSound(player.getLocation(), Sound.CLICK, 1, value.equalsIgnoreCase("true") ? 1.5f : 2);
			player.openInventory(Utils.getGameruleGUI(player, world));
			PlayerManager.setInfo(player, "openInventory", "gameruleViewer");
		}
		if (openInventory.equals("entityViewer")) {
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
			event.setCancelled(true);
			int page = (int) Math.round(PlayerManager.getDouble(player, "page"));
			if (item == null || item.getType() == Material.AIR)
				return;
			if (event.getSlot() == event.getInventory().getSize() - 1 && item.getType() == Material.ARROW) {
				PlayerManager.setInfo(player, "page", page + 1);
				player.openInventory(Utils.getEntityViewerGUI(player, player.getWorld()));
				PlayerManager.setInfo(player, "openInventory", "entityViewer");
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
				return;
			}
			if (event.getSlot() == event.getInventory().getSize() - 9 && item.getType() == Material.ARROW) {
				PlayerManager.setInfo(player, "page", page - 1);
				player.openInventory(Utils.getEntityViewerGUI(player, player.getWorld()));
				PlayerManager.setInfo(player, "openInventory", "entityViewer");
				player.playSound(player.getLocation(), Sound.CLICK, 1, .75f);
				return;
			}
			if(!item.hasItemMeta()||!item.getItemMeta().hasLore())
				return;
			String uuid = ChatColor.stripColor(item.getItemMeta().getLore().get(0));
			Entity ent = null;
			for (Entity e : world.getEntities()) {
				if (e.getUniqueId().toString().equals(uuid)) {
					ent = e;
					break;
				}
			}
			if (ent == null) {
				MSG.tell(player, MSG.getString("Unable.Entity", "Unable to manage that entity, reason: %reason%")
						.replace("%reason%", "Entity not found"));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
				return;
			}
			if (event.getClick() == ClickType.LEFT) {
				player.teleport(ent);
				return;
			}
			if (event.getClick() == ClickType.SHIFT_LEFT) {
				ent.teleport(player);
				return;
			}
			if (event.getClick() == ClickType.RIGHT) {
				if (ent instanceof Player) {
					((LivingEntity) ent).damage(((LivingEntity) ent).getHealth());
				} else {
					ent.remove();
				}
				player.playSound(player.getLocation(), Sound.GHAST_DEATH, 2, 1f);
			}
			if (event.getClick() == ClickType.SHIFT_RIGHT) {
				for (Entity e : world.getEntitiesByClass(ent.getClass())) {
					if (e instanceof Player) {
						((LivingEntity) e).damage(((LivingEntity) e).getHealth());
					} else {
						e.remove();
					}
				}
				player.playSound(player.getLocation(), Sound.BAT_HURT, .5f, .1f);

			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				player.openInventory(Utils.getEntityViewerGUI(player, player.getWorld()));
				PlayerManager.setInfo(player, "openInventory", "entityViewer");
			}, 2);
			return;
		}
		if (openInventory.equals("worldViewer")) {
			event.setCancelled(true);
			int page = (int) Math.round(PlayerManager.getDouble(player, "page"));
			if (item == null || item.getType() == Material.AIR)
				return;
			if (event.getSlot() == event.getInventory().getSize() - 1 && item.getType() == Material.ARROW) {
				PlayerManager.setInfo(player, "page", page + 1);
				player.openInventory(Utils.getWorldViewerGUI(player));
				PlayerManager.setInfo(player, "openInventory", "worldViewer");
				return;
			}
			if (event.getSlot() == event.getInventory().getSize() - 9 && item.getType() == Material.ARROW) {
				PlayerManager.setInfo(player, "page", page - 1);
				player.openInventory(Utils.getWorldViewerGUI(player));
				PlayerManager.setInfo(player, "openInventory", "worldViewer");
				return;
			}
			if(!item.hasItemMeta()||!item.getItemMeta().hasDisplayName())
				return;
			World world = Bukkit.getWorld(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
			String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			if (event.getClick() == ClickType.SHIFT_LEFT) {
				if (world != null && Utils.isPriorityWorld(world)) {
					MSG.tell(player, MSG.getString("Unable.Unload", "Unable to unload world, reason: %reason%")
							.replace("%reason%", "Priority World"));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
					return;
				}
				if (world.getPlayers().size() > 0) {
					MSG.tell(player, MSG.getString("Unable.Unload", "Unable to unload world, reason: %reason%")
							.replace("%reason%", "Players in world"));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
					return;
				}
				MSG.tell(player, MSG.getString("World.Unloading", "unloading %world%").replace("%world%", name));
				Bukkit.unloadWorld(world, false);
				MSG.tell(player, MSG.getString("World.Unloaded", "unloaded %world%").replace("%world%", name));
			}
			if (event.getClick() == ClickType.MIDDLE) {
				if (world == null) {
					MSG.tell(player, MSG.getString("Unable.Gamerule", "Unable to retrieve gamerules, reason: %reason%")
							.replace("%reason%", "World Unloaded"));
					return;
				}
				player.openInventory(Utils.getGameruleGUI(player, world));
				PlayerManager.setInfo(player, "openInventory", "gameruleViewer");
				PlayerManager.setInfo(player, "managingWorld", world.getName());
				return;
			}
			if (event.getClick() == ClickType.LEFT) {
				if (world == null) {
					player.closeInventory();
					MSG.tell(player, MSG.getString("World.Loading", "loading %world%").replace("%world%", name));
					MSG.tell(player, MSG.getString("Warning.Time", "this may take some time"));
					world = Bukkit.createWorld(WorldCreator.name(name));
					MSG.tell(player, MSG.getString("World.Loaded", "loaded %world%").replace("%world%", name));
					PlayerManager.setInfo(player, "page", 0);
					player.openInventory(Utils.getWorldViewerGUI(player));
					PlayerManager.setInfo(player, "openInventory", "worldViewer");
					return;
				}
				if (player.getWorld().equals(world)) {
					MSG.tell(player, MSG.getString("Unable.Teleport", "unable to enter %reason%").replace("%reason%",
							"Already in world"));
					return;
				}
				player.closeInventory();
				player.teleport(world.getSpawnLocation());
				return;
			}
			if (event.getClick() == ClickType.SHIFT_RIGHT) {
				File worldFolder = new File(Bukkit.getWorldContainer().toPath() + File.separator + name);
				if (world != null) {
					if (world != null && Utils.isPriorityWorld(world)) {
						MSG.tell(player, MSG.getString("Unable.Unload", "Unable to unload world, reason: %reason%")
								.replace("%reason%", "Priority World"));
						player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
						return;
					}
					if (world.getPlayers().size() > 0) {
						MSG.tell(player, MSG.getString("Unable.Delete", "Unable to delete world, reason: %reason%")
								.replace("%reason%", "Players in world"));
						player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
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
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
					return;
				}
				if (world.equals(player.getWorld())) {
					MSG.tell(player, MSG.getString("Unable.Kick", "Unable to kick players, reason: %reason%")
							.replace("%reason%", "You must be in a different world"));
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
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
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		String openInventory = PlayerManager.getString(player, "openInventory");
		if (openInventory == null)
			return;
		PlayerManager.setInfo(player, "openInventory", null);
	}

	@EventHandler
	public void onCommandSent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			PlayerManager.setInfo(player, "confirmed", null);
		}, 1);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (PlayerManager.getInfo(player, "forcefield") == null)
			return;
		float size = Float.parseFloat(PlayerManager.getString(player, "forcefield"));
		for (Entity ent : player.getNearbyEntities(size, size, size)) {
			if (!(ent instanceof LivingEntity) || ent instanceof ArmorStand)
				continue;
			LivingEntity lent = (LivingEntity) ent;
			lent.setVelocity((ent.getLocation().toVector().subtract(player.getLocation().toVector())
					.divide(new Vector(size/2.5, size/2.5, size/2.5)).setY(size/10)));
			lent.getWorld().playSound(lent.getLocation(), Sound.CHICKEN_EGG_POP, 2, .1f);
		}
	}
}
