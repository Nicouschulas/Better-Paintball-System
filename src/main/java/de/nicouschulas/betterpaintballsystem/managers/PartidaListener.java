package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.List;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import me.clip.placeholderapi.PlaceholderAPI;
import de.nicouschulas.betterpaintballsystem.juego.Equipo;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Killstreak;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesItems;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesOtros;
import de.nicouschulas.betterpaintballsystem.utils.ValueOfPatch;

public class PartidaListener implements Listener {

	private final BetterPaintballSystem plugin;
	public PartidaListener(BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void alSalir(PlayerQuitEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			PartidaManager.jugadorSale(partida, jugador, false, plugin, false);
		}
	}

	@EventHandler
	public void clickearItemSalir(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(event.getItem() != null) {
					FileConfiguration config = plugin.getConfig();
					ItemStack item = UtilidadesItems.crearItem(config, "leave_item");
					if(event.getItem().isSimilar(item)) {
						event.setCancelled(true);
						PartidaManager.jugadorSale(partida, jugador, false, plugin, false);
					}
				}
			}
		}
	}

	@EventHandler
	public void clickearItemHats(PlayerInteractEvent event) {
		final Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(event.getItem() != null) {
					FileConfiguration config = plugin.getConfig();
					ItemStack item = UtilidadesItems.crearItem(config, "hats_item");
					if(event.getItem().isSimilar(item)) {
						event.setCancelled(true);

						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							jugador.updateInventory();
                            jugador.getEquipment();
                            jugador.getEquipment().setHelmet(null);
                            InventarioHats.crearInventario(jugador, plugin);
						}, 2L);
					}
				}
			}
		}
	}

	@EventHandler
	public void clickearItemPlayAgain(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(event.getItem() != null) {
					FileConfiguration config = plugin.getConfig();
					ItemStack item = UtilidadesItems.crearItem(config, "play_again_item");
					if(event.getItem().isSimilar(item)) {
						event.setCancelled(true);
						Partida partidaNueva = PartidaManager.getPartidaDisponible(plugin);
						if(partidaNueva == null) {
							FileConfiguration messages = plugin.getMessages();
							String noArenas = messages.getString("noArenasAvailable", "&cNo arenas available.");
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', noArenas));
						} else {
							PartidaManager.jugadorSale(partida, jugador, true, plugin, false);
							PartidaManager.jugadorEntra(partidaNueva, jugador, plugin);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void clickearItemSelectorEquipo(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(event.getItem() != null) {
					FileConfiguration config = plugin.getConfig();
					FileConfiguration messages = plugin.getMessages();

					if("true".equals(config.getString("choose_team_system", "false"))) {
						ItemStack team1 = UtilidadesItems.crearItem(config, "teams." + partida.getTeam1().getTipo());
						if(team1.getItemMeta() != null) {
							ItemMeta meta = team1.getItemMeta();
							String team1Name = config.getString("teams." + partida.getTeam1().getTipo() + ".name", partida.getTeam1().getTipo());
							String teamChooseMsg = messages.getString("teamChoose", "&aChoose %team%");
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', teamChooseMsg.replace("%team%", team1Name)));
							team1.setItemMeta(meta);
						}

						ItemStack team2 = UtilidadesItems.crearItem(config, "teams." + partida.getTeam2().getTipo());
						if(team2.getItemMeta() != null) {
							ItemMeta meta = team2.getItemMeta();
							String team2Name = config.getString("teams." + partida.getTeam2().getTipo() + ".name", partida.getTeam2().getTipo());
							String teamChooseMsg = messages.getString("teamChoose", "&aChoose %team%");
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', teamChooseMsg.replace("%team%", team2Name)));
							team2.setItemMeta(meta);
						}

						JugadorPaintball j = partida.getJugador(jugador.getName());
						if(j == null) return;

						if(event.getItem().isSimilar(team1)) {
							event.setCancelled(true);
							jugador.updateInventory();
							if(j.getPreferenciaTeam() != null && j.getPreferenciaTeam().equals(partida.getTeam1().getTipo())) {
								String alreadySelected = messages.getString("errorTeamAlreadySelected", "&cAlready in this team.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadySelected));
								return;
							}
							if(partida.puedeSeleccionarEquipo(partida.getTeam1().getTipo())) {
								j.setPreferenciaTeam(partida.getTeam1().getTipo());
								String teamSelectedStr = messages.getString("teamSelected", "&aSelected %team%");
								String team1Name = config.getString("teams." + partida.getTeam1().getTipo() + ".name", partida.getTeam1().getTipo());
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', teamSelectedStr.replace("%team%", team1Name)));
							} else {
								String errorTeamSelected = messages.getString("errorTeamSelected", "&cTeam is full.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', errorTeamSelected));
							}

						} else if(event.getItem().isSimilar(team2)) {
							event.setCancelled(true);
							jugador.updateInventory();
							if(j.getPreferenciaTeam() != null && j.getPreferenciaTeam().equals(partida.getTeam2().getTipo())) {
								String alreadySelected = messages.getString("errorTeamAlreadySelected", "&cAlready in this team.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadySelected));
								return;
							}
							if(partida.puedeSeleccionarEquipo(partida.getTeam2().getTipo())) {
								j.setPreferenciaTeam(partida.getTeam2().getTipo());
								String teamSelectedStr = messages.getString("teamSelected", "&aSelected %team%");
								String team2Name = config.getString("teams." + partida.getTeam2().getTipo() + ".name", partida.getTeam2().getTipo());
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', teamSelectedStr.replace("%team%", team2Name)));
							} else {
								String errorTeamSelected = messages.getString("errorTeamSelected", "&cTeam is full.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', errorTeamSelected));
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void clickearItemKillstreak(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(event.getItem() != null && event.getItem().hasItemMeta()) {
					FileConfiguration config = plugin.getConfig();
					if(jugador.getInventory().getHeldItemSlot() == 8 && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
						if("true".equals(config.getString("killstreaks_item_enabled", "false"))) {
							if(partida.getEstado().equals(EstadoPartida.JUGANDO)) {
								event.setCancelled(true);
								String title = config.getString("killstreaks_inventory_title", "&8Killstreaks");
								Inventory inv = Bukkit.createInventory(null, 18, ChatColor.translateAlternateColorCodes('&', title));
								jugador.openInventory(inv);
								InventarioKillstreaks i = new InventarioKillstreaks(plugin);

								i.actualizarInventario(jugador, partida);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void alShiftear(PlayerToggleSneakEvent event) {
		if(event.isSneaking()) {
			Player jugador = event.getPlayer();
			Partida partida = plugin.getPartidaJugador(jugador.getName());
			if(partida != null && partida.getEstado().equals(EstadoPartida.JUGANDO)) {
				FileConfiguration messages = plugin.getMessages();
				JugadorPaintball j = partida.getJugador(jugador.getName());

				if(j != null) {
					String hat = j.getSelectedHat();
					if("guardian_hat".equals(hat) || "jump_hat".equals(hat)) {
						if(!j.isEfectoHatEnCooldown()) {
							FileConfiguration config = plugin.getConfig();
							int duration = Integer.parseInt(config.getString("hats_items."+hat+".duration", "5"));
							int cooldown = Integer.parseInt(config.getString("hats_items."+hat+".cooldown", "30"));

							if("jump_hat".equals(hat)) {
								jugador.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * duration, 1, false, false));
							} else {
								jugador.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * duration, 2, false, false));
								CooldownHats c = new CooldownHats(plugin);
								c.durationHat(j, partida, duration);
							}
							j.setEfectoHatActivado(true);
							j.setEfectoHatEnCooldown(true);

							String activeMsg = messages.getString("hatAbilityActivated", "&aHat ability activated!");
							j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', activeMsg));

							String soundPath = config.getString("hatAbilityActivatedSound");
							if(soundPath != null) {
								String[] separados = soundPath.split(";");
								try {
									Sound sound = ValueOfPatch.valueOf(separados[0]);
									j.getJugador().playSound(j.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
								} catch(Exception ex) {
									Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix + "&7Sound Name: &c" + separados[0] + " &7is not valid."));
								}
							}
							CooldownHats c = new CooldownHats(plugin);
							c.cooldownHat(j, partida, cooldown);
						} else {
							String cooldownMsg = messages.getString("hatCooldownError", "&cAbility on cooldown. Wait %time%s.");
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', cooldownMsg.replace("%time%", j.getTiempoEfectoHat() + "")));
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void alUsarComando(PlayerCommandPreprocessEvent event) {
		String comando = event.getMessage().toLowerCase();
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null && !jugador.isOp() && !jugador.hasPermission("paintball.admin")) {
			FileConfiguration config = plugin.getConfig();
			List<String> comandos = config.getStringList("commands_whitelist");
			for (String s : comandos) {
				if (comando.startsWith(s.toLowerCase())) {
					return;
				}
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void romperBloques(BlockBreakEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void dropearItem(PlayerDropItemEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void interactuarInventario(InventoryClickEvent event){
		Player jugador = (Player) event.getWhoClicked();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
            event.getInventory();
            if((event.getInventory().getType().equals(InventoryType.PLAYER) || event.getInventory().getType().equals(InventoryType.CRAFTING)) && event.getCurrentItem() != null){
				if(!event.getCurrentItem().getType().equals(Material.AIR) &&
						!event.getCurrentItem().getType().name().contains("SNOW")) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void ponerBloques(BlockPlaceEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void caerVacio(EntityDamageEvent event) {
		Entity entidad = event.getEntity();
		if(entidad instanceof Player jugador) {
			Partida partida = plugin.getPartidaJugador(jugador.getName());
			if(partida != null && partida.estaIniciada()) {
				if(event.getCause().equals(DamageCause.VOID)) {
					Equipo equipo = partida.getEquipoJugador(jugador.getName());
					if(equipo != null) {
						jugador.teleport(equipo.getSpawn());
					}
				}
			}
		}
	}

	@EventHandler
	public void craftear(InventoryClickEvent event) {
		Player jugador = (Player) event.getWhoClicked();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getClickedInventory() != null) {
				if(event.getClickedInventory().getType().equals(InventoryType.CRAFTING) && event.getSlot() == 0 && event.getSlotType().equals(SlotType.RESULT)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void romperGranjas(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			if(event.getClickedBlock() != null) {
				String name = event.getClickedBlock().getType().name();
				if(event.getAction() == Action.PHYSICAL && (name.equals("SOIL") || name.equals("FARMLAND"))) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void alDañar(EntityDamageEvent event) {
		Entity entidad = event.getEntity();
		if(entidad instanceof Player) {
			Partida partida = plugin.getPartidaJugador(entidad.getName());
			if(partida != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void nivelDeComida(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player jugador) {
			Partida partida = plugin.getPartidaJugador(jugador.getName());
			if(partida != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void alChatear(AsyncPlayerChatEvent event) {
		Player jugador = event.getPlayer();
		if(!event.isCancelled()) {
			Partida partida = plugin.getPartidaJugador(jugador.getName());
			FileConfiguration config = plugin.getConfig();
			if("false".equals(config.getString("arena_chat_enabled", "true"))) {
				return;
			}
			if(partida != null) {
				FileConfiguration messages = plugin.getMessages();
				String message = event.getMessage();
				event.setCancelled(true);
				ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
				String formatPath = config.getString("arena_chat_format", "<%team%> %player%: %message%");

				if(partida.estaIniciada() && partida.getEquipoJugador(jugador.getName()) != null) {
					String teamName = config.getString("teams."+partida.getEquipoJugador(jugador.getName()).getTipo()+".name", "");
					for(JugadorPaintball j : jugadores) {
						String msg = ChatColor.translateAlternateColorCodes('&', formatPath.replace("%player%", jugador.getName()).replace("%team%", teamName));
						if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
							msg = PlaceholderAPI.setPlaceholders(j.getJugador(), msg.replace("%message%", message));
						} else {
							msg = msg.replace("%message%", message);
						}
						j.getJugador().sendMessage(msg);
					}
				} else {
					String noneTeam = messages.getString("teamInformationNone", "None");
					for(JugadorPaintball j : jugadores) {
						String msg = ChatColor.translateAlternateColorCodes('&', formatPath.replace("%player%", jugador.getName()).replace("%team%", noneTeam));
						if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
							msg = PlaceholderAPI.setPlaceholders(j.getJugador(), msg.replace("%message%", message));
						} else {
							msg = msg.replace("%message%", message);
						}
						j.getJugador().sendMessage(msg);
					}
				}
			} else {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(plugin.getPartidaJugador(p.getName()) != null) {
						event.getRecipients().remove(p);
					}
				}
			}
		}
	}

	@EventHandler
	public void impactoBolaDeNieve(EntityDamageByEntityEvent event) {
		Entity e = event.getDamager();
		if(e instanceof Projectile proyectil && (e.getType().equals(EntityType.SNOWBALL) || e.getType().equals(EntityType.EGG))) {
			ProjectileSource shooter = proyectil.getShooter();
			Entity danado = event.getEntity();
			if(danado instanceof Player jugadorDanado && shooter instanceof Player jugadorAtacante) {
                Partida partida = plugin.getPartidaJugador(jugadorAtacante.getName());
				if(partida != null) {
					if(partida.getEstado().equals(EstadoPartida.JUGANDO)) {

						event.setCancelled(true);

						JugadorPaintball j = partida.getJugador(jugadorAtacante.getName());
						JugadorPaintball j2 = partida.getJugador(jugadorDanado.getName());

						if(j == null || j2 == null || j2.getKillstreak("fury") != null) {
							return;
						}

						PartidaManager.muereJugador(partida, j, j2, plugin, false, false);
					}
				}
			}
		}
	}

	@EventHandler
	public void clickInventarioKillstreak(InventoryClickEvent event){
		FileConfiguration config = plugin.getConfig();
		String titlePath = config.getString("killstreaks_inventory_title", "&8Killstreaks");
		String pathInventory = ChatColor.translateAlternateColorCodes('&', titlePath);
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();

		if(ChatColor.stripColor(event.getView().getTitle()).contains(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			Player jugador = (Player) event.getWhoClicked();
			event.setCancelled(true);

			if(event.getClickedInventory() != null && event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
				Partida partida = plugin.getPartidaJugador(jugador.getName());
				if(partida == null) return;

				JugadorPaintball j = partida.getJugador(jugador.getName());
				if(j == null) return;

				int slot = event.getSlot();
				var section = config.getConfigurationSection("killstreaks_items");
				if(section != null) {
					for(String key : section.getKeys(false)) {
						if(slot == config.getInt("killstreaks_items."+key+".slot", -1)) {
							if(j.getKillstreak(key) != null) {
								String alreadyActive = messages.getString("killstreakAlreadyActivated", "&cKillstreak already activated.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyActive));
								return;
							}

							String permission = config.getString("killstreaks_items."+key+".permission");
							if(permission != null && !permission.isEmpty()) {
								if(!jugador.hasPermission(permission)) {
									String permError = config.getString("killstreaks_items."+key+".permissionError", "&cNo permission.");
									jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', permError));
									return;
								}
							}

							int cost = config.getInt("killstreaks_items."+key+".cost", 0);
							if(j.getCoins() >= cost) {
								if(key.equalsIgnoreCase("nuke") && partida.isEnNuke()) {
									String nukeError = messages.getString("nukeError", "&cAKillstreak already active.");
									jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', nukeError));
									return;
								}

								j.disminuirCoins(cost);
								String name = ChatColor.translateAlternateColorCodes('&', config.getString("killstreaks_items."+key+".name", key));

								var equipo = partida.getEquipoJugador(jugador.getName());
								String teamName = (equipo != null) ? config.getString("teams."+equipo.getTipo()+".name", "") : "";

								for(JugadorPaintball player : partida.getJugadores()) {
									if(!player.getJugador().getName().equals(jugador.getName())) {
										String pActiveMsg = messages.getString("killstreakActivatedPlayer", "&a%player% activated %killstreak%");
										String msg = ChatColor.translateAlternateColorCodes('&', pActiveMsg
												.replace("%player%", jugador.getName())
												.replace("%team%", teamName)
												.replace("%killstreak%", name));
										player.getJugador().sendMessage(msg);
									}
								}

								String activatedMsg = messages.getString("killstreakActivated", "&aActivated %killstreak%");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', activatedMsg.replace("%killstreak%", name)));

								if(key.equalsIgnoreCase("strong_arm") || key.equalsIgnoreCase("triple_shoot") || key.equalsIgnoreCase("fury")) {
									int duration = config.getInt("killstreaks_items."+key+".duration", 10);
									if("time_hat".equals(j.getSelectedHat())) {
										duration = duration + 5;
									}
									Killstreak k = new Killstreak(key, duration);
									j.agregarKillstreak(k);
									CooldownKillstreaks cooldown = new CooldownKillstreaks(plugin);
									cooldown.cooldownKillstreak(j, partida, key, duration);
								} else {
									PartidaManager.killstreakInstantanea(key, jugador, partida, plugin);
								}

								String soundPath = config.getString("killstreaks_items."+key+".activateSound");
								if(soundPath != null) {
									String[] separados = soundPath.split(";");
									try {
										Sound sound = ValueOfPatch.valueOf(separados[0]);
										if(separados.length >= 4) {
											if(separados[3].equalsIgnoreCase("global")) {
												for(JugadorPaintball player : partida.getJugadores()) {
                                                    player.getJugador().playSound(player.getJugador().getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
                                                }
											} else {
												jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
											}
										} else {
											jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
										}
									} catch(Exception ex) {
										Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
									}
								}

								UtilidadesItems.crearItemKillstreaks(j, config);
							} else {
								String noCoins = messages.getString("noSufficientCoins", "&cNot enough coins.");
								jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', noCoins));
							}
							return;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void preLanzarSnowball(PlayerInteractEvent event) {
		Player jugador = event.getPlayer();
		ItemStack item = event.getItem();
		if(event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			if(item != null && (item.getType().name().contains("SNOW") || item.getType().name().contains("EGG"))) {
				Partida partida = plugin.getPartidaJugador(jugador.getName());
				if(partida != null) {
					event.setCancelled(true);
					JugadorPaintball player = partida.getJugador(jugador.getName());
					if(player == null) return;

					if(player.getKillstreak("fury") == null) {
						if(!UtilidadesOtros.isLegacy()) {
							if("chicken_hat".equals(player.getSelectedHat())) {
								jugador.getInventory().removeItem(new ItemStack(Material.EGG, 1));
							} else {
								jugador.getInventory().removeItem(new ItemStack(Material.SNOWBALL, 1));
							}
						} else {
							if("chicken_hat".equals(player.getSelectedHat())) {
								jugador.getInventory().removeItem(new ItemStack(Material.EGG, 1));
							} else {
								jugador.getInventory().removeItem(new ItemStack(Material.valueOf("SNOW_BALL"), 1));
							}
						}
					}

					FileConfiguration config = plugin.getConfig();
					String soundPath = config.getString("snowballShootSound");
					if(soundPath != null) {
						String[] separados = soundPath.split(";");
						try {
							Sound sound = ValueOfPatch.valueOf(separados[0]);
							jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separados[1]), Float.parseFloat(separados[2]));
						} catch(Exception ex) {
							Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
						}
					}

					if("chicken_hat".equals(player.getSelectedHat())) {
						jugador.launchProjectile(Egg.class, jugador.getLocation().getDirection());
					} else {
						jugador.launchProjectile(Snowball.class, jugador.getLocation().getDirection());
					}

					Killstreak k = player.getKillstreak("triple_shoot");
					if(k != null) {
						Vector direccion = jugador.getLocation().getDirection().clone();
						double anguloEntre = Math.toRadians(10);

						double xFinal = direccion.getX()*Math.cos(anguloEntre)-direccion.getZ()*Math.sin(anguloEntre);
						double zFinal = direccion.getX()*Math.sin(anguloEntre)+direccion.getZ()*Math.cos(anguloEntre);

						direccion = new Vector(xFinal, direccion.getY(), zFinal);

						if("chicken_hat".equals(player.getSelectedHat())) {
							jugador.launchProjectile(Egg.class, direccion);
						} else {
							jugador.launchProjectile(Snowball.class, direccion);
						}

						direccion = jugador.getLocation().getDirection().clone();
						anguloEntre = Math.toRadians(-10);

						xFinal = direccion.getX()*Math.cos(anguloEntre)-direccion.getZ()*Math.sin(anguloEntre);
						zFinal = direccion.getX()*Math.sin(anguloEntre)+direccion.getZ()*Math.cos(anguloEntre);

						direccion = new Vector(xFinal, direccion.getY(), zFinal);

						if("chicken_hat".equals(player.getSelectedHat())) {
							jugador.launchProjectile(Egg.class, direccion);
						} else {
							jugador.launchProjectile(Snowball.class, direccion);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void lanzarSnowball(ProjectileLaunchEvent event) {
		Projectile p = event.getEntity();
		ProjectileSource source = p.getShooter();
		FileConfiguration config = plugin.getConfig();
		if((p.getType().equals(EntityType.SNOWBALL) || p.getType().equals(EntityType.EGG)) && source instanceof Player jugador) {
			Partida partida = plugin.getPartidaJugador(jugador.getName());
			if(partida != null) {
				p.setMetadata("BetterPaintballSystem", new FixedMetadataValue(plugin, "proyectil"));
				p.setVelocity(p.getVelocity().multiply(1.25));
				JugadorPaintball player = partida.getJugador(jugador.getName());
				if(player == null) return;

				Killstreak k = player.getKillstreak("strong_arm");
				if(k != null) {
					p.setVelocity(p.getVelocity().multiply(2));
				}
				String particle = config.getString("snowball_particle", "none");
				if(!"none".equals(particle)) {
					if(!"chicken_hat".equals(player.getSelectedHat())) {
						CooldownSnowballParticle c = new CooldownSnowballParticle(plugin, p, particle);
						c.cooldown();
					}
				}
			}
		}
	}

	@EventHandler
	public void tirarHuevo(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		if(egg.hasMetadata("BetterPaintballSystem")) {
			event.setHatching(false);
		}
	}
}