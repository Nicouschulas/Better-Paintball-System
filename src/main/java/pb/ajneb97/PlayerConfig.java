package pb.ajneb97;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class PlayerConfig {

	private FileConfiguration config;
	private File configFile;
	private final String filePath;
	private final PaintballBattle plugin;

	public PlayerConfig(@NotNull String filePath, @NotNull PaintballBattle plugin) {
		this.config = null;
		this.configFile = null;
		this.filePath = filePath;
		this.plugin = plugin;
	}

	public @NotNull String getPath() {
		return this.filePath;
	}

	public @NotNull FileConfiguration getConfig() {
		if (config == null) {
			reloadPlayerConfig();
		}
		return this.config;
	}

	public void registerPlayerConfig() {
		configFile = Path.of(plugin.getDataFolder().getAbsolutePath(), "players", filePath).toFile();
		if (!configFile.exists()) {
			try {
				File parent = configFile.getParentFile();
				if (parent != null && !parent.exists()) {
					if (!parent.mkdirs()) {
						plugin.getLogger().warning("Could not create 'players' directory!");
					}
				}
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
		if (config == null || configFile == null) return;
		try {
			config.save(configFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while saving player file: " + filePath, e);
		}
	}

	public void reloadPlayerConfig() {
		if (configFile == null) {
			configFile = Path.of(plugin.getDataFolder().getAbsolutePath(), "players", filePath).toFile();
		}
		config = YamlConfiguration.loadConfiguration(configFile);
	}
}