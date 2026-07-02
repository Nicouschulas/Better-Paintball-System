package pb.ajneb97;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerConfig {

	private FileConfiguration config;
	private File configFile;
	private final String filePath;
	private final PaintballBattle plugin;

	public PlayerConfig(String filePath, PaintballBattle plugin){
		this.config = null;
		this.configFile = null;
		this.filePath = filePath;
		this.plugin = plugin;
	}

	public String getPath(){
		return this.filePath;
	}

	public FileConfiguration getConfig(){
		if (config == null) {
			reloadPlayerConfig();
		}
		return this.config;
	}

	public void registerPlayerConfig(){
		configFile = new File(plugin.getDataFolder() + File.separator + "players", filePath);
		if(!configFile.exists()){
			try {
				if (!configFile.createNewFile()) {
					plugin.getLogger().warning("Could not create player file for: " + filePath);
				}
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while creating player file: " + filePath, e);
			}
		}
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException | InvalidConfigurationException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while loading player file: " + filePath, e);
		}
	}

	public void savePlayerConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while saving player file: " + filePath, e);
		}
	}

	public void reloadPlayerConfig() {
		if (config == null) {
			configFile = new File(plugin.getDataFolder() + File.separator + "players", filePath);
		}
		config = YamlConfiguration.loadConfiguration(configFile);

		if (configFile != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(configFile);
			config.setDefaults(defConfig);
		}
	}
}