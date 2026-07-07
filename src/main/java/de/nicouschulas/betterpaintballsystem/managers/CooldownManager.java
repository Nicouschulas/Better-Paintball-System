package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.juego.Equipo;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Partida;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import de.nicouschulas.betterpaintballsystem.utils.ValueOfPatch;


public class CooldownManager {

	int taskID;
	int tiempo;
	private Partida partida;
	private final BetterPaintballSystem plugin;

	public CooldownManager(@NotNull BetterPaintballSystem plugin){
		this.plugin = plugin;
	}

	public void cooldownComenzarJuego(Partida partida, int cooldown){
		this.partida = partida;
		this.tiempo = cooldown;
		partida.setTiempo(tiempo);
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();

		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		String startingMsg = messages.getString("arenaStartingMessage", "&aThe game starts in %time% seconds!");
		for (JugadorPaintball j : jugadores) {
			j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', startingMsg.replace("%time%", tiempo + "")));
		}

		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarComenzarJuego(messages, config)){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarComenzarJuego(FileConfiguration messages, FileConfiguration config) {
		if(partida != null && partida.getEstado().equals(EstadoPartida.COMENZANDO)) {
			if(tiempo <= 5 && tiempo > 0) {
				ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
				String startingMsg = messages.getString("arenaStartingMessage", "&aThe game starts in %time% seconds!");
				String soundStr = config.getString("startCooldownSound", "NOTE_PLING;10;1");
				String[] separados = soundStr.split(";");

				for (JugadorPaintball j : jugadores) {
					j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', startingMsg.replace("%time%", tiempo + "")));
					try {
						Sound sound = ValueOfPatch.valueOf(separados[0]);
						j.getJugador().playSound(j.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
					}
				}
				partida.disminuirTiempo();
				tiempo--;
				return true;
			}else if(tiempo <= 0) {
				PartidaManager.iniciarPartida(partida, plugin);
				return false;
			}else {
				partida.disminuirTiempo();
				tiempo--;
				return true;
			}
		}else {
			if(partida != null) {
				ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
				String cancelMsg = messages.getString("gameStartingCancelled", "&cGame start cancelled because there are not enough players.");
				for (JugadorPaintball j : jugadores) {
					j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', cancelMsg));
				}
			}
			return false;
		}
	}

	public void cooldownJuego(Partida partida){
		this.partida = partida;
		this.tiempo = partida.getTiempoMaximo();
		partida.setTiempo(tiempo);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarJuego()){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarJuego() {
		if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			partida.disminuirTiempo();
			if(tiempo == 0) {
				PartidaManager.iniciarFaseFinalizacion(partida, plugin);
				return false;
			}else {
				tiempo--;
				return true;
			}
		}else {
			return false;
		}
	}

	public void cooldownFaseFinalizacion(Partida partida, int cooldown, final Equipo ganador){
		this.partida = partida;
		this.tiempo = cooldown;
		partida.setTiempo(tiempo);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarComenzarFaseFinalizacion(ganador)){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarComenzarFaseFinalizacion(Equipo ganador) {
		if(partida != null && partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
			partida.disminuirTiempo();
			if(tiempo == 0) {
				PartidaManager.finalizarPartida(partida, plugin, false, ganador);
				return false;
			}else {
				tiempo--;
				if(ganador != null) {
					PartidaManager.lanzarFuegos(ganador.getJugadores());
				}
				return true;
			}
		}else {
			return false;
		}
	}
}