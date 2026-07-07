package de.nicouschulas.betterpaintballsystem.managers;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Killstreak;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.utils.ValueOfPatch;

public class CooldownKillstreaks {

	int taskID;
	int tiempo;
	private JugadorPaintball jugador;
	private Partida partida;
	private final BetterPaintballSystem plugin;

	public CooldownKillstreaks(@NotNull BetterPaintballSystem plugin){
		this.plugin = plugin;
	}

	public void cooldownKillstreak(final JugadorPaintball jugador, final Partida partida, final String nombre, int tiempo){
		this.jugador = jugador;
		this.partida = partida;
		this.tiempo = tiempo;
		if(nombre.equalsIgnoreCase("fury")) {
			CooldownKillstreaks c = new CooldownKillstreaks(plugin);
			c.cooldownParticulasFury(jugador, partida);
		}
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarCooldownKillstreak(nombre)){
				FileConfiguration messages = plugin.getMessages();
				FileConfiguration config = plugin.getConfig();
				if(!partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
					String configName = config.getString("killstreaks_items." + nombre + ".name", nombre);
					String name = ChatColor.translateAlternateColorCodes('&', configName);

					String expiredMsg = messages.getString("killstreakExpired", "&cYour %killstreak% expired!");
					jugador.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', expiredMsg.replace("%killstreak%", name)));

					String soundStr = config.getString("expireKillstreakSound", "NOTE_PLING;10;1");
					String[] separados = soundStr.split(";");
					try {
						Sound sound = ValueOfPatch.valueOf(separados[0]);
						jugador.getJugador().playSound(jugador.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
					}
				}

				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 20L);
	}

	protected boolean ejecutarCooldownKillstreak(String nombre) {
		if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			if(tiempo <= 0) {
				jugador.removerKillstreak(nombre);
				return false;
			}else {
				Killstreak k = jugador.getKillstreak(nombre);
				if(k != null) {
					tiempo--;
					k.setTiempo(tiempo);
					return true;
				}else {
					jugador.removerKillstreak(nombre);
					return false;
				}
			}
		}else {
			jugador.removerKillstreak(nombre);
			return false;
		}
	}

	public void cooldownParticulasFury(final JugadorPaintball jugador, final Partida partida){
		this.jugador = jugador;
		this.partida = partida;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarParticulasFury()){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 5L);
	}

	protected boolean ejecutarParticulasFury() {
		if (partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			if (jugador != null) {
				if (jugador.getKillstreak("fury") != null) {
					Location l = jugador.getJugador().getLocation().clone();
					l.setY(l.getY() + 1.5);
					if (Bukkit.getVersion().contains("1.8")) {
						l.getWorld().playEffect(l, org.bukkit.Effect.valueOf("VILLAGER_THUNDERCLOUD"), 1);
					} else {
						l.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, l, 1);
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	public void cooldownNuke(final JugadorPaintball jugador, final Partida partida, final String[] separados1, final String[] separados2){
		this.jugador = jugador;
		this.partida = partida;
		this.tiempo = 5;
		final FileConfiguration messages = plugin.getMessages();

		String nukeMsg = messages.getString("nukeImpact", "&c&lNUKE INCOMING: &7%time%");
		for(JugadorPaintball player : partida.getJugadores()) {
			player.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', nukeMsg.replace("%time%", tiempo+"")));
		}
		tiempo--;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutarNuke(separados1,separados2,messages)){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 20L, 20L);
	}

	protected boolean ejecutarNuke(String[] separados1, String[] separados2, FileConfiguration messages) {
		if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
			if(jugador != null) {
				if(tiempo <= 0) {
					try {
						Sound sound = ValueOfPatch.valueOf(separados2[0]);
						if(separados2.length >= 4) {
							if(separados2[3].equalsIgnoreCase("global")) {
								for(JugadorPaintball player : partida.getJugadores()) {
									player.getJugador().playSound(player.getJugador().getLocation(), sound, Float.parseFloat(separados2[1]), Float.parseFloat(separados2[2]));
								}
							}else {
								jugador.getJugador().playSound(jugador.getJugador().getLocation(), sound, Float.parseFloat(separados2[1]), Float.parseFloat(separados2[2]));
							}
						}else {
							jugador.getJugador().playSound(jugador.getJugador().getLocation(), sound, Float.parseFloat(separados2[1]), Float.parseFloat(separados2[2]));
						}
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados2[0]+" &7is not valid."));
					}
					for(JugadorPaintball player : partida.getJugadores()) {
						PartidaManager.muereJugador(partida, jugador, player, plugin, false, true);
					}
					partida.setEnNuke(false);
					return false;
				}else {
					try {
						Sound sound = ValueOfPatch.valueOf(separados1[0]);
						if(separados1.length >= 4) {
							if(separados1[3].equalsIgnoreCase("global")) {
								for(JugadorPaintball player : partida.getJugadores()) {
									player.getJugador().playSound(player.getJugador().getLocation(), sound, Float.parseFloat(separados1[1]), Float.parseFloat(separados1[2]));
								}
							}else {
								jugador.getJugador().playSound(jugador.getJugador().getLocation(), sound, Float.parseFloat(separados1[1]), Float.parseFloat(separados1[2]));
							}
						}else {
							jugador.getJugador().playSound(jugador.getJugador().getLocation(), sound, Float.parseFloat(separados1[1]), Float.parseFloat(separados1[2]));
						}
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados1[0]+" &7is not valid."));
					}

					String nukeMsg = messages.getString("nukeImpact", "&c&lNUKE INCOMING: &7%time%");
					for(JugadorPaintball player : partida.getJugadores()) {
						player.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', nukeMsg.replace("%time%", tiempo+"")));
					}
					tiempo--;
					return true;
				}
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
}