package org.mswsplex.servermanager.customitems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.mswsplex.servermanager.utils.MSG;

public class StasisSword extends CustomItem {

	public StasisSword(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public String getName() {
		return "&6&lStasis &e&lSword";
	}

	@Override
	public String getId() {
		return "stasissword";
	}

	@Override
	public ItemStack getStack() {
		ItemStack item = new ItemStack(Material.GOLD_SWORD);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(MSG.color(getName()));
		List<String> lore = new ArrayList<String>();
		lore.add(MSG.color("&eLeft-Click&7 Lock entity in stasis"));
		lore.add(MSG.color("&eRight-click &7Manually unlock entity in stasis"));

		meta.setLore(lore);

		item.setItemMeta(meta);

		return item;
	}

}
