package de.nicouschulas.betterpaintballsystem.managers;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class Actualizacion implements Listener {

	private final BetterPaintballSystem plugin;

	public Actualizacion(@NotNull BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void Join(@NotNull PlayerJoinEvent event) {
		Player jugador = event.getPlayer();

		if (jugador.isOp() && !plugin.version.equals(plugin.latestversion)) {
			FileConfiguration config = plugin.getConfig();

			if (config.getBoolean("new_version_reminder", true)) {
				jugador.sendMessage(BetterPaintballSystem.prefix + ChatColor.RED + " There is a new version available. " + ChatColor.YELLOW +
						"(" + ChatColor.GRAY + plugin.latestversion + ChatColor.YELLOW + ")");
				jugador.sendMessage(ChatColor.RED + "You can download it at: " + ChatColor.GREEN + "https://www.spigotmc.org/resources/76676/");
			}
		}
	}
}