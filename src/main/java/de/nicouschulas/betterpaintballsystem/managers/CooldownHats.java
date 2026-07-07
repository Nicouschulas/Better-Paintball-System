package de.nicouschulas.betterpaintballsystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Partida;

public class CooldownHats {

	int taskID;
	int tiempo;
	private JugadorPaintball jugador;
	private Partida partida;
	private final BetterPaintballSystem plugin;

	public CooldownHats(@NotNull BetterPaintballSystem plugin){
		this.plugin = plugin;
	}

	public void cooldownHat(final JugadorPaintball jugador, final Partida partida, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.partida = partida;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarCooldownHat()){
				FileConfiguration messages = plugin.getMessages();
				if(!partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
					String msg = messages.getString("hatCooldownFinished", "&aHat cooldown finished!");
					jugador.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}

				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarCooldownHat() {
		if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			if(tiempo <= 0) {
				jugador.setEfectoHatEnCooldown(false);
				return false;
			}else {
				tiempo--;
				jugador.setTiempoEfectoHat(tiempo);
				return true;
			}
		}else {
			jugador.setEfectoHatEnCooldown(false);
			return false;
		}
	}

	public void durationHat(final JugadorPaintball jugador, final Partida partida, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.partida = partida;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarDurationHat()){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarDurationHat() {
		if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			if(tiempo <= 0) {
				jugador.setEfectoHatActivado(false);
				return false;
			}else {
				tiempo--;
				return true;
			}
		}else {
			jugador.setEfectoHatActivado(false);
			return false;
		}
	}
}