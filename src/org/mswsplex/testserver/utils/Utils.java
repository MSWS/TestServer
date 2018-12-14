package org.mswsplex.testserver.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.msws.Main;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class Utils {
	public static Main plugin;

	public Utils() {
		MSG.log("utils initialized");
	}

	/**
	 * Returns a ranking of all the armor from value
	 * 
	 * @param mat Material to compare
	 * @return Diamond: 4 Iron: 3 Chain: 2 Gold: 1 Leather: 0 Default: 0
	 */
	public static int getArmorValue(Material mat) {
		switch (getArmorType(mat).toLowerCase()) {
		case "diamond":
			return 4;
		case "iron":
			return 3;
		case "chainmail":
			return 2;
		case "gold":
			return 1;
		case "leather":
			return 0;
		default:
			return 0;
		}
	}

	/**
	 * Gets the armor slot that a type of armor should be in
	 * 
	 * @param type Material type (DIAMOND_CHESTPLATE, IRON_LEGGINGS, etc)
	 * @return Armor slot Helmet: 3 Chestplate: 2 Leggings: 1 Boots: 0
	 */
	public static int getSlot(Material type) {
		if (!type.name().contains("_"))
			return 0;
		switch (type.name().split("_")[1]) {
		case "HELMET":
			return 3;
		case "CHESTPLATE":
			return 2;
		case "LEGGINGS":
			return 1;
		case "BOOTS":
			return 0;
		}
		return 0;
	}

	/**
	 * Returns type of armor
	 * 
	 * @param mat Material to get type of
	 * @return DIAMOND, IRON, GOLD, CHAINMAIL
	 */
	public static String getArmorType(Material mat) {
		if (!mat.name().contains("_")) {
			return "";
		}
		String name = mat.name().split("_")[0];
		return name;
	}

	/**
	 * Returns if the specified material is armor
	 * 
	 * @param mat Material to check
	 * @return True if armor, false otherwise
	 */
	public static boolean isArmor(Material mat) {
		return mat.name().contains("CHESTPLATE") || mat.name().contains("LEGGINGS") || mat.name().contains("HELMET")
				|| mat.name().contains("BOOTS");
	}

	/**
	 * Returns a sound that a block would play if placed/broken
	 * 
	 * @param mat Material to check
	 * @return Sound closest, DIG_GRASS if unmatched
	 */
	public static Sound getBreakSound(Material mat) {
		if (mat.name().contains("GLOW") || mat.name().contains("GLASS"))
			return Sound.GLASS;
		if (mat.name().contains("STONE"))
			return Sound.DIG_STONE;
		if (mat.name().contains("SAND"))
			return Sound.DIG_SAND;
		if (mat.name().contains("SNOW"))
			return Sound.DIG_SNOW;
		if (mat.name().contains("WOOD") || mat.name().contains("LOG"))
			return Sound.DIG_WOOD;
		switch (mat.name()) {
		case "GRAVEL":
			return Sound.DIG_GRAVEL;
		case "GRASS":
		case "DIRT":
			return Sound.DIG_GRASS;
		case "WOOL":
			return Sound.DIG_WOOL;
		default:
			return Sound.DIG_GRASS;
		}
	}

	/**
	 * Gets a block based on the blockface
	 * 
	 * @param block Block to compare face to
	 * @param face  Relative face to get block
	 * @return
	 */
	public static Block blockFromFace(Block block, BlockFace face) {
		int x = 0, y = 0, z = 0;
		if (face == BlockFace.EAST)
			x = 1;
		if (face == BlockFace.WEST)
			x = -1;
		if (face == BlockFace.NORTH)
			z = -1;
		if (face == BlockFace.SOUTH)
			z = 1;
		if (face == BlockFace.UP)
			y = 1;
		if (face == BlockFace.DOWN)
			y = -1;
		return block.getLocation().add(x, y, z).getBlock();
	}

	/**
	 * Returns parsed Inventory from YAML config (guis.yml)
	 * 
	 * @param player Player to parse information with (%player% and other
	 *               placeholders)
	 * @param id     Name of the inventory to parse
	 * @param page   Page of the inventory
	 * @return
	 */
	public static Inventory getGui(OfflinePlayer player, String id, int page) {
		if (!plugin.gui.contains(id))
			return null;
		ConfigurationSection gui = plugin.gui.getConfigurationSection(id);
		if (!gui.contains("Size") || !gui.contains("Title"))
			return null;
		String title = gui.getString("Title").replace("%player%", player.getName());
		if (player.isOnline())
			title = title.replace("%world%", ((Player) player).getWorld().getName());
		title = title.replace("%world%", "");
		Inventory inv = Bukkit.createInventory(null, gui.getInt("Size"), MSG.color(title));
		ItemStack bg = null;
		boolean empty = true;
		for (String res : gui.getKeys(false)) {
			if (!gui.contains(res + ".Icon"))
				continue;
			empty = false;
			if (gui.contains(res + ".Page")) {
				if (page != gui.getInt(res + ".Page"))
					continue;
			} else if (page != 0)
				continue;
			if (player.isOnline()) {
				if (gui.contains(res + ".Permission")
						&& !((Player) player).hasPermission(gui.getString(res + ".Permission"))) {
					continue;
				}
			}
			ItemStack item = parseItem(plugin.gui, id + "." + res, player);
			if (res.equals("BACKGROUND_ITEM")) {
				bg = item;
				continue;
			}
			int slot = 0;
			if (!gui.contains(res + ".Slot")) {
				while (inv.getItem(slot) != null)
					slot++;
				inv.setItem(slot, item);
			} else {
				inv.setItem(gui.getInt(res + ".Slot"), item);
			}
		}
		if (empty)
			return null;
		if (bg != null) {
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
					inv.setItem(i, bg);
				}
			}
		}
		return inv;
	}

	/**
	 * Parses and returns an item from the specified YAML Path Supports
	 * enchantments, damage values, amounts, skulls, lores, and unbreakable
	 * 
	 * @param section Section to get item from
	 * @param path    Specified path after section
	 * @param player  Player to parse the items with (for %player% and other
	 *                placeholders)
	 * @return Parsed ItemStack
	 */
	public static ItemStack parseItem(ConfigurationSection section, String path, OfflinePlayer player) {
		ConfigurationSection gui = section.getConfigurationSection(path);
		ItemStack item = new ItemStack(Material.valueOf(gui.getString("Icon")));
		List<String> lore = new ArrayList<String>();
		if (gui.contains("Amount"))
			item.setAmount(gui.getInt("Amount"));
		if (gui.contains("Data"))
			item.setDurability((short) gui.getInt("Data"));
		if (gui.contains("Owner")) {
			SkullMeta meta = (SkullMeta) item.getItemMeta();
			meta.setOwner(gui.getString("Owner"));
			item.setItemMeta(meta);
		}
		ItemMeta meta = item.getItemMeta();
		if (gui.contains("Name"))
			meta.setDisplayName(MSG.color("&r" + gui.getString("Name")));
		if (gui.contains("Lore")) {
			for (String temp : gui.getStringList("Lore"))
				lore.add(MSG.color("&r" + temp));
		}
		if (gui.getBoolean("Unbreakable")) {
			meta.spigot().setUnbreakable(true);
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		if (gui.contains("Cost")) {
			ConfigurationSection costs = gui.getConfigurationSection("Cost");
			lore.add("");
			if (costs.getKeys(false).size() == 1) {
				String id = costs.getKeys(false).toArray()[0].toString();
				int cost = (costs.getInt(costs.getKeys(false).toArray()[0].toString()));
				lore.add(MSG.color("&c* " + cost + " " + MSG.camelCase(id))
						+ ((cost == 1 || id.toLowerCase().endsWith("s")) ? "" : "s"));
			} else {
				lore.add(MSG.color("&aCost:"));
				for (String mat : costs.getKeys(false)) {
					if (mat.equals("XP") || mat.equals("COINS")) {
						lore.add(MSG.color("&c* " + costs.getInt(mat) + " " + MSG.camelCase(mat)));
					} else {
						lore.add(MSG.color("&c* " + costs.getInt(mat) + " " + MSG.camelCase(mat))
								+ ((costs.getInt(mat) == 1 || mat.toLowerCase().endsWith("s")) ? "" : "s"));
					}
				}
			}
		}
		if (gui.contains("Enchantments")) {
			ConfigurationSection enchs = gui.getConfigurationSection("Enchantments");
			for (String enchant : enchs.getKeys(false)) {
				int level = 1;
				if (enchs.contains(enchant + ".Level"))
					level = enchs.getInt(enchant + ".Level");
				if (enchs.contains(enchant + ".Visible") && !enchs.getBoolean(enchant + ".Visible"))
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.getByName(enchant.toUpperCase()), level);
				meta = item.getItemMeta();
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Calculates a player's total exp based on level and progress to next.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * @param player the Player
	 * 
	 * @return the amount of exp the Player has
	 */
	public static int getExp(Player player) {
		return getExpFromLevel(player.getLevel()) + Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * Calculates total experience based on level.
	 * 
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 *      "One can determine how much experience has been collected to reach a
	 *      level using the equations:
	 * 
	 *      Total Experience = [Level]2 + 6[Level] (at levels 0-15) 2.5[Level]2 -
	 *      40.5[Level] + 360 (at levels 16-30) 4.5[Level]2 - 162.5[Level] + 2220
	 *      (at level 31+)"
	 * 
	 * @param level the level
	 * 
	 * @return the total experience calculated
	 */
	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	/**
	 * Calculates level based on total experience.
	 * 
	 * @param exp the total experience
	 * 
	 * @return the level calculated
	 */
	public static double getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}

	/**
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 *      "The formulas for figuring out how many experience orbs you need to get
	 *      to the next level are as follows: Experience Required = 2[Current Level]
	 *      + 7 (at levels 0-15) 5[Current Level] - 38 (at levels 16-30) 9[Current
	 *      Level] - 158 (at level 31+)"
	 */
	private static int getExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}

	/**
	 * Change a Player's exp.
	 * <p>
	 * This method should be used in place of {@link Player#giveExp(int)}, which
	 * does not properly account for different levels requiring different amounts of
	 * experience.
	 * 
	 * @param player the Player affected
	 * @param exp    the amount of experience to add or remove
	 */
	public static void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		double levelAndExp = getLevelFromExp(exp);

		int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}

	/**
	 * if oldVer is < newVer, both versions can only have numbers and .'s Outputs:
	 * 5.5, 10.3 | true 2.3.1, 3.1.4.6 | true 1.2, 1.1 | false
	 **/
	public static Boolean outdated(String oldVer, String newVer) {
		oldVer = oldVer.replace(".", "");
		newVer = newVer.replace(".", "");
		Double oldV = null, newV = null;
		try {
			oldV = Double.valueOf(oldVer);
			newV = Double.valueOf(newVer);
		} catch (Exception e) {
			MSG.log("&cError! &7Versions incompatible.");
			return false;
		}
		if (oldVer.length() > newVer.length()) {
			newV = newV * (10 * (oldVer.length() - newVer.length()));
		} else if (oldVer.length() < newVer.length()) {
			oldV = oldV * (10 * (newVer.length() - oldVer.length()));
		}
		return oldV < newV;
	}

	public static boolean isMaterial(String name) {
		for (Material mat : Material.values())
			if (mat.toString().equals(name))
				return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	public static Inventory getEntityViewerGUI(Player player, World world) {
		List<Entity> entities = world.getEntities();
		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
			Entity ent = it.next();
			if (ent instanceof LivingEntity && ((LivingEntity) ent).getHealth() <= 0)
				it.remove();
		}
		int maxSize = 54;
		int size = (int) Math.min(Math.max((Math.ceil(entities.size() / 9.0) * 9), 9), maxSize);
		int page = (int) Math.round(PlayerManager.getDouble(player, "page"));
		Inventory inv = Bukkit.createInventory(null, size, "Entities Viewer (" + entities.size() + " Total)");
		if (entities.size() == 0) {
			for (int i = 0; i < inv.getSize(); i++) {
				ItemStack item = new ItemStack(Material.BARRIER);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(MSG.color("&c&lNo Entities"));
				item.setItemMeta(meta);
				inv.setItem(i, item);
			}
			return inv;
		}
		int pos = (maxSize - 2) * page;
		for (int i = 0; i < size && i + (page * (maxSize - 2)) + 1 <= entities.size(); i++) {
			if (inv.getSize() == maxSize && (i == inv.getSize() - 9 || i == inv.getSize() - 1))
				continue;
			ItemStack item = new ItemStack(Material.MONSTER_EGG);
			Entity ent = entities.get(pos);
			String prefix = "&9&l", suffix = "", type = "Unknown";
			if (ent instanceof Player) {
				item.setType(Material.SKULL_ITEM);
				item.setDurability((short) 3);
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				meta.setOwner(ent.getName());
				item.setItemMeta(meta);
				suffix = " (" + ent.getName() + ")";
				prefix = "&6&l";
				type = "Player";
			} else if (ent instanceof LivingEntity && ent.getType() != EntityType.ARMOR_STAND) {
				try {
					item.setDurability((short) ent.getType().getTypeId());
					prefix = "&a&l";
					type = "Living Entity";
				} catch (IllegalArgumentException ee) {
				}
			} else if (ent instanceof Item) {
				item.setType(((Item) ent).getItemStack().getType());
				item.setAmount(((Item) ent).getItemStack().getAmount());
				suffix = " (Item)";
				prefix = "&7&l";
				type = "Dropped Item";
			} else if (ent instanceof FallingBlock) {
				item.setType(((FallingBlock) ent).getMaterial());
				type = "Falling Block";
				prefix = "&8&l";
			} else {
				try {
					item.setType(Material.valueOf(ent.getType() + ""));
					prefix = "&b&l";
					type = "Miscellaneous";
				} catch (Exception e) {
					item.setType(entityToMat(ent.getType()));
					prefix = "&e&l";
					type = "Entity";
				}
			}
			ItemMeta meta = item.getItemMeta();
			if (ent.getCustomName() != null)
				suffix = " (" + ent.getCustomName() + ")";
			meta.setDisplayName(MSG.color(prefix + MSG.camelCase(ent.getType() + "")) + suffix);
			if (ent instanceof Item) {
				meta.setDisplayName(
						MSG.color(prefix + MSG.camelCase(((Item) ent).getItemStack().getType() + "")) + suffix);
			}
//			if ((ent.getType()+"").contains(".")) {
//				meta.setDisplayName(MSG.color(
//						prefix + MSG.camelCase(ent.getName().split("\\.")[ent.getName().split("\\.").length - 1]))
//						+ suffix);
//			}
			List<String> lore = new ArrayList<>();
			lore.add(MSG.color("&8" + ent.getUniqueId()));
			lore.add(MSG.color("&8Type: &7" + type + ""));
			lore.add("");
			if (ent.getCustomName() != null)
				lore.add(MSG.color("&6Custom Name: &e" + ent.getCustomName()));
			lore.add(MSG
					.color("&7Distance: &e" + MSG.parseDecimal(ent.getLocation().distance(player.getLocation()), 2)));
			lore.add(MSG.color("&7X: &e" + ent.getLocation().getBlockX() + " &7Y: &e" + ent.getLocation().getBlockY()
					+ " &7Z: &e" + ent.getLocation().getBlockZ()));
			if (ent instanceof LivingEntity) {
				lore.add("");
				lore.add(MSG.color("&7Health: &e" + ((LivingEntity) ent).getHealth() + "&7/&e"
						+ ((LivingEntity) ent).getMaxHealth()));
			}

			if (ent instanceof Player) {
				GameMode mode = ((Player) ent).getGameMode();
				lore.add(MSG.color("&7Gamemode: &e" + MSG.camelCase(mode.toString())));
				if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
					lore.add(MSG.color("&7Food Level: &e" + ((Player) ent).getFoodLevel()));
				lore.add(MSG.color("&7Is Flying | Can Fly: &e" + MSG.TorF(player.isFlying()) + "&7 | "
						+ MSG.TorF(player.getAllowFlight())));
				lore.add(MSG.color("&7IP: &a" + player.getAddress().getHostName()));

			}

			lore.add("");

			lore.add(MSG.color("&e&lLeft-Click &eto teleport to"));
			lore.add(MSG.color("&e&lShift-Left &eClick to teleport to you"));
			lore.add(MSG.color("&e&lRight-Click &eto kill"));
			lore.add(MSG.color("&e&lShift-Right &eClick to kill all"));

			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(i, item);
			pos++;
		}

		if (page * (maxSize - 2) + (maxSize - 2) < entities.size()) {
			ItemStack nextArrow = new ItemStack(Material.ARROW);
			ItemMeta meta = nextArrow.getItemMeta();
			meta.setDisplayName(MSG.color("&a&lNext Page"));
			nextArrow.setItemMeta(meta);
			inv.setItem(inv.getSize() - 1, nextArrow);
		}

		if (page > 0) {
			ItemStack lastArrow = new ItemStack(Material.ARROW);
			ItemMeta lastMeta = lastArrow.getItemMeta();
			lastMeta.setDisplayName(MSG.color("&c&lLast Page"));
			lastArrow.setItemMeta(lastMeta);
			inv.setItem(inv.getSize() - 9, lastArrow);
		}

		return inv;
	}

	private static Material entityToMat(EntityType type) {
		switch (type.toString().toLowerCase()) {
		case ("minecart_chest"):
			return Material.STORAGE_MINECART;
		case ("minecart_furnace"):
			return Material.POWERED_MINECART;
		case ("minecart_hopper"):
			return Material.HOPPER_MINECART;
		case ("minecart_tnt"):
			return Material.EXPLOSIVE_MINECART;
		case ("minecart_command"):
			return Material.COMMAND_MINECART;
		case ("splash_potion"):
			return Material.POTION;
		case ("ender_crystal"):
			return Material.EYE_OF_ENDER;
		case ("experience_orb"):
			return Material.EXP_BOTTLE;
		default:
			MSG.log("Unknown Entity: " + type);
			return Material.BARRIER;
		}
	}

	public static Inventory getWorldViewerGUI(Player player) {
		List<String> worlds = Utils.getUnloadedWorlds(true);
		int maxSize = 54;
		int size = (int) Math.min(Math.max((Math.ceil(Utils.getUnloadedWorlds(true).size() / 9.0) * 9), 9), maxSize);
		int page = (int) Math.round(PlayerManager.getDouble(player, "page"));
		Inventory inv = Bukkit.createInventory(null, size, "World Viewer");
		int pos = (maxSize - 2) * page;

		for (int i = 0; i < size && i + (page * (maxSize - 2)) + 1 <= worlds.size(); i++) {
			if (inv.getSize() == maxSize && (i == inv.getSize() - 9 || i == inv.getSize() - 1))
				continue;
			// MSG.log("i: " + i + " pos: " + pos);
			World world = Bukkit.getWorld(worlds.get(pos));
			String name = worlds.get(pos);
			boolean loaded = world != null;
			ItemStack item = new ItemStack(
					!loaded ? Material.STAINED_GLASS
							: world.getWorldType() == WorldType.FLAT ? Material.STEP
									: world.getWorldType() == WorldType.AMPLIFIED ? Material.DIRT
											: world.getEnvironment() == Environment.NORMAL ? Material.GRASS
													: world.getEnvironment() == Environment.NETHER ? Material.NETHERRACK
															: Material.ENDER_STONE,
					world == null ? 1 : Math.max(1, world.getPlayers().size()));

			if (loaded && world.equals(player.getWorld()))
				item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);

			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&a&l" + name));

			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

			List<String> lore = new ArrayList<>();

			if (loaded) {
				lore.add(MSG.color("&8" + world.getUID() + ""));
				lore.add(MSG.color(""));
				lore.add(MSG.color("&7Entities: &e" + world.getEntities().size() + " &7(&aPlayers&7: &a"
						+ world.getPlayers().size() + "&7)"));
				lore.add(MSG.color("&7Loaded Chunks: &e" + world.getLoadedChunks().length));
				lore.add(MSG.color("&7Seed: &a" + world.getSeed()));
				lore.add(MSG.color("&7Difficulty: &b" + MSG.camelCase(world.getDifficulty().toString())));
				lore.add(MSG.color("&7World Type: &b" + MSG.camelCase(
						(world.getEnvironment() != Environment.NORMAL ? world.getEnvironment() : world.getWorldType())
								+ "")));
				lore.add(MSG.color(""));

				if (isPriorityWorld(world)) {
					lore.add(MSG.color("&e&lMiddle-Click &eto view gamerules"));
					lore.add(MSG.color("&e&l[Q] &eKick Players in this world to yours"));
					lore.add(MSG.color(""));
					lore.add(MSG.color("&d&lThis is a priority world"));
					lore.add(MSG.color("&dYou cannot unload or delete this world"));
				} else {
					lore.add(MSG.color("&e&lMiddle-Click &eto view gamerules"));
					lore.add(MSG.color("&e&lShift-Left Click to &c&lUnload"));
					lore.add(MSG.color("&e&lShift-Right Click to &4&lDelete"));
					lore.add(MSG.color("&e&l[Q] &eKick Players in this world to yours"));
				}

				lore.add(MSG.color(""));

				lore.add(MSG.color((world.equals(player.getWorld()) ? "&c&lThis is your current world"
						: "&e&lLeft-Click to teleport to this world")));
			} else {
				lore.add(MSG.color("&8Unloaded"));
				lore.add(MSG.color(""));

				lore.add(MSG.color("&e&lLeft Click &eto &a&lLoad"));
				lore.add(MSG.color("&e&lShift-Right Click &eto &4&lDelete"));
			}

			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(i, item);
			pos++;
		}

		if (page * (maxSize - 2) + (maxSize - 2) < worlds.size()) {
			ItemStack nextArrow = new ItemStack(Material.ARROW);
			ItemMeta meta = nextArrow.getItemMeta();
			meta.setDisplayName(MSG.color("&a&lNext Page"));
			nextArrow.setItemMeta(meta);
			inv.setItem(inv.getSize() - 1, nextArrow);
		}

		if (page > 0) {
			ItemStack lastArrow = new ItemStack(Material.ARROW);
			ItemMeta lastMeta = lastArrow.getItemMeta();
			lastMeta.setDisplayName(MSG.color("&c&lLast Page"));
			lastArrow.setItemMeta(lastMeta);
			inv.setItem(inv.getSize() - 9, lastArrow);
		}
		return inv;
	}

	public static Inventory getGameruleGUI(Player player, World world) {
		boolean addMV = plugin.getMultiverseCore() != null;
		int size = (int) Math.min(Math.max((Math.ceil(world.getGameRules().length / 9.0) * 9), 9) + (addMV ? 18 : 0),
				54);
		Inventory inv = Bukkit.createInventory(null, size, world.getName() + " Gamerules");
		int slot = 0;
		for (String res : world.getGameRules()) {
			String val = world.getGameRuleValue(res), result = "&e" + val;
			ItemStack item = new ItemStack(getGameruleIcon(res), val.equalsIgnoreCase("true") ? 2 : 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&a&l" + res));
			if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
				result = MSG.TorF(Boolean.parseBoolean(val));
				meta.setLore(Arrays.asList("", MSG.color(result), "", MSG.color("&e&lClick to toggle")));
			} else {
				meta.setLore(Arrays.asList("", MSG.color(result)));
			}
			item.setItemMeta(meta);
			inv.setItem(slot, item);
			slot++;
		}

		if (!addMV)
			return inv;

		for (int i = 0; i < 9; i++) {
			ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
			item.setDurability((short) 11);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&9&lMultiverse Settings"));
			meta.setLore(Arrays.asList(MSG.color("&bBelow are Multiverse Settings")));
			item.setItemMeta(meta);
			inv.setItem((inv.getSize() - 18) + i, item);
		}

		MultiverseCore mv = plugin.getMultiverseCore();
		MultiverseWorld mw = mv.getMVWorldManager().getMVWorld(world);
		HashMap<String, Object> entries = getMultiverseWorldValues(mw);

		int pos = 0;
		for (String res : entries.keySet()) {
			ItemStack item = new ItemStack(getGameruleIcon(res));
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(MSG.color("&a&l" + res));
			List<String> lore = new ArrayList<>();
			if (entries.get(res) instanceof Boolean) {
				lore.add(MSG.color("&e" + MSG.TorF(Boolean.valueOf(entries.get(res) + ""))));
				if((Boolean)entries.get(res)) {
					item.setAmount(2);
				}
			} else {
				if (res.equals("Player Limit")) {
					lore.add(MSG.color(
							"&e" + MSG.camelCase((int) entries.get(res) == -1 ? "None" : entries.get(res) + "")));
					item.setAmount(Math.max(1,(int) entries.get(res)));
				} else {
					lore.add(MSG.color("&e" + MSG.camelCase(entries.get(res) + "")));
				}
			}
			if (res.equals("Player Limit")) {
				lore.add("");
				lore.add(MSG.color("&e&lShift-Right Click &e+10"));
				lore.add(MSG.color("&e&lShift-Left Click &e-10"));
				lore.add(MSG.color("&e&lRight Click &e+1"));
				lore.add(MSG.color("&e&lLeft Click &e-1"));
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(inv.getSize() - 9 + pos, item);
			pos++;
		}

		return inv;
	}

	public static HashMap<String, Object> getMultiverseWorldValues(MultiverseWorld world) {
		HashMap<String, Object> values = new HashMap<>();
		values.put("Flight Enabled", world.getAllowFlight());
		values.put("Animals", world.canAnimalsSpawn());
		values.put("Monsters", world.canMonstersSpawn());
		values.put("Difficulty", world.getDifficulty());
		values.put("Gamemode", world.getGameMode());
		values.put("Hunger", world.getHunger());
		values.put("PVP", world.isPVPEnabled());
		values.put("Player Limit", world.getPlayerLimit());
		values.put("Visible", !world.isHidden());
		return values;
	}

	public static Material getGameruleIcon(String gamerule) {
		switch (gamerule.toLowerCase()) {
		case "commandblockoutput":
			return Material.COMMAND;
		case "dodaylightcycle":
			return Material.WATCH;
		case "doentitydrops":
			return Material.ARMOR_STAND;
		case "dofiretick":
			return Material.FLINT_AND_STEEL;
		case "domobloot":
			return Material.ROTTEN_FLESH;
		case "domobspawning":
			return Material.MONSTER_EGG;
		case "dotiledrops":
			return Material.SAND;
		case "keepinventory":
			return Material.CHEST;
		case "logadmincommands":
			return Material.BOOK_AND_QUILL;
		case "mobgriefing":
			return Material.SULPHUR;
		case "naturalregeneration":
			return Material.GOLDEN_APPLE;
		case "randomtickspeed":
			return Material.REDSTONE_COMPARATOR;
		case "reduceddebuginfo":
			return Material.BOOK;
		case "sendcommandfeedback":
			return Material.PAPER;
		case "showdeathmessages":
			return Material.SKULL_ITEM;
		case "flight enabled":
			return Material.FEATHER;
		case "animals":
			return Material.PORK;
		case "monsters":
			return Material.MONSTER_EGG;
		case "difficulty":
			return Material.DIAMOND_SWORD;
		case "gamemode":
			return Material.DIAMOND_BLOCK;
		case "hunger":
			return Material.ROTTEN_FLESH;
		case "pvp":
			return Material.BOW;
		case "player limit":
			return Material.SPONGE;
		case "visible":
			return Material.GLASS;
		default:
			return Material.BARRIER;
		}
	}

	public static boolean deleteWorld(File path) {
		if (path.exists()) {
			File files[] = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteWorld(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static List<String> getUnloadedWorlds(boolean includeLoaded) {
		List<String> worlds = new ArrayList<>();
		if (includeLoaded) {
			for (World world : Bukkit.getWorlds())
				worlds.add(world.getName());
		}

		for (String res : Bukkit.getWorldContainer().list()) {
			File file = new File(Bukkit.getWorldContainer().toPath() + File.separator + res);
			if (isWorldFile(file) && !worlds.contains(file.getName()))
				worlds.add(file.getName());
		}
		return worlds;
	}

	public static boolean isWorldFile(File file) {
		if (file != null && file.list() != null)
			for (String r : file.list())
				if (r.equals("session.lock"))
					return true;
		return false;
	}

	public static boolean isPriorityWorld(World world) {
		if (world != null && Bukkit.getWorlds().indexOf(world) < 3)
			return true;
		return false;
	}
}
