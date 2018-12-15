package org.mswsplex.servermanager.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.mswsplex.servermanager.msws.ServerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.Utils;

public class GiveCommand implements CommandExecutor, TabCompleter {

	private ServerManager plugin;

	public GiveCommand(ServerManager plugin) {
		this.plugin = plugin;
		PluginCommand cmd = plugin.getCommand("give");
		cmd.setExecutor(this);
		cmd.setPermission("manage.command.give");
		cmd.setPermissionMessage(MSG.noPermMessage());
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<String>();
		if (!sender.hasPermission("manage.command.give"))
			return result;
		switch (command.getName().toLowerCase()) {
		case "give":
		case "g":
			if (args.length == 1) {
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (target.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
						result.add(target.getName());
					}
				}
				for (String res : new String[] { "all", "world", "example", "perm:" }) {
					if (res.startsWith(args[0].toLowerCase()))
						result.add(res);
				}
			}
			if (args.length == 2) {
				ConfigurationSection section = plugin.config.getConfigurationSection("CustomKit");
				if (section != null) {
					for (String res : section.getKeys(false)) {
						if (res.toLowerCase().startsWith(args[1]))
							result.add(res);
					}
				}
				for (String res : new String[] { "hand", "inventory" }) {
					if (res.toLowerCase().startsWith(args[1]))
						result.add(res);
				}
				for (Material mat : Material.values()) {
					if (mat != Material.AIR)
						if ((mat + "").replace("_", "").toLowerCase().startsWith(args[1].toLowerCase().replace("_", ""))
								&& result.size() < 20) {
							result.add(MSG.camelCase(mat + "").replace(" ", ""));
						}
				}
			}
			if (args.length == 3) {
				for (String res : new String[] { "1", "64", "2304" }) {
					if (res.startsWith(args[2]))
						result.add(res);
				}
			}
			if (args.length >= 4) {
				for (Enchantment ench : Enchantment.values()) {
					if (ench.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(ench.getName());
				}
			}
			break;
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String giver = plugin.config.getString("Messages.Giver"),
				receiver = plugin.config.getString("Messages.Receiver"), itemName = "";
		switch (command.getName().toLowerCase()) {
		case "give":
			if (args.length <= 1) {
				if (args.length > 0) {
					if (args[0].matches("(?i)(example|examples|syntax|usage)")) {
						MSG.tell(sender, " Examples: ");
						MSG.tell(sender, "&7/" + label + " &eMSWS &bstonesword &c3 &9sharpness:&37 &9fire:&34");
						MSG.tell(sender, "  &7Gives &eMSWS &c3 &bstone swords &9sharpness &3VII&7, &9fireaspect &3IV");
						MSG.tell(sender, "&7/" + label + " &eworld &bskull:Notch &9protection:&37");
						MSG.tell(sender, "  &7Gives &eeveryone in world &bNotch head &7with &9protection VII");
						MSG.tell(sender, "&7/" + label + " &eall &7274 &c1 &9fortune:&3100 &dname:&4ez_miner");
						MSG.tell(sender,
								"  &7Gives &eeveryone &c1 &bID:274 &7with &9fortune C &7dispaly name &4ez miner");
						MSG.tell(sender, "&7/" + label + " &e30 &bhand");
						MSG.tell(sender, "  &7Gives &eeveryone within 30 blocks of you &c30 &bwhatever you're holding");
						MSG.tell(sender, "&7/" + label + " &eperm:rank.admin &bstick");
						MSG.tell(sender, "  &7Gives &eeveryone with permission rank.admin &ca &bstick");
						MSG.tell(sender, "&7/" + label + " &eall &bstone:&94 &8unbreakable");
						MSG.tell(sender, "  &7Gives &eeveryone &ca &bstone (&9damage 4&b) &7that is &8unbreakable");
						return true;
					}
				}
				MSG.sendHelp(sender, 0, "give");
				return true;
			}

			List<ItemStack> item = new ArrayList<ItemStack>();
			int amo = plugin.config.getInt("DefaultAmo");
			if (plugin.config.getInt("MaxAmount") > 0 && !sender.hasPermission("manage.bypass.maxgiveamount"))
				amo = Math.min(amo, plugin.config.getInt("MaxAmount"));
			String criteria = "(" + args[0] + "|" + args[1] + ")";
			if (args.length >= 3) {
				try {
					amo = Integer.valueOf(args[2]);
					criteria = "(" + args[0] + "|" + args[1] + "|" + args[2] + ")";
				} catch (Exception e) {
				}
			}
			String name = args[1];
			if (args[1].equalsIgnoreCase("hand")) {
				if (!(sender instanceof Player)) {
					MSG.tell(sender, plugin.config.getString("Messages.MustBePlayer"));
					return true;
				}
				item.add(((Player) sender).getItemInHand().clone());
				itemName = MSG.color(MSG.camelCase(item.get(0).getType() + ""));
				if (args.length < 3) {
					amo = item.get(0).getAmount();
				}
			} else if (args[1].equalsIgnoreCase("inventory")) {
				if (!(sender instanceof Player)) {
					MSG.tell(sender, plugin.config.getString("Messages.MustBePlayer"));
					return true;
				}
				Player tempP = ((Player) sender);
				for (int i = 0; i < tempP.getInventory().getSize(); i++) {
					itemName = "Inventory";
					if (tempP.getInventory().getItem(i) != null
							&& tempP.getInventory().getItem(i).getType() != Material.AIR) {
						item.add(((Player) sender).getInventory().getItem(i));
					}
				}
				if (item.isEmpty()) {
					MSG.tell(sender, plugin.config.getString("Messages.EmptyInventory"));
					return true;
				}
			} else if (plugin.config.contains("CustomKit." + args[1])) {
				if (!sender.hasPermission("manage.give.kit." + args[1])) {
					MSG.noPerm(sender);
					return true;
				}
				ConfigurationSection section = plugin.config.getConfigurationSection("CustomKit." + args[1]);
				itemName = MSG.camelCase(args[1]);
				for (String res : section.getKeys(false)) {
					item.add(new ItemStack(Material.valueOf(section.getString(res + ".Icon"))));
					ItemMeta meta = item.get(item.size() - 1).getItemMeta();
					if (args.length < 3 && section.contains(res + ".Amount")) {
						amo = section.getInt(res + ".Amount");
					}
					if (section.contains(res + ".Damage"))
						item.get(item.size() - 1).setDurability((short) section.getInt(res + ".Damage"));

					List<String> lore = new ArrayList<String>();
					if (section.contains(res + ".Lore")) {
						for (String loreLine : section.getStringList(res + ".Lore"))
							lore.add(MSG.color(loreLine));
						meta.setLore(lore);
					}
					if (section.contains(res + ".Unbreakable")) {
						meta.spigot().setUnbreakable(section.getBoolean(res + ".Unbreakable"));
					}
					ConfigurationSection enchants = section.getConfigurationSection(res + ".Enchantments");
					if (enchants != null) {
						String unknowns = "";
						for (String theEnchant : enchants.getKeys(false)) {
							Enchantment enchant = Enchantment.getByName(Utils.getEnchant(theEnchant));
							if (enchant == null) {
								unknowns = unknowns + res + ", ";
								continue;
							}
							meta.addEnchant(enchant, enchants.getInt(theEnchant), true);
						}
						if (!unknowns.equals("")) {
							MSG.tell(sender, plugin.config.getString("Messages.UnknownEnchant").replace("%enchantName%",
									unknowns.substring(0, unknowns.length() - 2)));
						}
					}
					if (section.contains(res + ".Name")) {
						meta.setDisplayName(MSG.color(section.getString(res + ".Name")));
					}
					item.get(item.size() - 1).setItemMeta(meta);
				}
			} else if (args[1].toLowerCase().startsWith("skull:")) {
				if (args[1].length() <= 6) {
					name = "MSWS";
				} else {
					name = args[1].split(":")[1];
				}
				ItemStack head = new ItemStack(Material.SKULL_ITEM);
				head.setDurability((short) 3);
				SkullMeta meta = (SkullMeta) head.getItemMeta();
				if (name.toLowerCase().endsWith("s")) {
					itemName = name + "' Skull";
				} else {
					itemName = name + "'s Skull";
				}
				meta.setOwner(name);
				head.setItemMeta(meta);
				item.add(head);
			} else {
				try {
					if (args[1].contains(":")) {
						name = args[1].split(":")[0];
					}
					item.clear();
					item.add(new ItemStack(Integer.valueOf(name), 1));
					itemName = MSG.camelCase(item.get(0).getType() + "");
					if (args[1].contains(":")) {
						item.get(0).setDurability(Integer.valueOf(args[1].split(":")[1]).shortValue());
					}
				} catch (Exception e) {
					try {
						if (name.contains(":"))
							name = name.split(":")[0];
						List<Material> mats = getMaterials(name);
						if (mats.size() != 1 || mats.isEmpty()) {
							String matList = "";
							for (Material mat : mats) {
								if (matList.length() < 100) {
									matList = matList + MSG.camelCase(mat.name())
											+ plugin.config.getString("Messages.Separators.ItemList");
								} else {
									matList = matList + "and more";
									for (int i = 0; i < MSG
											.color(plugin.config.getString("Messages.Separators.ItemList"))
											.length(); i++) {
										matList = matList + " ";
									}
									break;
								}
							}
							matList = matList.substring(0,
									matList.length() - Math.min(
											MSG.color(plugin.config.getString("Messages.Separators.ItemList")).length(),
											matList.length()));
							if (matList.equals(""))
								matList = "None";
							MSG.tell(sender, plugin.config.getString("Messages.UnknownItem")
									.replace("%amount%", mats.size() + "").replace("%results%", matList));
							return true;
						}
						item.add(new ItemStack(mats.get(0), amo));
						if (args[1].contains(":"))
							try {
								item.get(0).setDurability(Integer.valueOf(args[1].split(":")[1]).shortValue());
							} catch (Exception ee) {
							}
						itemName = MSG.camelCase(mats.get(0).name());
					} catch (Exception ee) {
					}
				}
			}
			if (item.get(0).getType() == Material.AIR) {
				MSG.tell(sender, plugin.config.getString("Messages.Air"));
				return true;
			}
			if (!sender.hasPermission("manage.give.item." + item.get(0).getType())) {
				MSG.tell(sender, plugin.config.getString("Messages.NoItemPerm"));
				return true;
			}
			if (plugin.config.getStringList("Blacklisted").contains(item.get(0).getType() + "")
					&& !sender.hasPermission("manage.give.blacklist.bypass")) {
				MSG.tell(sender, plugin.config.getString("Messages.Blacklisted").replace("%item%",
						MSG.camelCase(item.get(0).getType() + "")));
				return true;
			}
			if (!args[1].equalsIgnoreCase("inventory"))
				for (int i = 0; i < item.size(); i++)
					item.get(i).setAmount(amo);
			if (args.length >= 3) {
				String unknowns = "";
				for (String res : args) {
					if (!res.matches(criteria)) {
						ItemMeta meta = item.get(0).getItemMeta();
						if (res.startsWith("name:")) {
							if (res.length() <= 5) {
								continue;
							}
							for (int i = 0; i < item.size(); i++) {
								meta = item.get(i).getItemMeta();
								meta.setDisplayName(MSG.color("&r" + res.split(":")[1].replace("_", " ")));
								itemName = MSG.color(res.split(":")[1].replace("_", " "));
								item.get(i).setItemMeta(meta);
							}
							continue;
						}
						if (res.equalsIgnoreCase("unbreakable")) {
							for (int i = 0; i < item.size(); i++) {
								meta = item.get(i).getItemMeta();
								meta.spigot().setUnbreakable(true);
								item.get(i).setItemMeta(meta);
							}
							continue;
						}
						Enchantment enchant = null;
						String eName = res;
						int level = 1;
						if (res.contains(":")) {
							eName = res.split(":")[0];
							try {
								level = Integer.valueOf(res.split(":")[1]);
							} catch (Exception ee) {

							}
						}
						enchant = Enchantment.getByName(Utils.getEnchant(eName));

						if (enchant == null) {
							unknowns = unknowns + res + ", ";
							continue;
						}

						for (int i = 0; i < item.size(); i++)
							item.get(i).addUnsafeEnchantment(enchant, level);
					}
				}
				if (!unknowns.equals("")) {
					MSG.tell(sender, plugin.config.getString("Messages.UnknownEnchant").replace("%enchantName%",
							unknowns.substring(0, unknowns.length() - 2)));
				}
			}
			// If the item name hasn't been defined yet, set it to the Item Type.
			// if (!plugin.config.contains("CustomKit." + args[1]) &&
			// !item.get(0).getItemMeta().hasDisplayName()
			// &&
			// !args[1].equalsIgnoreCase("inventory")&&!args[1].toLowerCase().startsWith("skull:"))
			if (itemName.equals(""))
				itemName = MSG.camelCase(MSG.color(item.get(0).getType() + ""));

			giver = giver.replace("%amount%", amo + "");
			receiver = receiver.replace("%amount%", amo + "");

			giver = giver.replace("%item%", itemName);
			receiver = receiver.replace("%item%", itemName).replace("%player%", sender.getName());

			if (args[0].equalsIgnoreCase("all")) {
				giver = giver.replace("%player%", "everyone");
				if (!sender.hasPermission("manage.give.all")) {
					MSG.noPerm(sender);
					return true;
				}
				for (Player it : Bukkit.getOnlinePlayers()) {
					if ((args[1].equalsIgnoreCase("inventory") || args[1].equalsIgnoreCase("hand")) && it == sender)
						continue;
					MSG.tell(it, receiver);
					for (ItemStack theItem : item)
						it.getInventory().addItem(theItem);
				}
				MSG.tell(sender, giver);
				return true;
			}
			if (args[0].equalsIgnoreCase("world")) {
				giver = giver.replace("%player%", "world");
				if (!sender.hasPermission("manage.give.world")) {
					MSG.noPerm(sender);
					return true;
				}
				if (!(sender instanceof Player)) {
					MSG.tell(sender, plugin.config.getString("Messages.MustBePlayer"));
					return true;
				}
				for (Player it : Bukkit.getOnlinePlayers()) {
					if ((args[1].equalsIgnoreCase("inventory") || args[1].equalsIgnoreCase("hand")) && it == sender)
						continue;
					MSG.tell(it, receiver);
					for (ItemStack theItem : item)
						it.getInventory().addItem(theItem);
				}
				MSG.tell(sender, giver);
				return true;
			}

			if (args[0].toLowerCase().startsWith("perm:")) {
				String perm = args[0].split(":")[1];
				giver = giver.replace("%player%", "Permission: " + perm);
				for (Player it : Bukkit.getOnlinePlayers()) {
					if (it.hasPermission(perm)) {
						MSG.tell(it, receiver);
						for (ItemStack theItem : item)
							it.getInventory().addItem(theItem);
					}
				}
				MSG.tell(sender, giver);
				return true;
			}

			try {
				Double range = Double.valueOf(args[0]);
				if (!(sender instanceof Player)) {
					MSG.tell(sender, plugin.config.getString("Messages.MustBePlayer"));
					return true;
				}
				if (!sender.hasPermission("manage.give.radius")) {
					MSG.noPerm(sender);
					return true;
				}
				giver = giver.replace("%player%", "radius of " + args[0]);
				Player tempP = (Player) sender;
				for (Player it : tempP.getWorld().getPlayers()) {
					if ((args[1].equalsIgnoreCase("inventory") || args[1].equalsIgnoreCase("hand")) && it == sender)
						continue;
					if (it.getLocation().distance(tempP.getLocation()) <= range) {
						MSG.tell(it, receiver);
						for (ItemStack theItem : item)
							it.getInventory().addItem(theItem);
					}
				}
				MSG.tell(sender, giver);
				return true;
			} catch (Exception e) {
			}

			Player target = getPlayer(sender, args[0]);

			if (target != null) {
				giver = giver.replace("%player%", target.getName());
				MSG.tell(target, receiver);
				for (ItemStack theItem : item)
					target.getInventory().addItem(theItem);
				MSG.tell(sender, giver);
				return true;
			}

			break;
		default:
			return false;
		}
		return true;
	}

	public Player getPlayer(CommandSender sender, String name) {
		List<Player> list = new ArrayList<Player>();
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.getName().toLowerCase().contains(name.toLowerCase())) {
				list.add(target);
			}
		}
		if (list.size() != 1) {
			String msg = "";
			for (int i = 0; i < list.size(); i++) {
				if (i > 10) {
					msg = msg + "and more";
					for (int ii = 0; ii < MSG.color(plugin.config.getString("Messages.Separators.PlayerList"))
							.length(); ii++) {
						msg = msg + " ";
					}
					break;
				}
				msg = msg + list.get(i) + plugin.config.getString("Messages.Separators.PlayerList");
			}
			String result = msg.substring(0, Math.max(0,
					msg.length() - MSG.color(plugin.config.getString("Messages.Separators.PlayerList")).length()));
			if (list.size() == 0) {
				result = "None";
			}
			for (String res : plugin.config.getStringList("Messages.UnknownPlayer")) {
				MSG.tell(sender, res.replace("%amount%", list.size() + "").replace("%criteria%", name)
						.replace("%results%", result));
			}
			return null;
		}
		return list.get(0);
	}

	public List<Material> getMaterials(String matName) {
		List<Material> result = new ArrayList<Material>();
		for (Material mat : Material.values()) {
			if (mat.name().toLowerCase().replace("_", "").contains(matName.toLowerCase().replace("_", ""))) {
				result.add(mat);
			}
			if (mat.name().toLowerCase().replace("_", "").equals(matName.toLowerCase().replace("_", ""))) {
				result.clear();
				result.add(mat);
				return result;
			}
		}
		return result;
	}
}