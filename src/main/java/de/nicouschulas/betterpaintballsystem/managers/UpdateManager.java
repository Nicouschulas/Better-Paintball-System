package de.nicouschulas.betterpaintballsystem.managers;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public class UpdateManager implements Listener {

	private final BetterPaintballSystem plugin;
	private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().character('&').hexColors().build();

	public UpdateManager(@NotNull BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	public void checkForUpdates() {
		FileConfiguration config = plugin.getConfig();
		if (!config.getBoolean("update-checker.enabled", true)) {
			return;
		}

		final String notifyMethod = config.getString("update-checker.notify-method", "both");
		final String currentVersion = plugin.getDescription().getVersion();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				URL url = new URI("https://api.modrinth.com/v2/project/c7PiXtN1/version").toURL();
				try (InputStream inputStream = url.openStream(); Scanner scanner = new Scanner(inputStream)) {
					String json = scanner.useDelimiter("\\A").next();

					if (json.contains("\"version_number\":\"")) {
						String fetchedLatestVersion = json.split("\"version_number\":\"")[1].split("\"")[0];

						if (!currentVersion.equals(fetchedLatestVersion)) {
							plugin.latestversion = fetchedLatestVersion;

							if ("console".equalsIgnoreCase(notifyMethod) || "both".equalsIgnoreCase(notifyMethod)) {
								plugin.getLogger().warning("-----------------------------------------------------");
								plugin.getLogger().warning("A new version of BetterPaintballSystem is available!");
								plugin.getLogger().warning("Current version: " + currentVersion);
								plugin.getLogger().warning("Latest version: " + plugin.latestversion);
								plugin.getLogger().warning("Download it here: https://modrinth.com/project/better-paintball-system");
								plugin.getLogger().warning("-----------------------------------------------------");
							}
						}
					}
				}
			} catch (IOException | URISyntaxException e) {
				plugin.getLogger().log(Level.FINER, "Update checker failed to connect to the server!", e);
			}
		});
	}

	@EventHandler
	public void Join(@NotNull PlayerJoinEvent event) {
		FileConfiguration config = plugin.getConfig();
		if (!config.getBoolean("update-checker.enabled", true)) {
			return;
		}

		String notifyMethod = config.getString("update-checker.notify-method", "both");

		if (plugin.latestversion != null) {
			Player jugador = event.getPlayer();

			if ((jugador.isOp() || jugador.hasPermission("betterpaintballsystem.update")) &&
					("player".equalsIgnoreCase(notifyMethod) || "both".equalsIgnoreCase(notifyMethod))) {

				Component prefixComponent = LegacyComponentSerializer.legacySection().deserialize(BetterPaintballSystem.prefix);

				Component textComponent = legacySerializer.deserialize("&aA new version of BetterPaintballSystem is available: &e" + plugin.latestversion + " ");

				Component linkComponent = Component.text("Click here to download it at Modrinth", NamedTextColor.GRAY)
						.clickEvent(ClickEvent.openUrl("https://modrinth.com/project/better-paintball-system"));

				Component updateMessage = prefixComponent.append(textComponent).append(linkComponent);
				jugador.sendMessage(updateMessage);
			}
		}
	}
}