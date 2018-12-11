package org.mswsplex.testserver.msws;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mswsplex.testserver.commands.ConfirmCommand;
import org.mswsplex.testserver.commands.FillCommand;
import org.mswsplex.testserver.commands.ForcefieldCommand;
import org.mswsplex.testserver.commands.GamerulesCommand;
import org.mswsplex.testserver.commands.TestCommand;
import org.mswsplex.testserver.commands.TestworldCommand;
import org.mswsplex.testserver.events.Events;
import org.mswsplex.testserver.managers.PlayerManager;
import org.mswsplex.testserver.utils.MSG;

public class Main extends JavaPlugin {
	public FileConfiguration config, data, lang, gui;
	public File configYml = new File(getDataFolder(), "config.yml"), dataYml = new File(getDataFolder(), "data.yml"),
			langYml = new File(getDataFolder(), "lang.yml"), guiYml = new File(getDataFolder(), "guis.yml");

	public void onEnable() {
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

		new TestCommand(this);
		new TestworldCommand(this);
		new ConfirmCommand(this);
		new FillCommand(this);
		new GamerulesCommand(this);
		new ForcefieldCommand(this);

		new Events(this);
		MSG.plugin = this;
		PlayerManager.plugin = this;

		MSG.log("&aSuccessfully Enabled!");
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
