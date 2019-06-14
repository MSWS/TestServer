package org.mswsplex.servermanager.customitems;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CustomItem implements Listener {
	protected JavaPlugin plugin;

	public CustomItem(JavaPlugin plugin) {
		this.plugin = plugin;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public abstract String getName();
	public abstract String getId();


	public abstract ItemStack getStack();
}
