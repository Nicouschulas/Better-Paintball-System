package de.nicouschulas.betterpaintballsystem;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import org.jspecify.annotations.NonNull;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.juego.PartidaEditando;
import de.nicouschulas.betterpaintballsystem.managers.Checks;
import de.nicouschulas.betterpaintballsystem.managers.InventarioAdmin;
import de.nicouschulas.betterpaintballsystem.managers.InventarioShop;
import de.nicouschulas.betterpaintballsystem.managers.PartidaManager;
import de.nicouschulas.betterpaintballsystem.managers.TopHologram;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesOtros;

public class Comando implements CommandExecutor {

	BetterPaintballSystem plugin;
	public Comando(BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args){
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "&7[&cPaintball&7]"))+" ";
		if (!(sender instanceof Player jugador)){
			if(args.length >= 1) {
				if(args[0].equalsIgnoreCase("givecoins")) {
					// /paintball givecoins <player> <amount>
					giveCoins(sender,args,messages,prefix);
				}else if(args[0].equalsIgnoreCase("reload")) {
					// /paintball reload
					plugin.reloadConfig();
					plugin.reloadMessages();
					plugin.reloadShop();
					plugin.recargarCarteles();
					plugin.recargarScoreboard();
					plugin.recargarHologramas();
					sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("configReloaded", "&aConfig Reloaded!")));
				}
			}
			return false;
		}
		if(args.length >= 1) {

			if(args[0].equalsIgnoreCase("create")) {
				// /paintball create <name>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if(args.length >= 2) {
						if(plugin.getPartida(args[1]) == null) {
							FileConfiguration config = plugin.getConfig();
							if(!config.contains("MainLobby")) {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noMainLobby", "&cBefore creating an arena you need to set the Main Lobby with: &7/paintball setmainlobby")));
								return true;
							}
							String equipo1 = "";
							String equipo2 = "";
							int i=0;

							ConfigurationSection teamsSection = config.getConfigurationSection("teams");
							if (teamsSection != null) {
								for(String key : teamsSection.getKeys(false)) {
									if(i==0) {
										equipo1 = key;
									}else {
										equipo2 = key;
										break;
									}
									i++;
								}
							}

							int time = config.getInt("arena_time_default", 300);
							int lives = config.getInt("team_starting_lives_default", 5);

							Partida partida = new Partida(args[1], time, equipo1, equipo2, lives);
							plugin.agregarPartida(partida);

							String msgCreated = messages.getString("arenaCreated", "");
							String msgCreatedExtra = messages.getString("arenaCreatedExtraInfo", "");
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgCreated.replace("%name%", args[1])));
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgCreatedExtra.replace("%name%", args[1])));
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyExists", "&cThat arena already exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateErrorUse", "&cYou need to use &7/paintball create <arena>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("delete")) {
				// /paintball delete <name>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if(args.length >= 2) {
						if(plugin.getPartida(args[1]) != null) {
							plugin.removerPartida(args[1]);
							String msgDeleted = messages.getString("arenaDeleted", "");
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgDeleted.replace("%name%", args[1])));
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists", "&cThat arena doesn't exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDeleteErrorUse", "&cYou need to use &7/paintball delete <arena>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("reload")) {
				// /paintball reload
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					plugin.reloadConfig();
					plugin.reloadMessages();
					plugin.reloadShop();
					plugin.recargarCarteles();
					plugin.recargarScoreboard();
					plugin.recargarHologramas();
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("configReloaded", "&aConfig Reloaded!")));
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("setmainlobby")) {
				// /paintball setmainlobby
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					FileConfiguration config = plugin.getConfig();

					Location l = jugador.getLocation();
					config.set("MainLobby.x", l.getX()+"");
					config.set("MainLobby.y", l.getY()+"");
					config.set("MainLobby.z", l.getZ()+"");
					if (l.getWorld() != null) {
						config.set("MainLobby.world", l.getWorld().getName());
					}
					config.set("MainLobby.pitch", l.getPitch());
					config.set("MainLobby.yaw", l.getYaw());
					plugin.saveConfig();
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("mainLobbyDefined", "&aMain Lobby defined!")));
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("join")) {
				// /paintball join <arena>
				if(!Checks.checkTodo(plugin, jugador)) {
					return false;
				}
				if(args.length >= 2) {
					Partida partida = plugin.getPartida(args[1]);
					if(partida != null) {
						if(partida.estaActivada()) {
							if(plugin.getPartidaJugador(jugador.getName()) == null) {
								if(!partida.estaIniciada()) {
									if(!partida.estaLlena()) {
										if(!UtilidadesOtros.pasaConfigInventario(jugador, plugin.getConfig())) {
											jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorClearInventory", "&c&lERROR! &7To join an arena clear your inventory first!")));
											return true;
										}
										PartidaManager.jugadorEntra(partida, jugador,plugin);
									}else {
										jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaIsFull", "&cThat arena is full!")));
									}
								}else {
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyStarted", "&cThat arena already started!")));
								}
							}else {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena", "&cYou are already in a game!")));
							}
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabledError", "&cThat arena is disabled!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists", "&cThat arena doesn't exists!")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandJoinErrorUse", "&cYou need to use &7/paintball join <arena>")));
				}
			}else if(args[0].equalsIgnoreCase("joinrandom")) {
				// /paintball joinrandom
				if(plugin.getPartidaJugador(jugador.getName()) == null) {
					Partida partidaNueva = PartidaManager.getPartidaDisponible(plugin);
					if(partidaNueva == null) {
						jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("noArenasAvailable", "&cThere are no arenas available!")));
					}else {
						PartidaManager.jugadorEntra(partidaNueva, jugador, plugin);
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena", "&cYou are already in a game!")));
				}
			}else if(args[0].equalsIgnoreCase("leave")) {
				// /paintball leave
				Partida partida = plugin.getPartidaJugador(jugador.getName());
				if(partida != null) {
					PartidaManager.jugadorSale(partida, jugador, false, plugin, false);
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("notInAGame", "&cYou are not in a game.")));
				}
			}else if(args[0].equalsIgnoreCase("shop")) {
				// /paintball shop
				if(!Checks.checkTodo(plugin, jugador)) {
					return false;
				}
				InventarioShop.crearInventarioPrincipal(jugador, plugin);
			}else if(args[0].equalsIgnoreCase("enable")) {
				// /paintball enable <arena>
				//To activate an arena, everything must be defined
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if(args.length >= 2) {
						Partida partida = plugin.getPartida(args[1]);
						if(partida != null) {
							if(partida.estaActivada()) {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyEnabled", "&cThat arena is already enabled.")));
							}else {
								if(partida.getLobby() == null) {
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaLobbyError", "&cTo enable the arena you need to define the Lobby first!")));
									return true;
								}
								if(partida.getTeam1().getSpawn() == null) {
									String msgSpawnErr = messages.getString("enableArenaSpawnError", "");
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgSpawnErr.replace("%number%", "1")));
									return true;
								}
								if(partida.getTeam2().getSpawn() == null) {
									String msgSpawnErr = messages.getString("enableArenaSpawnError", "");
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgSpawnErr.replace("%number%", "2")));
									return true;
								}

								partida.setEstado(EstadoPartida.ESPERANDO);
								String msgEnabled = messages.getString("arenaEnabled", "");
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgEnabled.replace("%name%", args[1])));
							}
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists", "&cThat arena doesn't exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandEnableErrorUse", "&cYou need to use &7/paintball enable <arena>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("disable")) {
				// /paintball disable <arena>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if(args.length >= 2) {
						Partida partida = plugin.getPartida(args[1]);
						if(partida != null) {
							if(!partida.estaActivada()) {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyDisabled", "&cThat arena is already disabled.")));
							}else {
								partida.setEstado(EstadoPartida.DESACTIVADA);
								String msgDisabled = messages.getString("arenaDisabled", "");
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgDisabled.replace("%name%", args[1])));
							}
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists", "&cThat arena doesn't exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDisableErrorUse", "&cYou need to use &7/paintball disable <arena>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("edit")) {
				// /paintball edit <arena>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if(!Checks.checkTodo(plugin, jugador)) {
						return false;
					}
					if(args.length >= 2) {
						Partida partida = plugin.getPartida(args[1]);
						if(partida != null) {
							if(!partida.estaActivada()) {
								PartidaEditando p = plugin.getPartidaEditando();
								if(p == null) {

									InventarioAdmin.crearInventario(jugador,partida,plugin);
								}else {
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaModifyingError", "&cOnly one arena can be modified at a time.")));
								}
							}else {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaMustBeDisabled", "&cThe arena must be disable to do that.")));
							}
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists", "&cThat arena doesn't exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandAdminErrorUse", "&cYou need to use &7/paintball edit <arena>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("createtophologram")) {
				// /paintball createtophologram <name> kills/wins <global/monthly/weekly>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need DecentHolograms plugin to use this feature!")); // TODO add message key
						return true;
					}
					if(args.length >= 3) {
						if(args[2].equalsIgnoreCase("kills") || args[2].equalsIgnoreCase("wins")) {
							TopHologram topHologram = plugin.getTopHologram(args[1]);
							if(topHologram == null) {
								String period = "global";
								if(args.length >= 4) {
									period = args[3];
								}
								if(period.equalsIgnoreCase("global") || period.equalsIgnoreCase("monthly") || period.equalsIgnoreCase("weekly")) {
									if(!MySQL.isEnabled(plugin.getConfig()) && (period.equalsIgnoreCase("monthly") || period.equalsIgnoreCase("weekly"))) {
										jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramPeriodSQLError", "&cTo create Weekly or Monthly Holograms you need to set up a MySQL database!")));
										return true;
									}
									TopHologram hologram = new TopHologram(args[1],args[2],jugador.getLocation(),plugin,period);
									plugin.agregarTopHolograma(hologram);
									hologram.spawnHologram(plugin);
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramCreated", "&aTop Hologram created!")));
								}else {
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse", "&cYou need to use &7/paintball createtophologram <name> <kills/wins> <global/monthly/weekly>")));
								}
							}else {
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramAlreadyExists", "&cThat hologram already exists! Use another name.")));
							}
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse", "&cYou need to use &7/paintball createtophologram <name> <kills/wins> <global/monthly/weekly>")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse", "&cYou need to use &7/paintball createtophologram <name> <kills/wins> <global/monthly/weekly>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("removetophologram")) {
				// /paintball removetophologram <name>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need DecentHolograms plugin to use this feature!")); // TODO add message key
						return true;
					}
					if(args.length >= 2) {
						TopHologram topHologram = plugin.getTopHologram(args[1]);
						if(topHologram != null) {
							plugin.eliminarTopHologama(args[1]);
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramRemoved", "&aTop Hologram removed!")));
						}else {
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramDoesNotExists", "&cThat hologram doesn't exists!")));
						}
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandRemoveHologramErrorUse", "&cYou need to use &7/paintball removetopholgram <name>")));
					}
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}else if(args[0].equalsIgnoreCase("givecoins")) {
				// /paintball givecoins <player> <amount>
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					giveCoins(sender,args,messages,prefix);
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}
			}
			else {
				// /paintball help or any other command
				if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
					enviarAyuda(jugador);
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
				}

			}
		}else {
			if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				enviarAyuda(jugador);
			}else {
				jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions", "&cYou don't have permissions to use that command!")));
			}
		}
		return true;
	}

	public void giveCoins(CommandSender sender, String[] args, FileConfiguration messages, String prefix) {
		if(args.length >= 3) {
			String player = args[1];
			try {
				int amount = Integer.parseInt(args[2]);
				//If the player is not in the database or a file, they MUST be online to receive coins.
				if(MySQL.isEnabled(plugin.getConfig())) {
					if(MySQL.jugadorExiste(plugin, player)) {
						MySQL.agregarCoinsJugadorAsync(plugin, player, amount);
						String msgGive = messages.getString("giveCoinsMessage", "&aYou gave &e%amount% &acoins to &e%player%&a.");
						sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgGive.replace("%player%", player).replace("%amount%", amount+"")));
						Player p = Bukkit.getPlayer(player);
						if(p != null) {
							String msgReceive = messages.getString("receiveCoinsMessage", "&aYou received &e%amount% &acoins.");
							p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgReceive.replace("%amount%", amount+"")));
						}
					}else {
						Player p = Bukkit.getPlayer(player);
						if(p != null) {
							MySQL.crearJugadorPartidaAsync(plugin, p.getUniqueId().toString(), p.getName(), "", 0, 0, 0, 0, amount, 1);
							String msgGive = messages.getString("giveCoinsMessage", "&aYou gave &e%amount% &acoins to &e%player%&a.");
							String msgReceive = messages.getString("receiveCoinsMessage", "&aYou received &e%amount% &acoins.");
							sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgGive.replace("%player%", player).replace("%amount%", amount+"")));
							p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgReceive.replace("%amount%", amount+"")));
						}else {
							sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline", "&cThe player must be online to do that!")));
						}
					}
				}else {
					Player p = Bukkit.getPlayer(player);
					if(p != null) {
						plugin.registerPlayer(p.getUniqueId() +".yml");
						if(plugin.getJugador(p.getName()) == null) {
							plugin.agregarJugadorDatos(new JugadorDatos(p.getName(),p.getUniqueId().toString(),0,0,0,0,0, new ArrayList<>(), new ArrayList<>()));
						}
						JugadorDatos jDatos = plugin.getJugador(p.getName());
						if (jDatos != null) {
							jDatos.aumentarCoins(amount);
						}
						String msgGive = messages.getString("giveCoinsMessage", "&aYou gave &e%amount% &acoins to &e%player%&a.");
						String msgReceive = messages.getString("receiveCoinsMessage", "&aYou received &e%amount% &acoins.");
						sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgGive.replace("%player%", player).replace("%amount%", amount+"")));
						p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', msgReceive.replace("%amount%", amount+"")));
					}else {
						sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline", "&cThe player must be online to do that!")));
					}
				}

			}catch(NumberFormatException e) {
				sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError", "&cYou need to use a valid number!")));
			}

		}else {
			sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandGiveCoinsErrorUse", "&cYou need to use &7/paintball givecoins <player> <amount>")));
		}
	}
	
	public void enviarAyuda(Player jugador) {
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7&m--------------------&r&7[&cPaintball&7]&7&m--------------------&r"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball create <arena> &7- &7Creates a new arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball delete <arena> &7- &7Deletes an arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball join <arena> &7- &7Joins an arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball joinrandom &7- &7Joins a random arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball leave &7- &7Leaves from the arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball shop &7- &7Opens the Paintball Shop"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball givecoins <player> <amount> &7- &7Gives a player coins"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball setmainlobby &7- &7Defines the minigame main lobby"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball enable <arena> &7- &7Enables an arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball disable <arena> &7- &7Disables an arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball edit <arena> &7- &7Edit the properties of an arena"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball createtophologram <name> <kills/wins> <global/monthly/weekly> &7- &7Creates a top hologram"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball removetophologram <name> &7- &7Removes a top hologram"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7/&cpaintball reload &7- &7Reloads the configuration files"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7&m------------------------------------------------&r"));
	}
}
