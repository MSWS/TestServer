package org.mswsplex.servermanager.events;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.NBTEditor;
import org.mswsplex.servermanager.utils.Utils;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class Events implements Listener {
	private ServerManager plugin;

	public Events(ServerManager plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		String openInventory = PlayerManager.getString(player, "openInventory");
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR)
			return;
		if (openInventory == null)
			return;
		if (openInventory.equals("gameruleViewer")) {
			event.setCancelled(true);
			if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
				return;
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));

			String rule = ChatColor.stripColor(item.getItemMeta().getDisplayName()),
					value = world.getGameRuleValue(rule);

			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				player.playSound(player.getLocation(), Sound.CLICK, 1, value.equalsIgnoreCase("true") ? 1.5f : 2);
			}
			if (value.equalsIgnoreCase("true")) {
				world.setGameRuleValue(rule, "False");
			} else if (value.equalsIgnoreCase("false")) {
				world.setGameRuleValue(rule, "True");
			} else if (plugin.getMultiverseCore() != null) {
				MultiverseCore mv = plugin.getMultiverseCore();
				MultiverseWorld mw = mv.getMVWorldManager().getMVWorld(world);
				String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()), soundType = "iterate";
				switch (name.toLowerCase()) {
				case "hunger":
					mw.setHunger(!mw.getHunger());
					soundType = mw.getHunger() ? "enable" : "disable";
					break;
				case "flight enabled":
					mw.setAllowFlight(!mw.getAllowFlight());
					soundType = mw.getAllowFlight() ? "enable" : "disable";
					break;
				case "animals":
					mw.setAllowAnimalSpawn(!mw.canAnimalsSpawn());
					soundType = mw.canAnimalsSpawn() ? "enable" : "disable";
					break;
				case "monsters":
					mw.setAllowMonsterSpawn(!mw.canMonstersSpawn());
					soundType = mw.canMonstersSpawn() ? "enable" : "disable";
					break;
				case "difficulty":
					mw.setDifficulty(
							Difficulty.getByValue((mw.getDifficulty().ordinal() + 1) % Difficulty.values().length));
					break;
				case "visible":
					mw.setHidden(!mw.isHidden());
					soundType = mw.isHidden() ? "enable" : "disable";
					break;
				case "pvp":
					mw.setPVPMode(!mw.isPVPEnabled());
					soundType = mw.isPVPEnabled() ? "enable" : "disable";
					break;
				case "player limit":
					if (event.getClick() == ClickType.RIGHT) {
						mw.setPlayerLimit(mw.getPlayerLimit() + 1);
					} else if (event.getClick() == ClickType.SHIFT_RIGHT) {
						mw.setPlayerLimit(mw.getPlayerLimit() + 10);
					} else if (event.getClick() == ClickType.LEFT) {
						mw.setPlayerLimit(Math.max(-1, mw.getPlayerLimit() - 1));
					} else if (event.getClick() == ClickType.SHIFT_LEFT) {
						mw.setPlayerLimit(Math.max(-1, mw.getPlayerLimit() - 10));
					}
					break;
				case "gamemode":
					GameMode mode = mw.getGameMode();
					if (mode == GameMode.SPECTATOR) {
						mode = GameMode.SURVIVAL;
					} else if (mode == GameMode.SURVIVAL) {
						mode = GameMode.CREATIVE;
					} else if (mode == GameMode.CREATIVE) {
						mode = GameMode.ADVENTURE;
					} else if (mode == GameMode.ADVENTURE) {
						mode = GameMode.SPECTATOR;
					}
					mw.setGameMode(mode);
					break;
				default:
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .2f);
					return;
				}
				if (soundType.equals("iterate")) {
					player.playSound(player.getLocation(), Sound.CLICK, 1f, 1f);
				} else {
					player.playSound(player.getLocation(), Sound.CLICK, 1,
							soundType.equalsIgnoreCase("enable") ? 2 : 1.5f);
				}
			} else {
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .2f);
				return;
			}
			player.openInventory(Utils.getGameruleGUI(player, world));
			PlayerManager.setInfo(player, "openInventory", "gameruleViewer");
		}
		if (openInventory.equals("professionSelector")) {
			event.setCancelled(true);
			if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
				return;
			String uuid = PlayerManager.getString(player, "managingEntity");
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
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
				player.closeInventory();
				return;
			}
			Profession prof = Profession
					.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName().toUpperCase()));
			Villager villager = (Villager) ent;
			villager.setProfession(prof);
			PlayerManager.setInfo(player, "ignoreClose", true);
			player.openInventory(Utils.getProfessionSelectionGUI(prof));
			player.playSound(player.getLocation(), Sound.DOOR_CLOSE, 2, 2);
			PlayerManager.setInfo(player, "openInventory", "professionSelector");

		}
		if (openInventory.equals("colorSelector")) {
			event.setCancelled(true);
			if (item.getType() != Material.WOOL) {
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
				return;
			}
			if (!item.hasItemMeta())
				return;
			String uuid = PlayerManager.getString(player, "managingEntity");
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
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
				player.closeInventory();
				return;
			}
			int id = item.getDurability();
			((Sheep) ent).setColor(DyeColor.values()[id]);
			PlayerManager.setInfo(player, "ignoreClose", true);
			player.openInventory(Utils.getWoolSelectionGUI(DyeColor.values()[id]));
			// player.openInventory(
			// Utils.getEntityManagerGUI(player, ent,
			// ChatColor.stripColor(Utils.getCustomName(ent))));
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 2, 2);
			PlayerManager.setInfo(player, "openInventory", "colorSelector");
		}
		if (openInventory.equals("entityManager")) {
			event.setCancelled(true);
			if (!item.hasItemMeta())
				return;
			String uuid = PlayerManager.getString(player, "managingEntity");
			World world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
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
				player.closeInventory();
				return;
			}
			String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
			Sound sound = Sound.CLICK;
			Ageable age = null;
			int diff;
			if (ent instanceof Ageable)
				age = (Ageable) ent;
			boolean reopen = true, close = false;
			switch (name.toLowerCase()) {
			case "teleport to":
				PlayerManager.setInfo(player, "inventoryOnClose", null);
				player.teleport(ent.getLocation());
				reopen = false;
				break;
			case "teleport to you":
				ent.teleport(player.getLocation());
				sound = Sound.ENDERMAN_TELEPORT;
				reopen = false;
				break;
			case "kill":
				if (ent instanceof Player || (ent instanceof LivingEntity && ent.getLocation().getChunk().isLoaded())) {
					((LivingEntity) ent).setHealth(0);
				} else {
					ent.remove();
				}
				sound = Sound.GHAST_DEATH;
				reopen = false;
				close = true;
				break;
			case "make baby":
				age.setBaby();
				sound = Sound.GHAST_SCREAM;
				break;
			case "make adult":
				age.setAdult();
				sound = Sound.VILLAGER_HAGGLE;
				break;
			case "modify health":
				diff = 0;
				sound = Sound.BURP;
				switch (event.getClick()) {
				case SHIFT_LEFT:
					diff = -10;
					break;
				case SHIFT_RIGHT:
					diff = 10;
					break;
				case LEFT:
					diff = -1;
					break;
				case RIGHT:
					diff = 1;
					break;
				default:
					break;
				}
				((LivingEntity) ent).setHealth(Math.min(Math.max(((LivingEntity) ent).getHealth() + diff, 0),
						((LivingEntity) ent).getMaxHealth()));
				if (ent.isDead())
					close = true;
				break;
			case "change wool color":
				PlayerManager.setInfo(player, "ignoreClose", true);
				Sheep sheep = (Sheep) ent;
				player.openInventory(Utils.getWoolSelectionGUI(sheep.getColor()));
				close = false;
				reopen = false;
				sound = Sound.DIG_WOOL;
				PlayerManager.setInfo(player, "openInventory", "colorSelector");
				PlayerManager.setInfo(player, "inventoryOnClose", "entityManager");
				break;
			case "toggle ai":
				Object ai = NBTEditor.getEntityTag(ent, "NoAI");
				NBTEditor.setEntityTag(ent, (ai == null || (byte) ai == 0) ? 1 : 0, "NoAI");
				sound = Sound.ZOMBIE_REMEDY;
				break;
			case "modify size":
				sound = Sound.SLIME_WALK;
				Slime slime = (Slime) ent;
				diff = 0;
				switch (event.getClick()) {
				case SHIFT_LEFT:
					diff = -10;
					break;
				case SHIFT_RIGHT:
					diff = 10;
					break;
				case LEFT:
					diff = -1;
					break;
				case RIGHT:
					diff = 1;
					break;
				default:
					break;
				}
				slime.setSize(Math.max(slime.getSize() + diff, 1));
				break;
			case "modify profession":
				PlayerManager.setInfo(player, "ignoreClose", true);
				Villager villager = (Villager) ent;
				player.openInventory(Utils.getProfessionSelectionGUI(villager.getProfession()));
				close = false;
				reopen = false;
				sound = Sound.ANVIL_USE;
				PlayerManager.setInfo(player, "openInventory", "professionSelector");
				PlayerManager.setInfo(player, "inventoryOnClose", "entityManager");
				break;
			case "toggle powered":
				Creeper creeper = (Creeper) ent;
				creeper.setPowered(!creeper.isPowered());
				sound = Sound.EXPLODE;
				break;
			case "toggle skeleton type":
				Skeleton skeleton = (Skeleton) ent;
				skeleton.setSkeletonType(SkeletonType.values()[(skeleton.getSkeletonType().ordinal() + 1)
						% SkeletonType.values().length]);
				sound = Sound.SKELETON_DEATH;
				break;
			case "remove target":
				Creature creature = (Creature) ent;
				creature.setTarget(null);
				sound = Sound.GLASS;
				break;
			default:
				sound = Sound.ITEM_BREAK;
				break;
			}
			player.playSound(player.getLocation(), sound, 1, 2);
			if (reopen && !close) {
				PlayerManager.setInfo(player, "ignoreClose", true);
				player.openInventory(
						Utils.getEntityManagerGUI(player, ent, ChatColor.stripColor(Utils.getCustomName(ent))));
				PlayerManager.setInfo(player, "openInventory", "entityManager");
			}
			if (close)
				player.closeInventory();
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
			if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
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
			} else if (event.getClick() == ClickType.RIGHT) {
				PlayerManager.setInfo(player, "managingEntity", ent.getUniqueId().toString());
				player.openInventory(Utils.getEntityManagerGUI(player, ent,
						ChatColor.stripColor(item.getItemMeta().getDisplayName())));
				player.playSound(player.getLocation(), Sound.CLICK, 2, 1);
				PlayerManager.setInfo(player, "openInventory", "entityManager");
				PlayerManager.setInfo(player, "inventoryOnClose", "entityViewer");
				return;
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
			if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
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
					player.playSound(player.getLocation(), Sound.ITEM_BREAK, .5f, .2f);
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

		String openInventory = PlayerManager.getString(player, "openInventory"),
				nextInventory = PlayerManager.getString(player, "inventoryOnClose");

		if (nextInventory != null) {
			// Should we ignore this time when the player "closes" the inventory?
			if (PlayerManager.getInfo(player, "ignoreClose") != null) {
				PlayerManager.setInfo(player, "ignoreClose", null);
				return;
			}
			Inventory inv = null;
			World world;
			PlayerManager.setInfo(player, "inventoryOnClose", null);
			switch (nextInventory) {
			case "entityManager":
				String uuid = PlayerManager.getString(player, "managingEntity");
				world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
				Entity ent = null;
				for (Entity e : world.getEntities()) {
					if (e.getUniqueId().toString().equals(uuid)) {
						ent = e;
						break;
					}
				}
				inv = Utils.getEntityManagerGUI(player, ent, ChatColor.stripColor(Utils.getCustomName(ent)));
				if (PlayerManager.getInfo(player, "openedEntityViewer") != null)
					PlayerManager.setInfo(player, "inventoryOnClose", "entityViewer");
				break;
			case "entityViewer":
				world = Bukkit.getWorld(PlayerManager.getString(player, "managingWorld"));
				inv = Utils.getEntityViewerGUI(player, world);
				break;
			}
			Inventory fInv = inv;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				player.openInventory(fInv);
				PlayerManager.setInfo(player, "openInventory", nextInventory);
			}, 1);
			return;
		}
		if (openInventory == null)
			return;
		PlayerManager.setInfo(player, "openInventory", null);
	}

	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		Entity ent = event.getRightClicked();
		if (ent == null)
			return;
		if (!player.isSneaking() || !player.hasPermission("manage.managebyclick"))
			return;
		PlayerManager.setInfo(player, "openedEntityViewer", null);
		player.playSound(player.getLocation(), Sound.SILVERFISH_HIT, 2, 1);
		event.setCancelled(true);

		// If the entity that was clicked was a complexentitypart, set entity to its
		// parent
		// (this may happen for an enderdragon for example)
		if (ent instanceof ComplexEntityPart) {
			ComplexEntityPart cpart = (ComplexEntityPart) ent;
			ent = (Entity) cpart.getParent();
		}

		final Entity e = ent;

		if (ent instanceof Villager) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				player.closeInventory();
				PlayerManager.setInfo(player, "managingEntity", e.getUniqueId() + "");
				PlayerManager.setInfo(player, "openInventory", "entityManager");
				PlayerManager.setInfo(player, "managingWorld", player.getWorld().getName() + "");

				player.openInventory(Utils.getEntityManagerGUI(player, event.getRightClicked(),
						ChatColor.stripColor(Utils.getCustomName(event.getRightClicked()))));
			}, 1);
		} else {
			PlayerManager.setInfo(player, "managingEntity", e.getUniqueId() + "");
			PlayerManager.setInfo(player, "openInventory", "entityManager");
			PlayerManager.setInfo(player, "managingWorld", player.getWorld().getName() + "");

			player.openInventory(Utils.getEntityManagerGUI(player, e, ChatColor.stripColor(Utils.getCustomName(e))));
		}
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
					.divide(new Vector(size / 2.5, size / 2.5, size / 2.5)).setY(size / 10)));
			lent.getWorld().playSound(lent.getLocation(), Sound.CHICKEN_EGG_POP, 2, .1f);
		}
	}

}
