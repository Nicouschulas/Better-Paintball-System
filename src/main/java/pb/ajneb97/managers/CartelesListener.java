package pb.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.juego.EstadoPartida;
import pb.ajneb97.juego.Partida;
import pb.ajneb97.utils.UtilidadesOtros;

public class CartelesListener implements Listener {

	private final PaintballBattle plugin;

	public CartelesListener(@NotNull PaintballBattle plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void crearCartel(@NotNull SignChangeEvent event) {
		Player jugador = event.getPlayer();
		if (jugador.isOp() || jugador.hasPermission("paintball.admin")) {
			String firstLine = event.getLine(0);
			if (firstLine != null && firstLine.equals("[Paintball]")) {
				String arena = event.getLine(1);
				if (arena != null && plugin.getPartida(arena) != null) {
					FileConfiguration messages = plugin.getMessages();
					Partida partida = plugin.getPartida(arena);

					String estado = "UNKNOWN";
					if (partida.getEstado().equals(EstadoPartida.JUGANDO)) {
						estado = messages.getString("signStatusIngame", "Ingame");
					} else if (partida.getEstado().equals(EstadoPartida.COMENZANDO)) {
						estado = messages.getString("signStatusStarting", "Starting");
					} else if (partida.getEstado().equals(EstadoPartida.ESPERANDO)) {
						estado = messages.getString("signStatusWaiting", "Waiting");
					} else if (partida.getEstado().equals(EstadoPartida.DESACTIVADA)) {
						estado = messages.getString("signStatusDisabled", "Disabled");
					} else if (partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
						estado = messages.getString("signStatusFinishing", "Finishing");
					}

					List<String> lista = messages.getStringList("signFormat");
					for (int c = 0; c < lista.size(); c++) {
						String replacement = lista.get(c)
								.replace("%arena%", arena)
								.replace("%current_players%", String.valueOf(partida.getCantidadActualJugadores()))
								.replace("%max_players%", String.valueOf(partida.getCantidadMaximaJugadores()))
								.replace("%status%", estado);
						event.setLine(c, ChatColor.translateAlternateColorCodes('&', replacement));
					}

					FileConfiguration config = plugin.getConfig();
					List<String> listaCarteles = new ArrayList<>();
					if (config.contains("Signs." + arena)) {
						listaCarteles = config.getStringList("Signs." + arena);
					}
					listaCarteles.add(event.getBlock().getX() + ";" + event.getBlock().getY() + ";" + event.getBlock().getZ() + ";" + event.getBlock().getWorld().getName());
					config.set("Signs." + arena, listaCarteles);
					plugin.saveConfig();
				}
			}
		}
	}

	@EventHandler
	public void eliminarCartel(@NotNull BlockBreakEvent event) {
		Player jugador = event.getPlayer();
		Block block = event.getBlock();
		if (jugador.isOp() || jugador.hasPermission("paintball.admin")) {
			if (block.getType().name().contains("SIGN")) {
				FileConfiguration config = plugin.getConfig();
				ConfigurationSection section = config.getConfigurationSection("Signs");
				if (section != null) {
					for (String arena : section.getKeys(false)) {
						List<String> listaCarteles = new ArrayList<>();
						if (config.contains("Signs." + arena)) {
							listaCarteles = config.getStringList("Signs." + arena);
						}
						for (int i = 0; i < listaCarteles.size(); i++) {
							String[] separados = listaCarteles.get(i).split(";");
							if (separados.length < 4) continue;

							int x = Integer.parseInt(separados[0]);
							int y = Integer.parseInt(separados[1]);
							int z = Integer.parseInt(separados[2]);
							World world = Bukkit.getWorld(separados[3]);
							if (world != null) {
								if (block.getX() == x && block.getY() == y && block.getZ() == z && world.getName().equals(block.getWorld().getName())) {
									listaCarteles.remove(i);
									config.set("Signs." + arena, listaCarteles);
									plugin.saveConfig();
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void entrarPartida(@NotNull PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Block block = event.getClickedBlock();
		if (block != null && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (block.getType().name().contains("SIGN")) {
				FileConfiguration config = plugin.getConfig();
				ConfigurationSection section = config.getConfigurationSection("Signs");
				if (section != null) {
					FileConfiguration messages = plugin.getMessages();
					String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "[Paintball]")) + " ";
					for (String arena : section.getKeys(false)) {
						Partida partida = plugin.getPartida(arena);
						if (partida != null) {
							List<String> listaCarteles = new ArrayList<>();
							if (config.contains("Signs." + arena)) {
								listaCarteles = config.getStringList("Signs." + arena);
							}
							for (String s : listaCarteles) {
								String[] separados = s.split(";");
								if (separados.length < 4) continue;

								int x = Integer.parseInt(separados[0]);
								int y = Integer.parseInt(separados[1]);
								int z = Integer.parseInt(separados[2]);
								World world = Bukkit.getWorld(separados[3]);
								if (world != null) {
									if (block.getX() == x && block.getY() == y && block.getZ() == z && world.getName().equals(block.getWorld().getName())) {
										if (!Checks.checkTodo(plugin, jugador)) {
											return;
										}
										if (partida.estaActivada()) {
											if (plugin.getPartidaJugador(jugador.getName()) == null) {
												if (!partida.estaIniciada()) {
													if (!partida.estaLlena()) {
														if (!UtilidadesOtros.pasaConfigInventario(jugador, config)) {
															jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("errorClearInventory", "Clear inventory error")));
															return;
														}
														PartidaManager.jugadorEntra(partida, jugador, plugin);
													} else {
														jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("arenaIsFull", "Arena is full")));
													}
												} else {
													jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyStarted", "Arena already started")));
												}
											} else {
												jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena", "Already in arena")));
											}
										} else {
											jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabledError", "Arena disabled")));
										}
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}
}