package de.nicouschulas.betterpaintballsystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Killstreak;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.lib.actionbarapi.ActionBarAPI;

public class CooldownKillstreaksActionbar {

	int taskID;
	private final BetterPaintballSystem plugin;

	public CooldownKillstreaksActionbar(@NotNull BetterPaintballSystem plugin){
		this.plugin = plugin;
	}

	public void crearActionbars() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				actualizarActionbars(player, messages, config);
			}
		}, 0L, 20L);
	}

	protected void actualizarActionbars(final Player player, final FileConfiguration messages, final FileConfiguration config) {
		Partida partida = plugin.getPartidaJugador(player.getName());
		if(partida != null) {
			JugadorPaintball jugador = partida.getJugador(player.getName());
			if(jugador != null) {
				Killstreak ultima = jugador.getUltimaKillstreak();
				if(ultima != null) {
					String configName = config.getString("killstreaks_items." + ultima.getTipo() + ".name", ultima.getTipo());
					String actionbarMsg = messages.getString("killstreakActionbar", "&e%killstreak% &7expires in &c%time%s");

					int tiempo = ultima.getTiempo();

					String finalMessage = actionbarMsg
							.replace("%killstreak%", configName)
							.replace("%time%", String.valueOf(tiempo));

					ActionBarAPI.sendActionBar(jugador.getJugador(), ChatColor.translateAlternateColorCodes('&', finalMessage));
				}
			}
		}
	}
}