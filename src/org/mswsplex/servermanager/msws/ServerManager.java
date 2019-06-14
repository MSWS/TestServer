package org.mswsplex.servermanager.msws;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mswsplex.servermanager.commands.ConfirmCommand;
import org.mswsplex.servermanager.commands.EnumerateCommand;
import org.mswsplex.servermanager.commands.FillCommand;
import org.mswsplex.servermanager.commands.ForcefieldCommand;
import org.mswsplex.servermanager.commands.GamerulesCommand;
import org.mswsplex.servermanager.commands.GetCommand;
import org.mswsplex.servermanager.commands.ManageCommand;
import org.mswsplex.servermanager.commands.TestworldCommand;
import org.mswsplex.servermanager.customitems.CustomItem;
import org.mswsplex.servermanager.customitems.EntityMover;
import org.mswsplex.servermanager.events.Events;
import org.mswsplex.servermanager.managers.PlayerManager;
import org.mswsplex.servermanager.utils.MSG;
import org.mswsplex.servermanager.utils.Utils;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class ServerManager extends JavaPlugin {
	public FileConfiguration config, data, lang, gui;
	public File configYml = new File(getDataFolder(), "config.yml"), dataYml = new File(getDataFolder(), "data.yml"),
			langYml = new File(getDataFolder(), "lang.yml"), guiYml = new File(getDataFolder(), "guis.yml");

	private Map<String, CustomItem> items;

	public void onEnable() {
		MSG.plugin = this;
		PlayerManager.plugin = this;
		Utils.plugin = this;

		if (!configYml.exists())
			saveResource("config.yml", true);
		if (!langYml.exists())
			saveResource("lang.yml", true);
		if (!guiYml.exists())
			saveResource("guis.yml", true);
		config = YamlConfiguration.loadConfiguration(configYml);
		data = YamlConfiguration.loadConfiguration(dataYml);
		lang = YamlConfiguration.loadConfiguration(langYml);
		gui = YamlConfiguration.loadConfiguration(guiYml);

		new ManageCommand(this);
		new TestworldCommand(this);
		new ConfirmCommand(this);
		new FillCommand(this);
		new GamerulesCommand(this);
		new ForcefieldCommand(this);
		new EnumerateCommand(this);
		new GetCommand(this);

		new Events(this);

		checkUpdate: if (config.getBoolean("AutoUpdateChecker")) {
			String ver = getDescription().getVersion(), oVer = Utils.getSpigotVersion(63102);
			if (oVer == null) {
				MSG.log("Could not check latest version.");
				break checkUpdate;
			}
			switch (Utils.outdated(getDescription().getVersion(), oVer)) {
			case -1:
				MSG.log("You are using a developmental version (" + ver + " vs " + oVer + ")");
				break;
			case 0:
				MSG.log(getDescription().getName() + " is up to date");
				break;
			case 1:
				MSG.log("You are using an outdated version (" + ver + " vs " + oVer + ")");
				break;
			}
		}

		if (getMultiverseCore() != null)
			MSG.log("Multiverse succesfully found");

		items = new HashMap<String, CustomItem>();
		items.put("entitymover", new EntityMover(this));
	}

	public CustomItem getItem(String id) {
		return items.get(id);
	}

	public Map<String, CustomItem> getItems() {
		return items;
	}

	public MultiverseCore getMultiverseCore() {
		return (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
	}

	public void onDisable() {
		saveData();
	}

	public void saveData() {
		try {
			data.save(dataYml);
		} catch (Exception e) {
			MSG.log("&cError saving data file");
			MSG.log("&a----------Start of Stack Trace----------");
			e.printStackTrace();
			MSG.log("&a----------End of Stack Trace----------");
		}
	}

	public void saveConfig() {
		try {
			config.save(configYml);
		} catch (Exception e) {
			MSG.log("&cError saving data file");
			MSG.log("&a----------Start of Stack Trace----------");
			e.printStackTrace();
			MSG.log("&a----------End of Stack Trace----------");
		}
	}
}
