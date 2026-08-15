package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import de.nicouschulas.betterpaintballsystem.api.Hat;
import de.nicouschulas.betterpaintballsystem.api.PaintballAPI;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.juego.Equipo;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.lib.titleapi.TitleAPI;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesItems;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesOtros;
import de.nicouschulas.betterpaintballsystem.utils.ValueOfPatch;

public class PartidaManager {

	public static void jugadorEntra(Partida partida, Player jugador, BetterPaintballSystem plugin) {
		JugadorPaintball jugadorPaintball = new JugadorPaintball(jugador);
		FileConfiguration messages = plugin.getMessages();
		partida.agregarJugador(jugadorPaintball);
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();

		String joinMsgFormat = messages.getString("playerJoin", "&a%player% joined!");
		for (JugadorPaintball jugadore : jugadores) {
			jugadore.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', joinMsgFormat
					.replace("%player%", jugador.getName())
					.replace("%current_players%", partida.getCantidadActualJugadores() + "")
					.replace("%max_players%", partida.getCantidadMaximaJugadores() + "")));
		}

		jugador.getInventory().clear();
		jugador.getEquipment();
		jugador.getEquipment().clear();
		jugador.getInventory().clear();
		jugador.getEquipment();
		jugador.getEquipment().clear();
		jugador.getEquipment().setArmorContents(new ItemStack[4]);
		jugador.updateInventory();
		jugador.updateInventory();

		jugador.setGameMode(GameMode.SURVIVAL);
		jugador.setExp(0);
		jugador.setLevel(0);
		jugador.setFoodLevel(20);
		jugador.setMaxHealth(20);
		jugador.setHealth(20);
		jugador.setFlying(false);
		jugador.setAllowFlight(false);
		for (PotionEffect p : jugador.getActivePotionEffects()) {
			jugador.removePotionEffect(p.getType());
		}

		if (partida.getLobby() != null) {
			jugador.teleport(partida.getLobby());
		}

		FileConfiguration config = plugin.getConfig();
		if ("true".equals(config.getString("leave_item_enabled", "false"))) {
			ItemStack item = UtilidadesItems.crearItem(config, "leave_item");
			jugador.getInventory().setItem(8, item);
		}
		if ("true".equals(config.getString("hats_item_enabled", "false"))) {
			ItemStack item = UtilidadesItems.crearItem(config, "hats_item");
			jugador.getInventory().setItem(7, item);
		}
		if ("true".equals(config.getString("choose_team_system", "false"))) {
			ItemStack item = UtilidadesItems.crearItem(config, "teams." + partida.getTeam1().getTipo());
			if (item.getItemMeta() != null) {
				ItemMeta meta = item.getItemMeta();
				String t1Name = config.getString("teams." + partida.getTeam1().getTipo() + ".name", partida.getTeam1().getTipo());
				String teamChooseMsg = messages.getString("teamChoose", "&aChoose %team%");
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', teamChooseMsg.replace("%team%", t1Name)));
				item.setItemMeta(meta);
				jugador.getInventory().setItem(0, item);
			}

			item = UtilidadesItems.crearItem(config, "teams." + partida.getTeam2().getTipo());
			if (item.getItemMeta() != null) {
				ItemMeta meta = item.getItemMeta();
				String t2Name = config.getString("teams." + partida.getTeam2().getTipo() + ".name", partida.getTeam2().getTipo());
				String teamChooseMsg = messages.getString("teamChoose", "&aChoose %team%");
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', teamChooseMsg.replace("%team%", t2Name)));
				item.setItemMeta(meta);
				jugador.getInventory().setItem(1, item);
			}
		}

		if (partida.getCantidadActualJugadores() >= partida.getCantidadMinimaJugadores()
				&& EstadoPartida.ESPERANDO.equals(partida.getEstado())) {
			cooldownIniciarPartida(partida, plugin);
		}
	}

	public static void jugadorSale(Partida partida, Player jugador, boolean finalizaPartida,
								   BetterPaintballSystem plugin, boolean cerrandoServer) {
		JugadorPaintball jugadorPaintball = partida.getJugador(jugador.getName());
		FileConfiguration messages = plugin.getMessages();

		if (jugadorPaintball != null) {
			ItemStack[] inventarioGuardado = jugadorPaintball.getGuardados().getInventarioGuardado();
			ItemStack[] equipamientoGuardado = jugadorPaintball.getGuardados().getEquipamientoGuardado();
			GameMode gamemodeGuardado = jugadorPaintball.getGuardados().getGamemodeGuardado();
			float xpGuardada = jugadorPaintball.getGuardados().getXPGuardada();
			int levelGuardado = jugadorPaintball.getGuardados().getLevelGuardado();
			int hambreGuardada = jugadorPaintball.getGuardados().getHambreGuardada();
			double vidaGuardada = jugadorPaintball.getGuardados().getVidaGuardada();
			double maxVidaGuardada = jugadorPaintball.getGuardados().getMaxVidaGuardada();
			boolean allowFligth = jugadorPaintball.getGuardados().isAllowFlight();
			boolean isFlying = jugadorPaintball.getGuardados().isFlying();

			partida.removerJugador(jugador.getName());

			if (!finalizaPartida) {
				ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
				String leaveMsgFormat = messages.getString("playerLeave", "&c%player% left!");
				for (JugadorPaintball jugadore : jugadores) {
					jugadore.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', leaveMsgFormat
							.replace("%player%", jugador.getName())
							.replace("%current_players%", partida.getCantidadActualJugadores() + "")
							.replace("%max_players%", partida.getCantidadMaximaJugadores() + "")));
				}
			}

			FileConfiguration config = plugin.getConfig();
			double x = Double.parseDouble(config.getString("MainLobby.x", "0.0"));
			double y = Double.parseDouble(config.getString("MainLobby.y", "64.0"));
			double z = Double.parseDouble(config.getString("MainLobby.z", "0.0"));
			String world = config.getString("MainLobby.world", "world");
			float yaw = Float.parseFloat(config.getString("MainLobby.yaw", "0.0"));
			float pitch = Float.parseFloat(config.getString("MainLobby.pitch", "0.0"));

			var BukkitWorld = Bukkit.getWorld(world);
			if (BukkitWorld != null) {
				Location mainLobby = new Location(BukkitWorld, x, y, z, yaw, pitch);
				jugador.teleport(mainLobby);
			}

			jugador.getInventory().setContents(inventarioGuardado);
			jugador.getEquipment();
			jugador.getEquipment().setArmorContents(equipamientoGuardado);
			jugador.setGameMode(gamemodeGuardado != null ? gamemodeGuardado : GameMode.SURVIVAL);
			jugador.setLevel(levelGuardado);
			jugador.setExp(xpGuardada);
			jugador.setFoodLevel(hambreGuardada);
			jugador.setMaxHealth(maxVidaGuardada);
			jugador.setHealth(vidaGuardada);
			for (PotionEffect p : jugador.getActivePotionEffects()) {
				jugador.removePotionEffect(p.getType());
			}
			jugador.updateInventory();

			jugador.setAllowFlight(allowFligth);
			jugador.setFlying(isFlying);
		} else {
			partida.removerJugador(jugador.getName());
		}

		if (!cerrandoServer) {
			if (partida.getCantidadActualJugadores() < partida.getCantidadMinimaJugadores()
					&& EstadoPartida.COMENZANDO.equals(partida.getEstado())) {
				partida.setEstado(EstadoPartida.ESPERANDO);
			} else if (partida.getCantidadActualJugadores() <= 1 && EstadoPartida.JUGANDO.equals(partida.getEstado())) {
				PartidaManager.iniciarFaseFinalizacion(partida, plugin);
			} else if ((partida.getTeam1().getCantidadJugadores() == 0 || partida.getTeam2().getCantidadJugadores() == 0) && EstadoPartida.JUGANDO.equals(partida.getEstado())) {
				PartidaManager.iniciarFaseFinalizacion(partida, plugin);
			}
		}
	}

	public static void cooldownIniciarPartida(Partida partida, BetterPaintballSystem plugin) {
		partida.setEstado(EstadoPartida.COMENZANDO);
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int time = Integer.parseInt(config.getString("arena_starting_cooldown", "30"));

		CooldownManager cooldown = new CooldownManager(plugin);
		cooldown.cooldownComenzarJuego(partida, time);

		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "&7[&cPaintball&7]")) + " ";

		if ("true".equals(config.getString("broadcast_starting_arena.enabled", "false"))) {
			List<String> worlds = config.getStringList("broadcast_starting_arena.worlds");
			for (String world : worlds) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getWorld().getName().equals(world)) {
						String broadcastMsg = messages.getString("arenaStartingBroadcast", "&aArena %arena% is starting!");
						player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', broadcastMsg.replace("%arena%", partida.getNombre())));
					}
				}
			}
		}
	}

	public static void iniciarPartida(Partida partida, BetterPaintballSystem plugin) {
		partida.setEstado(EstadoPartida.JUGANDO);

		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();

		if ("true".equals(plugin.getConfig().getString("choose_team_system", "false"))) {
			setTeams(partida);
		} else {
			setTeamsAleatorios(partida);
		}

		darItems(partida, plugin.getConfig(), plugin.getShop(), plugin.getMessages());
		teletransportarJugadores(partida);
		setVidas(partida, plugin.getShop());

		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		String soundPath = config.getString("startGameSound");
		Sound sound = null;
		float volume = 0;
		float pitch = 0;

		if (soundPath != null) {
			String[] separados = soundPath.split(";");
			try {
				sound = ValueOfPatch.valueOf(separados[0]);
				volume = Float.parseFloat(separados[1]);
				pitch = Float.parseFloat(separados[2]);
			} catch (Exception ex) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix + "&7Sound Name: &c" + separados[0] + " &7is not valid."));
			}
		}

		for (JugadorPaintball jugadore : jugadores) {
			var equipo = partida.getEquipoJugador(jugadore.getJugador().getName());
			String nombreTeam = (equipo != null) ? equipo.getTipo() : "";

			jugadore.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStarted", "&aGame started!")));
			String teamInfo = messages.getString("teamInformation", "&7Your Team: %team%");
			jugadore.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', teamInfo.replace("%team%", plugin.getConfig().getString("teams." + nombreTeam + ".name", nombreTeam))));
			jugadore.getJugador().closeInventory();
			if (sound != null) {
				jugadore.getJugador().playSound(jugadore.getJugador().getLocation(), sound, volume, pitch);
			}
		}

		CooldownManager cooldown = new CooldownManager(plugin);
		cooldown.cooldownJuego(partida);
	}


	public static void setVidas(Partida partida, FileConfiguration shop) {
		partida.getTeam1().setVidas(partida.getVidasIniciales());
		partida.getTeam2().setVidas(partida.getVidasIniciales());

		ArrayList<JugadorPaintball> jugadoresTeam1 = partida.getTeam1().getJugadores();
		for (JugadorPaintball j : jugadoresTeam1) {
			//check perk extralives
			int nivelExtraLives = PaintballAPI.getPerkLevel(j.getJugador(), "extra_lives");
			if (nivelExtraLives != 0) {
				String linea = shop.getStringList("perks_upgrades.extra_lives").get(nivelExtraLives - 1);
				String[] sep = linea.split(";");
				int cantidad = Integer.parseInt(sep[0]);
				partida.getTeam1().aumentarVidas(cantidad);
			}
		}
		ArrayList<JugadorPaintball> jugadoresTeam2 = partida.getTeam2().getJugadores();
		for (JugadorPaintball j : jugadoresTeam2) {
			//check perk extralives
			int nivelExtraLives = PaintballAPI.getPerkLevel(j.getJugador(), "extra_lives");
			if (nivelExtraLives != 0) {
				String linea = shop.getStringList("perks_upgrades.extra_lives").get(nivelExtraLives - 1);
				String[] sep = linea.split(";");
				int cantidad = Integer.parseInt(sep[0]);
				partida.getTeam2().aumentarVidas(cantidad);
			}
		}
	}

	public static void killstreakInstantanea(String key, Player jugador, Partida partida, BetterPaintballSystem plugin) {
		FileConfiguration config = plugin.getConfig();
		if (key.equalsIgnoreCase("3_lives")) {
			Equipo equipo = partida.getEquipoJugador(jugador.getName());
			if (equipo != null) {
				equipo.aumentarVidas(3);
			}
		} else if (key.equalsIgnoreCase("teleport")) {
			JugadorPaintball j = partida.getJugador(jugador.getName());
			if (j != null) {
				if (j.getDeathLocation() != null) {
					j.getJugador().teleport(j.getDeathLocation());
				} else {
					Equipo equipo = partida.getEquipoJugador(jugador.getName());
					if (equipo != null && equipo.getSpawn() != null) {
						j.getJugador().teleport(equipo.getSpawn());
					}
				}
			}
		} else if (key.equalsIgnoreCase("more_snowballs")) {
			JugadorPaintball j = partida.getJugador(jugador.getName());
			if (j != null) {
				int snowballs = Integer.parseInt(config.getString("killstreaks_items." + key + ".snowballs", "0"));
				ItemStack item;
				String selectedHat = j.getSelectedHat();
				if (!UtilidadesOtros.isLegacy()) {
					if (selectedHat.equals("chicken_hat")) {
						item = new ItemStack(Material.EGG, 1);
					} else {
						item = new ItemStack(Material.SNOWBALL, 1);
					}

				} else {
					if (selectedHat.equals("chicken_hat")) {
						item = new ItemStack(Material.EGG, 1);
					} else {
						item = new ItemStack(Material.valueOf("SNOW_BALL"), 1);
					}

				}
				for (int i = 0; i < snowballs; i++) {
					jugador.getInventory().addItem(item);
				}
			}
		} else if (key.equalsIgnoreCase("lightning")) {
			JugadorPaintball jugadorAtacante = partida.getJugador(jugador.getName());
			int radio = Integer.parseInt(config.getString("killstreaks_items." + key + ".radius", "0"));
			Collection<Entity> entidades = jugador.getWorld().getNearbyEntities(jugador.getLocation(), radio, radio, radio);
			for (Entity e : entidades) {
				if (e != null && e.getType().equals(EntityType.PLAYER)) {
					Player player = (Player) e;
					JugadorPaintball jugadorDanado = partida.getJugador(player.getName());
					if (jugadorDanado != null) {
						PartidaManager.muereJugador(partida, jugadorAtacante, jugadorDanado, plugin, true, false);
					}
				}
			}
		} else if (key.equalsIgnoreCase("nuke")) {
			partida.setEnNuke(true);
			JugadorPaintball jugadorAtacante = partida.getJugador(jugador.getName());
			CooldownKillstreaks c = new CooldownKillstreaks(plugin);
			String actSound = config.getString("killstreaks_items." + key + ".activateSound", "");
			String finSound = config.getString("killstreaks_items." + key + ".finalSound", "");
			String[] separados1 = actSound.split(";");
			String[] separados2 = finSound.split(";");
			c.cooldownNuke(jugadorAtacante, partida, separados1, separados2);
		}
	}

	@SuppressWarnings("unchecked")
	public static void setTeamsAleatorios(Partida partida) {
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		ArrayList<JugadorPaintball> jugadoresCopia = (ArrayList<JugadorPaintball>) partida.getJugadores().clone();
		//If there are 4, 2 are selected; if there are 5, also 2; if there are 6, 3; if there are 7, also 3.
		Random r = new Random();
		int num = jugadores.size() / 2;
		for (int i = 0; i < num; i++) {
			int pos = r.nextInt(jugadoresCopia.size());
			JugadorPaintball jugadorSelect = jugadoresCopia.get(pos);
			jugadoresCopia.remove(pos);

			partida.repartirJugadorTeam2(jugadorSelect);
		}
	}

	private static void setTeams(Partida partida) {
		//The following remains to be verified:
		//If two users select a team and one leaves, both users will be in the same team.
		//Start the game, and it will be just the two of them.

		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		for (JugadorPaintball j : jugadores) {
			Equipo eq = partida.getEquipoJugador(j.getJugador().getName());
			if (eq != null) {
				eq.removerJugador(j.getJugador().getName());
			}
			String preferenciaTeam = j.getPreferenciaTeam();
			if (preferenciaTeam == null) {
				if (partida.puedeSeleccionarEquipo(partida.getTeam1().getTipo())) {
					j.setPreferenciaTeam(partida.getTeam1().getTipo());
				} else {
					j.setPreferenciaTeam(partida.getTeam2().getTipo());
				}
			}
			preferenciaTeam = j.getPreferenciaTeam();
			if (preferenciaTeam != null && preferenciaTeam.equals(partida.getTeam2().getTipo())) {
				partida.getTeam2().agregarJugador(j);
			} else {
				partida.getTeam1().agregarJugador(j);
			}
		}

		//Final balancing
		Equipo equipo1 = partida.getTeam1();
		Equipo equipo2 = partida.getTeam2();
		for (JugadorPaintball j : jugadores) {
			Equipo equipo = partida.getEquipoJugador(j.getJugador().getName());
			if (equipo != null && equipo.getTipo() != null) {
				if (equipo1.getCantidadJugadores() > equipo2.getCantidadJugadores() + 1) {
					if (equipo.getTipo().equals(equipo1.getTipo())) {
						//Move the player from Team 1 to Team 2
						equipo1.removerJugador(j.getJugador().getName());
						equipo2.agregarJugador(j);
					}
				} else if (equipo2.getCantidadJugadores() > equipo1.getCantidadJugadores() + 1) {
					if (equipo.getTipo().equals(equipo2.getTipo())) {
						//Move the player from Team 2 to Team 1
						equipo2.removerJugador(j.getJugador().getName());
						equipo1.agregarJugador(j);
					}
				}
			}
		}
	}

	public static void darItems(Partida partida, FileConfiguration config, FileConfiguration shop, FileConfiguration messages) {
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		for (JugadorPaintball j : jugadores) {
			Player p = j.getJugador();
			p.getInventory().setItem(8, null);

			Equipo equipo = partida.getEquipoJugador(p.getName());
			if (equipo != null && equipo.getTipo() != null && config.contains("teams." + equipo.getTipo())) {
				darEquipamientoJugador(p, Integer.parseInt(config.getString("teams." + equipo.getTipo() + ".color", "0")));
			} else {
				darEquipamientoJugador(p, 0);
			}
			//check perk initial killcoins
			int nivelInitialKillcoins = PaintballAPI.getPerkLevel(j.getJugador(), "initial_killcoins");
			if (nivelInitialKillcoins != 0) {
				String linea = shop.getStringList("perks_upgrades.initial_killcoins").get(nivelInitialKillcoins - 1);
				String[] sep = linea.split(";");
				int cantidad = Integer.parseInt(sep[0]);
				j.agregarCoins(cantidad);
			}
			UtilidadesItems.crearItemKillstreaks(j, config);
			ponerHat(partida, j, config, messages);
			setBolasDeNieve(j, config);
		}
	}

	@SuppressWarnings("unchecked")
	public static void ponerHat(Partida partida, JugadorPaintball jugador, FileConfiguration config, FileConfiguration messages) {
		ArrayList<Hat> hats = PaintballAPI.getHats(jugador.getJugador());
		for (Hat h : hats) {
			if (h.isSelected()) {
				jugador.setSelectedHat(h.getName());
				ItemStack item = UtilidadesItems.crearItem(config, "hats_items." + h.getName());
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					meta.setLore(null);
					item.setItemMeta(meta);
				}
				if (config.contains("hats_items." + h.getName() + ".skull_id")) {
					String id = config.getString("hats_items." + h.getName() + ".skull_id");
					String textura = config.getString("hats_items." + h.getName() + ".skull_texture");
					item = UtilidadesItems.getCabeza(item, id, textura);
				}
				jugador.getJugador().getEquipment().setHelmet(item);

				if (h.getName().equals("speed_hat")) {
					jugador.getJugador().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, 0, false, false));
				} else if (h.getName().equals("present_hat")) {
					Equipo equipo = partida.getEquipoJugador(jugador.getJugador().getName());
					if (equipo != null && equipo.getJugadores() != null) {
						ArrayList<JugadorPaintball> jugadoresCopy = (ArrayList<JugadorPaintball>) equipo.getJugadores().clone();
						jugadoresCopy.remove(jugador);
						if (!jugadoresCopy.isEmpty()) {
							Random r = new Random();
							int pos = r.nextInt(jugadoresCopy.size());
							String jName = jugadoresCopy.get(pos).getJugador().getName();
							JugadorPaintball j = partida.getJugador(jName);
							if (j != null) {
								j.agregarCoins(3);
								String msgGive = messages.getString("presentHatGive", "");
								String msgReceive = messages.getString("presentHatReceive", "");
								jugador.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', msgGive.replace("%player%", j.getJugador().getName())));
								j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', msgReceive.replace("%player%", jugador.getJugador().getName())));
							}
						}
					}
				}
				return;
			}
		}
	}

	public static void darEquipamientoJugador(Player jugador, int color) {
		ItemStack item = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		if (meta != null) {
			meta.setColor(Color.fromRGB(color));
			item.setItemMeta(meta);
		}
		jugador.getInventory().setHelmet(item);

		item = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		if (meta != null) {
			meta.setColor(Color.fromRGB(color));
			item.setItemMeta(meta);
		}
		jugador.getInventory().setChestplate(item);

		item = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		if (meta != null) {
			meta.setColor(Color.fromRGB(color));
			item.setItemMeta(meta);
		}
		jugador.getInventory().setLeggings(item);

		item = new ItemStack(Material.LEATHER_BOOTS, 1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		if (meta != null) {
			meta.setColor(Color.fromRGB(color));
			item.setItemMeta(meta);
		}
		jugador.getInventory().setBoots(item);
	}

	public static void setBolasDeNieve(JugadorPaintball j, FileConfiguration config) {
		for (int i = 0; i <= 7; i++) {
			j.getJugador().getInventory().setItem(i, null);
		}
		for (int i = 9; i <= 35; i++) {
			j.getJugador().getInventory().setItem(i, null);
		}
		int amount = Integer.parseInt(config.getString("initial_snowballs", "0"));
		ItemStack item;
		String selectedHat = j.getSelectedHat();
		if (!UtilidadesOtros.isLegacy()) {
			if (selectedHat.equals("chicken_hat")) {
				item = new ItemStack(Material.EGG, 1);
			} else {
				item = new ItemStack(Material.SNOWBALL, 1);
			}
		} else {
			if (selectedHat.equals("chicken_hat")) {
				item = new ItemStack(Material.EGG, 1);
			} else {
				item = new ItemStack(Material.valueOf("SNOW_BALL"), 1);
			}
		}

		for (int i = 0; i < amount; i++) {
			j.getJugador().getInventory().addItem(item);
		}
	}

	public static void lanzarFuegos(ArrayList<JugadorPaintball> jugadores) {
		for (JugadorPaintball j : jugadores) {
			Firework fw = (Firework) j.getJugador().getWorld().spawnEntity(j.getJugador().getLocation(), EntityType.FIREWORK_ROCKET);
			FireworkMeta fwm = fw.getFireworkMeta();
			Type type = Type.BALL;
			Color c1 = Color.RED;
			Color c2 = Color.AQUA;
			FireworkEffect efecto = FireworkEffect.builder().withColor(c1).withFade(c2).with(type).build();
			fwm.addEffect(efecto);
			fwm.setPower(2);
			fw.setFireworkMeta(fwm);
		}
	}

	public static void teletransportarJugadores(Partida partida) {
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		for (JugadorPaintball j : jugadores) {
			Player p = j.getJugador();
			Equipo equipo = partida.getEquipoJugador(p.getName());
			if (equipo != null && equipo.getSpawn() != null) {
				p.teleport(equipo.getSpawn());
			}
		}
	}

	public static void iniciarFaseFinalizacion(Partida partida, BetterPaintballSystem plugin) {
		partida.setEstado(EstadoPartida.TERMINANDO);
		Equipo ganador = partida.getGanador();
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();

		String nameTeam1 = config.getString("teams." + partida.getTeam1().getTipo() + ".name", "&cERROR! CHECK YOUR TEAM CONFIG!");
		String nameTeam2 = config.getString("teams." + partida.getTeam2().getTipo() + ".name", "&cERROR! CHECK YOUR TEAM CONFIG!");

		String status;
		if (ganador == null) {
			//draw
			status = messages.getString("gameFinishedTieStatus", "&e&lIt's a tie!");
		} else {
			String ganadorTexto = plugin.getConfig().getString("teams." + ganador.getTipo() + ".name", "&cERROR! CHECK YOUR TEAM CONFIG!");
			String statusTemplate = messages.getString("gameFinishedWinnerStatus", "&a&l%winner_team% &eTeam have won!");
			status = statusTemplate.replace("%winner_team%", ganadorTexto);
		}

		ArrayList<JugadorPaintball> jugadoresKillsOrd = partida.getJugadoresKills();
		String top1;
		String top2;
		String top3;
		int top1Kills;
		int top2Kills = 0;
		int top3Kills = 0;

		if (jugadoresKillsOrd.size() == 2) {
			top1 = jugadoresKillsOrd.get(0).getJugador().getName();
			top1Kills = jugadoresKillsOrd.get(0).getAsesinatos();
			top2 = jugadoresKillsOrd.get(1).getJugador().getName();
			top2Kills = jugadoresKillsOrd.get(1).getAsesinatos();
			top3 = messages.getString("topKillsNone", "&aNone");
		} else if (jugadoresKillsOrd.size() == 1) {
			top1 = jugadoresKillsOrd.getFirst().getJugador().getName();
			top1Kills = jugadoresKillsOrd.getFirst().getAsesinatos();
			top3 = messages.getString("topKillsNone", "&aNone");
			top2 = messages.getString("topKillsNone", "&aNone");
		} else if (jugadoresKillsOrd.size() >= 3) {
			top1 = jugadoresKillsOrd.get(0).getJugador().getName();
			top1Kills = jugadoresKillsOrd.get(0).getAsesinatos();
			top2 = jugadoresKillsOrd.get(1).getJugador().getName();
			top3 = jugadoresKillsOrd.get(2).getJugador().getName();
			top2Kills = jugadoresKillsOrd.get(1).getAsesinatos();
			top3Kills = jugadoresKillsOrd.get(2).getAsesinatos();
		} else {
			top1 = messages.getString("topKillsNone", "&aNone");
			top2 = messages.getString("topKillsNone", "&aNone");
			top3 = messages.getString("topKillsNone", "&aNone");
			top1Kills = 0;
		}
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		List<String> msg = messages.getStringList("gameFinished");
		for (JugadorPaintball j : jugadores) {
			for (String s : msg) {
				j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', s.replace("%status_message%", status)
						.replace("%team1%", nameTeam1)
						.replace("%team2%", nameTeam2)
						.replace("%kills_team1%", partida.getTeam1().getAsesinatosTotales() + "")
						.replace("%kills_team2%", partida.getTeam2().getAsesinatosTotales() + "")
						.replace("%player1%", top1)
						.replace("%player2%", top2)
						.replace("%player3%", top3)
						.replace("%kills_player1%", top1Kills + "")
						.replace("%kills_player2%", top2Kills + "")
						.replace("%kills_player3%", top3Kills + "")
						.replace("%kills_player%", j.getAsesinatos() + "")));
			}
			Equipo equipoJugador = partida.getEquipoJugador(j.getJugador().getName());
			if (MySQL.isEnabled(plugin.getConfig())) {
				int win = 0;
				int lose = 0;
				int tie = 0;
				if (equipoJugador != null && equipoJugador.equals(ganador)) {
					win = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("winnerTitleMessage", "&a&lYou've won!"), "");
				} else if (ganador == null) {
					tie = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("tieTitleMessage", "&9&lIt's a tie!"), "");
				} else {
					lose = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("loserTitleMessage", "&c&lYou've lost!"), "");
				}
				//The player's global record is created/modified here.
				if (!MySQL.jugadorExiste(plugin, j.getJugador().getName())) {
					MySQL.crearJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), "", win, tie, lose, j.getAsesinatos(), 0, 1);
				} else {
					JugadorDatos player = MySQL.getJugador(plugin, j.getJugador().getName());
					int kills = j.getAsesinatos() + (player != null ? player.getKills() : 0);
					int wins = (player != null ? player.getWins() : 0) + win;
					int loses = (player != null ? player.getLoses() : 0) + lose;
					int ties = (player != null ? player.getTies() : 0) + tie;
					MySQL.actualizarJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), wins, loses, ties, kills);
				}
				//This record is the one created for monthly and weekly data
				MySQL.crearJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), partida.getNombre(), win, tie, lose, j.getAsesinatos(), 0, 0);
			} else {
				plugin.registerPlayer(j.getJugador().getUniqueId() + ".yml");
				if (plugin.getJugador(j.getJugador().getName()) == null) {
					plugin.agregarJugadorDatos(new JugadorDatos(j.getJugador().getName(), j.getJugador().getUniqueId().toString(), 0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>()));
				}
				JugadorDatos jugador = plugin.getJugador(j.getJugador().getName());
				Equipo eqJugador = partida.getEquipoJugador(j.getJugador().getName());
				if (jugador != null) {
					if (eqJugador != null && eqJugador.equals(ganador)) {
						jugador.aumentarWins();
						TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("winnerTitleMessage", "&a&lYou've won!"), "");
					} else if (ganador == null) {
						jugador.aumentarTies();
						TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("tieTitleMessage", "&9&lIt's a tie!"), "");
					} else {
						jugador.aumentarLoses();
						TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("loserTitleMessage", "&c&lYou've lost!"), "");
					}

					jugador.aumentarKills(j.getAsesinatos());
				}
			}
			j.getJugador().closeInventory();
			j.getJugador().getInventory().clear();


			if ("true".equals(config.getString("leave_item_enabled"))) {
				ItemStack item = UtilidadesItems.crearItem(config, "leave_item");
				j.getJugador().getInventory().setItem(8, item);
			}
			if ("true".equals(config.getString("play_again_item_enabled"))) {
				ItemStack item = UtilidadesItems.crearItem(config, "play_again_item");
				j.getJugador().getInventory().setItem(7, item);
			}

			if ("false".equals(config.getString("rewards_executed_after_teleport"))) {
				if (ganador != null) {
                    List<String> commands;
                    if (equipoJugador != null && ganador.getTipo() != null && ganador.getTipo().equals(equipoJugador.getTipo())) {
                        commands = config.getStringList("winners_command_rewards");
                    } else {
                        commands = config.getStringList("losers_command_rewards");
                    }
                    ejecutarComandosRewards(commands, j);
                } else {
					List<String> commands = config.getStringList("tie_command_rewards");
					ejecutarComandosRewards(commands, j);
				}
			}
		}

		int time = Integer.parseInt(config.getString("arena_ending_phase_cooldown", "0"));
		CooldownManager c = new CooldownManager(plugin);
		c.cooldownFaseFinalizacion(partida, time, ganador);
	}

	public static void ejecutarComandosRewards(List<String> commands, JugadorPaintball j) {
		CommandSender console = Bukkit.getServer().getConsoleSender();
		for (String command : commands) {
			if (command.startsWith("msg %player%")) {
				String mensaje = command.replace("msg %player% ", "");
				j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', mensaje));
			} else {
				String comandoAEnviar = command.replaceAll("%player%", j.getJugador().getName());
				if (comandoAEnviar.contains("%random")) {
					int pos = comandoAEnviar.indexOf("%random");
					int nextPos = comandoAEnviar.indexOf("%", pos + 1);
					String variableCompleta = comandoAEnviar.substring(pos, nextPos + 1);
					String variable = variableCompleta.replace("%random_", "").replace("%", "");
					String[] sep = variable.split("-");
					int cantidadMinima = 0;
					int cantidadMaxima = 0;

					try {
						cantidadMinima = (int) UtilidadesOtros.eval(sep[0].replace("kills", j.getAsesinatos() + ""));
						cantidadMaxima = (int) UtilidadesOtros.eval(sep[1].replace("kills", j.getAsesinatos() + ""));
					} catch (Exception _) {

					}
					int num = UtilidadesOtros.getNumeroAleatorio(cantidadMinima, cantidadMaxima);
					comandoAEnviar = comandoAEnviar.replace(variableCompleta, num + "");
				}
				Bukkit.dispatchCommand(console, comandoAEnviar);
			}
		}
	}

	public static void finalizarPartida(Partida partida, BetterPaintballSystem plugin, boolean cerrandoServer, Equipo ganadorEquipo) {
		FileConfiguration config = plugin.getConfig();
		ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
		//Remove scoreboards for all players
		for (JugadorPaintball j : jugadores) {
			String tipoFin;
			if (ganadorEquipo != null) {
				Equipo equipoJugador = partida.getEquipoJugador(j.getJugador().getName());
				if (equipoJugador != null && ganadorEquipo.getTipo() != null && ganadorEquipo.getTipo().equals(equipoJugador.getTipo())) {
					tipoFin = "ganador";
				} else {
					tipoFin = "perdedor";
				}
			} else {
				tipoFin = "empate";
			}
			jugadorSale(partida, j.getJugador(), true, plugin, cerrandoServer);
			if ("true".equals(config.getString("rewards_executed_after_teleport")) && !cerrandoServer) {
				if (tipoFin.equals("ganador")) {
					List<String> commands = config.getStringList("winners_command_rewards");
					ejecutarComandosRewards(commands, j);
				} else if (tipoFin.equals("perdedor")) {
					List<String> commands = config.getStringList("losers_command_rewards");
					ejecutarComandosRewards(commands, j);
				} else {
					List<String> commands = config.getStringList("tie_command_rewards");
					ejecutarComandosRewards(commands, j);
				}
			}
		}
		partida.getTeam1().setVidas(0);
		partida.getTeam2().setVidas(0);
		partida.setEnNuke(false);
		partida.modificarTeams(config);

		partida.setEstado(EstadoPartida.ESPERANDO);
	}

	public static void muereJugador(Partida partida, JugadorPaintball jugadorAtacante, final JugadorPaintball jugadorDanado, BetterPaintballSystem plugin, boolean lightning, boolean nuke) {
		if (jugadorDanado.haSidoAsesinadoRecientemente()) {
			return;
		}
		String selectedHatDanado = jugadorDanado.getSelectedHat();
		if (selectedHatDanado.equals("guardian_hat") && jugadorDanado.isEfectoHatActivado()) {
			return;
		}
		if (selectedHatDanado.equals("protector_hat")) {
			Random r = new Random();
			int num = r.nextInt(100);
			if (num >= 80) {
				return;
			}
		}

		Equipo equipoDanado = partida.getEquipoJugador(jugadorDanado.getJugador().getName());
		Equipo equipoAtacante = partida.getEquipoJugador(jugadorAtacante.getJugador().getName());
		if (equipoDanado != null && equipoDanado.equals(equipoAtacante)) {
			return;
		}

		if (lightning) {
			jugadorDanado.getJugador().getWorld().strikeLightningEffect(jugadorDanado.getJugador().getLocation());
		}
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		jugadorDanado.aumentarMuertes();
		jugadorDanado.setDeathLocation(jugadorDanado.getJugador().getLocation().clone());
		String killedByMsg = messages.getString("killedBy", "&cYou have been killed by &6%player%&c.");
		jugadorDanado.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', killedByMsg.replace("%player%", jugadorAtacante.getJugador().getName())));

		String soundKilledBy = config.getString("killedBySound", "block.note_block.pling;10;0.1");
		String[] separados = soundKilledBy.split(";");
		if (separados.length >= 3) {
			try {
				Sound sound = ValueOfPatch.valueOf(separados[0]);
				jugadorDanado.getJugador().playSound(jugadorDanado.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
			} catch (Exception ex) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix + "&7Sound Name: &c" + separados[0] + " &7is not valid."));
			}
		}
		jugadorDanado.setAsesinadoRecientemente(true);
		jugadorDanado.setLastKilledBy(jugadorAtacante.getJugador().getName());
		if (equipoDanado != null) {
			equipoDanado.disminuirVidas(1);
		}

		Equipo equipo = partida.getEquipoJugador(jugadorDanado.getJugador().getName());
		if (selectedHatDanado.equals("explosive_hat")) {
			Random r = new Random();
			int num = r.nextInt(100);
			if (num >= 80) {
				if (Bukkit.getVersion().contains("1.8")) {
					jugadorDanado.getJugador().getWorld().playEffect(jugadorDanado.getJugador().getLocation(), Effect.valueOf("EXPLOSION_LARGE"), 2);
				} else {
					jugadorDanado.getJugador().getWorld().spawnParticle(Particle.EXPLOSION, jugadorDanado.getJugador().getLocation(), 2);
				}
				String soundExplosive = config.getString("explosiveHatSound", "entity.generic.explode;10;1");
				separados = soundExplosive.split(";");
				if (separados.length >= 3) {
					try {
						Sound sound = ValueOfPatch.valueOf(separados[0]);
						jugadorDanado.getJugador().getWorld().playSound(jugadorDanado.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
					} catch (Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix + "&7Sound Name: &c" + separados[0] + " &7is not valid."));
					}
				}
				Collection<Entity> entidades = jugadorDanado.getJugador().getWorld().getNearbyEntities(jugadorDanado.getJugador().getLocation(), 5, 5, 5);
				for (Entity e : entidades) {
					if (e != null && e.getType().equals(EntityType.PLAYER)) {
						Player player = (Player) e;
						JugadorPaintball jugadorDanado2 = partida.getJugador(player.getName());
						if (jugadorDanado2 != null) {
							PartidaManager.muereJugador(partida, jugadorDanado, jugadorDanado2, plugin, false, false);
						}
					}
				}
			}
		}
		if (equipo != null && equipo.getSpawn() != null) {
			jugadorDanado.getJugador().teleport(equipo.getSpawn());
		}
		if (!UtilidadesOtros.isLegacy()) {
			if (selectedHatDanado.equals("chicken_hat")) {
				jugadorDanado.getJugador().getInventory().removeItem(new ItemStack(Material.EGG));
			} else {
				jugadorDanado.getJugador().getInventory().removeItem(new ItemStack(Material.SNOWBALL));
			}
		} else {
			if (selectedHatDanado.equals("chicken_hat")) {
				jugadorDanado.getJugador().getInventory().removeItem(new ItemStack(Material.EGG));
			} else {
				jugadorDanado.getJugador().getInventory().removeItem(new ItemStack(Material.valueOf("SNOW_BALL")));
			}
		}
		PartidaManager.setBolasDeNieve(jugadorDanado, config);

		jugadorAtacante.aumentarAsesinatos();
		int cantidadCoinsGanados = UtilidadesOtros.coinsGanados(jugadorAtacante.getJugador(), config);
		int nivelExtraKillCoins = PaintballAPI.getPerkLevel(jugadorAtacante.getJugador(), "extra_killcoins");
		if (nivelExtraKillCoins != 0) {
			List<String> perks = plugin.getShop().getStringList("perks_upgrades.extra_killcoins");
			if (nivelExtraKillCoins - 1 < perks.size()) {
				String linea = perks.get(nivelExtraKillCoins - 1);
				String[] sep = linea.split(";");
				if (sep.length > 0) {
					int cantidad = Integer.parseInt(sep[0]);
					cantidadCoinsGanados = cantidadCoinsGanados + cantidad;
				}
			}
		}
		String lastKilledBy = jugadorAtacante.getLastKilledBy();
		if (lastKilledBy != null && lastKilledBy.equals(jugadorDanado.getJugador().getName())) {
			cantidadCoinsGanados = cantidadCoinsGanados + 1;
		}
		jugadorAtacante.agregarCoins(cantidadCoinsGanados);
		UtilidadesItems.crearItemKillstreaks(jugadorAtacante, config);

		if (nuke) {
			String tipoAtacante = equipoAtacante != null ? equipoAtacante.getTipo() : "";
			String tipoDanado = equipoDanado != null ? equipoDanado.getTipo() : "";
			String equipoAtacanteName = config.getString("teams." + tipoAtacante + ".name", "ERROR! CHECK YOUR TEAM CONFIG!");
			String equipoDanadoName = config.getString("teams." + tipoDanado + ".name", "ERROR! CHECK YOUR TEAM CONFIG!");
			String nukeMsg = messages.getString("nukeKillMessage", "&8[&7%team_player1%&8] &a%player1% &ewas nuked by &8[&7%team_player2%&8] &a%player2%");
			for (JugadorPaintball j : partida.getJugadores()) {
				if (!j.getJugador().getName().equals(jugadorAtacante.getJugador().getName())) {
					j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', nukeMsg.replace("%team_player1%", equipoDanadoName)
							.replace("%player1%", jugadorDanado.getJugador().getName()).replace("%team_player2%", equipoAtacanteName)
							.replace("%player2%", jugadorAtacante.getJugador().getName())));
				}
			}
		}
		String killMsg = messages.getString("kill", "&aYou have killed &6%player%&a.");
		jugadorAtacante.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', killMsg.replace("%player%", jugadorDanado.getJugador().getName())));
		if (!nuke) {
			String soundKill = config.getString("killSound", "entity.firework_rocket.blast;10;2");
			separados = soundKill.split(";");
			if (separados.length >= 3) {
				try {
					Sound sound = ValueOfPatch.valueOf(separados[0]);
					jugadorAtacante.getJugador().playSound(jugadorAtacante.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
				} catch (Exception ex) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix + "&7Sound Name: &c" + separados[0] + " &7is not valid."));
				}
			}
		}


		int snowballs = Integer.parseInt(config.getString("snowballs_per_kill", "0"));
		String selectedHatAtacante = jugadorAtacante.getSelectedHat();
		if (!UtilidadesOtros.isLegacy()) {
			if (selectedHatAtacante.equals("chicken_hat")) {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.EGG, snowballs));
			} else {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.SNOWBALL, snowballs));
			}

		} else {
			if (selectedHatAtacante.equals("chicken_hat")) {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.EGG, snowballs));
			} else {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.valueOf("SNOW_BALL"), snowballs));
			}

		}

		if (equipoDanado != null && equipoDanado.getVidas() <= 0) {
			//end game
			PartidaManager.iniciarFaseFinalizacion(partida, plugin);
			return;
		}

		int invulnerability = Integer.parseInt(config.getString("respawn_invulnerability", "0"));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> jugadorDanado.setAsesinadoRecientemente(false), invulnerability * 20L);
	}

	public static Partida getPartidaDisponible(BetterPaintballSystem plugin) {
		ArrayList<Partida> partidas = plugin.getPartidas();
		ArrayList<Partida> disponibles = new ArrayList<>();
		for (Partida partida : partidas) {
			if (partida.getEstado().equals(EstadoPartida.ESPERANDO) ||
					partida.getEstado().equals(EstadoPartida.COMENZANDO)) {
				if (!partida.estaLlena()) {
					disponibles.add(partida);
				}
			}
		}

		if (disponibles.isEmpty()) {
			return null;
		}

		//sort
		for (int i = 0; i < disponibles.size(); i++) {
			for (int c = i + 1; c < disponibles.size(); c++) {
				if (disponibles.get(i).getCantidadActualJugadores() < disponibles.get(c).getCantidadActualJugadores()) {
					Partida p = disponibles.get(i);
					disponibles.set(i, disponibles.get(c));
					disponibles.set(c, p);
				}
			}
		}
		return disponibles.getFirst();
	}
}