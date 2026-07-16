package de.nicouschulas.betterpaintballsystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import de.nicouschulas.betterpaintballsystem.api.ExpansionPaintballBattle;
import de.nicouschulas.betterpaintballsystem.api.Hat;
import de.nicouschulas.betterpaintballsystem.api.PaintballAPI;
import de.nicouschulas.betterpaintballsystem.api.Perk;
import de.nicouschulas.betterpaintballsystem.database.ConexionDatabase;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;
import de.nicouschulas.betterpaintballsystem.juego.Partida;
import de.nicouschulas.betterpaintballsystem.juego.PartidaEditando;
import de.nicouschulas.betterpaintballsystem.managers.UpdateManager;
import de.nicouschulas.betterpaintballsystem.managers.CartelesAdmin;
import de.nicouschulas.betterpaintballsystem.managers.CartelesListener;
import de.nicouschulas.betterpaintballsystem.managers.Checks;
import de.nicouschulas.betterpaintballsystem.managers.CooldownKillstreaksActionbar;
import de.nicouschulas.betterpaintballsystem.managers.InventarioAdmin;
import de.nicouschulas.betterpaintballsystem.managers.InventarioHats;
import de.nicouschulas.betterpaintballsystem.managers.InventarioShop;
import de.nicouschulas.betterpaintballsystem.managers.PartidaListener;
import de.nicouschulas.betterpaintballsystem.managers.PartidaListenerNew;
import de.nicouschulas.betterpaintballsystem.managers.PartidaManager;
import de.nicouschulas.betterpaintballsystem.managers.ScoreboardAdmin;
import de.nicouschulas.betterpaintballsystem.managers.TopHologram;
import de.nicouschulas.betterpaintballsystem.managers.TopHologramAdmin;
import de.nicouschulas.betterpaintballsystem.utils.ServerVersion;


public class BetterPaintballSystem extends JavaPlugin {
  
	PluginDescriptionFile pdfFile = getDescription();
	public String version = pdfFile.getVersion();
	public static String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&cPaintball&7] ");
	private ArrayList<Partida> partidas;
	private FileConfiguration arenas = null;
	private File arenasFile = null;
	private FileConfiguration messages = null;
	private File messagesFile = null;
	private FileConfiguration shop = null;
	private File shopFile = null;
	private PartidaEditando partidaEditando;
	private ArrayList<PlayerConfig> configPlayers;
	private ArrayList<JugadorDatos> jugadoresDatos;
	private ArrayList<TopHologram> topHologramas;
	private FileConfiguration holograms = null;
	private File hologramsFile = null;
	private static Economy econ = null;	
	public boolean primeraVez = false;
	public String latestversion;
	
	public String rutaMessages;
	public String rutaConfig;
	
	private ScoreboardAdmin scoreboardTask;
	private CartelesAdmin cartelesTask;
	private TopHologramAdmin hologramasTask;
	
	private ConexionDatabase conexionDatabase;


	public static ServerVersion serverVersion;
	
	
	@SuppressWarnings("unused")
	public void onEnable(){
	   setVersion();
	   configPlayers = new ArrayList<>();
	   jugadoresDatos = new ArrayList<>();
	   topHologramas = new ArrayList<>();
	   registerEvents();
	   registerArenas();
	   registerConfig();
	   registerHolograms();
	   registerMessages();
	   createPlayersFolder();
	   registerPlayers();
	   registerShop();
	   cargarPartidas();
	   registerCommands();
	   
	   cargarJugadores();
	   setupEconomy();
	   
	   if(MySQL.isEnabled(getConfig())){
		   conexionDatabase = new ConexionDatabase(getConfig());
	   }
	   
	   scoreboardTask = new ScoreboardAdmin(this);
	   scoreboardTask.crearScoreboards();
	   cartelesTask = new CartelesAdmin(this);
	   cartelesTask.actualizarCarteles();
	   CooldownKillstreaksActionbar c = new CooldownKillstreaksActionbar(this);
	   c.crearActionbars();
	   
	   cargarTopHologramas();
	   hologramasTask = new TopHologramAdmin(this);
	   hologramasTask.actualizarHologramas();

	   PaintballAPI.init(this);
	   if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
		   new ExpansionPaintballBattle(this).register();
	   }
	   
	   Checks.checkearYModificar(this, primeraVez);
	   Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);

        UpdateManager updateManager = new UpdateManager(this);
        getServer().getPluginManager().registerEvents(updateManager, this);
        updateManager.checkForUpdates();
	}

	public void onDisable(){
		if(partidas != null) {
            for (Partida partida : partidas) {
                if (!partida.getEstado().equals(EstadoPartida.DESACTIVADA)) {
                    PartidaManager.finalizarPartida(partida, this, true, null);
                }
            }
		}
		guardarPartidas();
		guardarJugadores();
		guardarTopHologramas();
		
		Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
	}
	
	public void setVersion(){
		String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
		switch(bukkitVersion){
			case "1.20.5":
			case "1.20.6":
				serverVersion = ServerVersion.v1_20_R4;
				break;
			case "1.21":
			case "1.21.1":
			case "1.21.4":
			case "1.21.5":	
				serverVersion = ServerVersion.v1_21_R1;
				break;
			default:
				serverVersion = ServerVersion.detect();
		}
	}
	
	public void recargarScoreboard() {
		int taskID = scoreboardTask.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		scoreboardTask = new ScoreboardAdmin(this);
		scoreboardTask.crearScoreboards();
	}
	
	public void recargarCarteles() {
		int taskID = cartelesTask.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		cartelesTask = new CartelesAdmin(this);
		cartelesTask.actualizarCarteles();
	}
	
	public void recargarHologramas() {
		int taskID = hologramasTask.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		hologramasTask = new TopHologramAdmin(this);
		hologramasTask.actualizarHologramas();
	}
	
	public void setPartidaEditando(PartidaEditando p) {
		this.partidaEditando = p;
	}
	
	public void removerPartidaEditando() {
		this.partidaEditando = null;
	}
	
	public PartidaEditando getPartidaEditando() {
		return this.partidaEditando;
	}
	
	public ConexionDatabase getConexionDatabase() {
		return this.conexionDatabase;
	}	
	
	private boolean setupEconomy() {
		  if (getServer().getPluginManager().getPlugin("Vault") == null) {
	          return false;
	      }
	      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	      if (rsp == null) {
	          return false;
	      }
	      econ = rsp.getProvider();
	      return true;
	  }
	  
	public Economy getEconomy(){	
		return econ;
	}
	
	public void registerEvents(){
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PartidaListener(this), this);
		if(!Bukkit.getVersion().contains("1.8")) {
			pm.registerEvents(new PartidaListenerNew(this), this);
		}
		pm.registerEvents(new CartelesListener(this), this);
		pm.registerEvents(new InventarioAdmin(this), this);
		pm.registerEvents(new InventarioShop(this), this);
		pm.registerEvents(new InventarioHats(this), this);
	}

	public void registerCommands(){
		org.bukkit.command.PluginCommand paintballCommand = this.getCommand("paintball");
		if (paintballCommand != null) {
			paintballCommand.setExecutor(new Comando(this));
			paintballCommand.setTabCompleter(new ComandoTabCompleter(this));
		}
	}
	
	public Partida getPartidaJugador(String jugador) {
        for (Partida partida : partidas) {
            ArrayList<JugadorPaintball> jugadores = partida.getJugadores();
            for (JugadorPaintball jugadore : jugadores) {
                if (jugadore.getJugador().getName().equals(jugador)) {
                    return partida;
                }
            }
        }
		return null;
	}
	
	public ArrayList<Partida> getPartidas() {
		return this.partidas;
	}
	
	public Partida getPartida(String nombre) {
        for (Partida partida : partidas) {
            if (partida.getNombre().equals(nombre)) {
                return partida;
            }
        }
		return null;
	}
	
	public void agregarPartida(Partida partida) {
		this.partidas.add(partida);
	}

	public void removerPartida(String nombre) {
		partidas.removeIf(partida -> partida.getNombre().equals(nombre));
	}

	public void cargarPartidas() {
		this.partidas = new ArrayList<>();
		FileConfiguration arenas = getArenas();

		if(arenas.contains("Arenas") && arenas.getConfigurationSection("Arenas") != null) {
			for(String key : arenas.getConfigurationSection("Arenas").getKeys(false)) {
				int min_players = arenas.getInt("Arenas."+key+".min_players", 0);
				int max_players = arenas.getInt("Arenas."+key+".max_players", 0);
				int time = arenas.getInt("Arenas."+key+".time", 0);
				int vidas = arenas.getInt("Arenas."+key+".lives", 0);

				Location lLobby = null;
				if(arenas.contains("Arenas."+key+".Lobby")) {
					double xLobby = arenas.getDouble("Arenas."+key+".Lobby.x", 0.0);
					double yLobby = arenas.getDouble("Arenas."+key+".Lobby.y", 0.0);
					double zLobby = arenas.getDouble("Arenas."+key+".Lobby.z", 0.0);
					String worldLobby = arenas.getString("Arenas."+key+".Lobby.world");
					float pitchLobby = (float) arenas.getDouble("Arenas."+key+".Lobby.pitch", 0.0);
					float yawLobby = (float) arenas.getDouble("Arenas."+key+".Lobby.yaw", 0.0);

					if (worldLobby != null && Bukkit.getWorld(worldLobby) != null) {
						lLobby = new Location(Bukkit.getWorld(worldLobby), xLobby, yLobby, zLobby, yawLobby, pitchLobby);
					}
				}

				String nombreTeam1 = arenas.getString("Arenas."+key+".Team1.name", "Team1");

				Location lSpawnTeam1 = null;
				if(arenas.contains("Arenas."+key+".Team1.Spawn")) {
					double xSpawnTeam1 = arenas.getDouble("Arenas."+key+".Team1.Spawn.x", 0.0);
					double ySpawnTeam1 = arenas.getDouble("Arenas."+key+".Team1.Spawn.y", 0.0);
					double zSpawnTeam1 = arenas.getDouble("Arenas."+key+".Team1.Spawn.z", 0.0);
					String worldSpawnTeam1 = arenas.getString("Arenas."+key+".Team1.Spawn.world");
					float pitchSpawnTeam1 = (float) arenas.getDouble("Arenas."+key+".Team1.Spawn.pitch", 0.0);
					float yawSpawnTeam1 = (float) arenas.getDouble("Arenas."+key+".Team1.Spawn.yaw", 0.0);

					if (worldSpawnTeam1 != null && Bukkit.getWorld(worldSpawnTeam1) != null) {
						lSpawnTeam1 = new Location(Bukkit.getWorld(worldSpawnTeam1), xSpawnTeam1, ySpawnTeam1, zSpawnTeam1, yawSpawnTeam1, pitchSpawnTeam1);
					}
				}

				String nombreTeam2 = arenas.getString("Arenas."+key+".Team2.name", "Team2");
				Location lSpawnTeam2 = null;
				if(arenas.contains("Arenas."+key+".Team2.Spawn")) {
					double xSpawnTeam2 = arenas.getDouble("Arenas."+key+".Team2.Spawn.x", 0.0);
					double ySpawnTeam2 = arenas.getDouble("Arenas."+key+".Team2.Spawn.y", 0.0);
					double zSpawnTeam2 = arenas.getDouble("Arenas."+key+".Team2.Spawn.z", 0.0);
					String worldSpawnTeam2 = arenas.getString("Arenas."+key+".Team2.Spawn.world");
					float pitchSpawnTeam2 = (float) arenas.getDouble("Arenas."+key+".Team2.Spawn.pitch", 0.0);
					float yawSpawnTeam2 = (float) arenas.getDouble("Arenas."+key+".Team2.Spawn.yaw", 0.0);

					if (worldSpawnTeam2 != null && Bukkit.getWorld(worldSpawnTeam2) != null) {
						lSpawnTeam2 = new Location(Bukkit.getWorld(worldSpawnTeam2), xSpawnTeam2, ySpawnTeam2, zSpawnTeam2, yawSpawnTeam2, pitchSpawnTeam2);
					}
				}

				Partida partida = new Partida(key,time,nombreTeam1,nombreTeam2,vidas);
				if("random".equalsIgnoreCase(nombreTeam1)) {
					partida.getTeam1().setRandom(true);
				}
				if("random".equalsIgnoreCase(nombreTeam2)) {
					partida.getTeam2().setRandom(true);
				}
				partida.modificarTeams(getConfig());
				partida.setCantidadMaximaJugadores(max_players);
				partida.setCantidadMinimaJugadores(min_players);
				partida.setLobby(lLobby);
				partida.getTeam1().setSpawn(lSpawnTeam1);
				partida.getTeam2().setSpawn(lSpawnTeam2);

				String enabled = arenas.getString("Arenas."+key+".enabled", "false");
				if("true".equals(enabled)) {
					partida.setEstado(EstadoPartida.ESPERANDO);
				}else {
					partida.setEstado(EstadoPartida.DESACTIVADA);
				}

				this.partidas.add(partida);
			}
		}
	}
	
	public void guardarPartidas() {
		  FileConfiguration arenas = getArenas();
		  arenas.set("Arenas", null);
		  for(Partida p : this.partidas) {
			  String nombre = p.getNombre();
			  arenas.set("Arenas."+nombre+".min_players", p.getCantidadMinimaJugadores()+"");
			  arenas.set("Arenas."+nombre+".max_players", p.getCantidadMaximaJugadores()+"");
			  arenas.set("Arenas."+nombre+".time", p.getTiempoMaximo()+"");
			  arenas.set("Arenas."+nombre+".lives", p.getVidasIniciales()+"");
			  Location lLobby = p.getLobby();
			  if(lLobby != null) {
				  arenas.set("Arenas."+nombre+".Lobby.x", lLobby.getX()+"");
				  arenas.set("Arenas."+nombre+".Lobby.y", lLobby.getY()+"");
				  arenas.set("Arenas."+nombre+".Lobby.z", lLobby.getZ()+"");
				  arenas.set("Arenas."+nombre+".Lobby.world", lLobby.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Lobby.pitch", lLobby.getPitch());
				  arenas.set("Arenas."+nombre+".Lobby.yaw", lLobby.getYaw());
			  }
			  
			  Location lSpawnTeam1 = p.getTeam1().getSpawn();
			  if(lSpawnTeam1 != null) {
				  arenas.set("Arenas."+nombre+".Team1.Spawn.x", lSpawnTeam1.getX()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.y", lSpawnTeam1.getY()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.z", lSpawnTeam1.getZ()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.world", lSpawnTeam1.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Team1.Spawn.pitch", lSpawnTeam1.getPitch());
				  arenas.set("Arenas."+nombre+".Team1.Spawn.yaw", lSpawnTeam1.getYaw());
			  }
			  if(p.getTeam1().esRandom()) {
				  arenas.set("Arenas."+nombre+".Team1.name", "random");
			  }else {
				  arenas.set("Arenas."+nombre+".Team1.name", p.getTeam1().getTipo()); 
			  }
			  
			  
			  Location lSpawnTeam2 = p.getTeam2().getSpawn();
			  if(lSpawnTeam2 != null) {
				  arenas.set("Arenas."+nombre+".Team2.Spawn.x", lSpawnTeam2.getX()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.y", lSpawnTeam2.getY()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.z", lSpawnTeam2.getZ()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.world", lSpawnTeam2.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Team2.Spawn.pitch", lSpawnTeam2.getPitch());
				  arenas.set("Arenas."+nombre+".Team2.Spawn.yaw", lSpawnTeam2.getYaw());
			  }
			  if(p.getTeam2().esRandom()) {
				  arenas.set("Arenas."+nombre+".Team2.name", "random");
			  }else {
				  arenas.set("Arenas."+nombre+".Team2.name", p.getTeam2().getTipo()); 
			  }
			  
			  if(p.getEstado().equals(EstadoPartida.DESACTIVADA)) {
				  arenas.set("Arenas."+nombre+".enabled", "false");
			  }else {
				  arenas.set("Arenas."+nombre+".enabled", "true");
			  }
		  }
		  this.saveArenas();
	  }
	
	 public void registerArenas(){
		  arenasFile = new File(this.getDataFolder(), "arenas.yml");
		  if(!arenasFile.exists()){
		    	this.getArenas().options().copyDefaults(true);
				saveArenas();
		    }
	  }
	  public void saveArenas() {
		 try {
			 arenas.save(arenasFile);
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	  public FileConfiguration getArenas() {
		    if (arenas == null) {
		        reloadArenas();
		    }
		    return arenas;
		}
	  
	  public void reloadArenas() {
		    if (arenas == null) {
		    	arenasFile = new File(getDataFolder(), "arenas.yml");
		    }
		    arenas = YamlConfiguration.loadConfiguration(arenasFile);

		    Reader defConfigStream;
          defConfigStream = new InputStreamReader(this.getResource("arenas.yml"), StandardCharsets.UTF_8);
          YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
          arenas.setDefaults(defConfig);
      }
	  
	  public void registerConfig(){	
			File config = new File(this.getDataFolder(), "config.yml");
			rutaConfig = config.getPath();
		    if(!config.exists()){
		    	this.primeraVez = true;
		    	this.getConfig().options().copyDefaults(true);
				saveConfig();  
		    }
	  }
	  
	  public void registerShop(){
		  shopFile = new File(this.getDataFolder(), "shop.yml");
			if(!shopFile.exists()){
				this.getShop().options().copyDefaults(true);
				saveShop();
			}
		}
		
		public void saveShop() {
			try {
				shop.save(shopFile);
			}catch (IOException e) {
				 e.printStackTrace();
		 	}
		}
		  
		public FileConfiguration getShop() {
			if (shop == null) {
			   reloadShop();
			}
			return shop;
		}
		  
		public void reloadShop() {
			if (shop == null) {
				shopFile = new File(getDataFolder(), "shop.yml");
			}
			shop = YamlConfiguration.loadConfiguration(shopFile);
			Reader defConfigStream;
            defConfigStream = new InputStreamReader(this.getResource("shop.yml"), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            shop.setDefaults(defConfig);
        }
	  
	  public void registerMessages(){
		  messagesFile = new File(this.getDataFolder(), "messages.yml");
		  rutaMessages = messagesFile.getPath();
			if(!messagesFile.exists()){
				this.getMessages().options().copyDefaults(true);
				saveMessages();
			}
		}
		
		public void saveMessages() {
			try {
				messages.save(messagesFile);
			}catch (IOException e) {
				 e.printStackTrace();
		 	}
		}
		  
		public FileConfiguration getMessages() {
			if (messages == null) {
			   reloadMessages();
			}
			return messages;
		}
		  
		public void reloadMessages() {
			if (messages == null) {
			    messagesFile = new File(getDataFolder(), "messages.yml");
			}
			messages = YamlConfiguration.loadConfiguration(messagesFile);
			Reader defConfigStream;
            defConfigStream = new InputStreamReader(this.getResource("messages.yml"), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            messages.setDefaults(defConfig);
        }
	
		public void createPlayersFolder(){
			File folder;
	        try {
	            folder = new File(this.getDataFolder() + File.separator + "players");
	            if(!folder.exists()){
	                folder.mkdirs();
	            }
	        } catch(SecurityException e) {
	            folder = null;
	        }
		}
		
		public void savePlayers() {
            for (PlayerConfig configPlayer : configPlayers) {
                configPlayer.savePlayerConfig();
            }
		}
		
		public void registerPlayers(){
			String path = this.getDataFolder() + File.separator + "players";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    String pathName = listOfFile.getName();
                    PlayerConfig config = new PlayerConfig(pathName, this);
                    config.registerPlayerConfig();
                    configPlayers.add(config);
                }
            }
		}
		
		public ArrayList<PlayerConfig> getConfigPlayers(){
			return this.configPlayers;
		}
		
		public boolean archivoYaRegistrado(String pathName) {
            for (PlayerConfig configPlayer : configPlayers) {
                if (configPlayer.getPath().equals(pathName)) {
                    return true;
                }
            }
			return false;
		}
		
		public PlayerConfig getPlayerConfig(String pathName) {
            for (PlayerConfig configPlayer : configPlayers) {
                if (configPlayer.getPath().equals(pathName)) {
                    return configPlayer;
                }
            }
			return null;
		}
		public ArrayList<PlayerConfig> getPlayerConfigs() {
			return this.configPlayers;
		}
		
		public boolean registerPlayer(String pathName) {
			if(!archivoYaRegistrado(pathName)) {
				PlayerConfig config = new PlayerConfig(pathName,this);
		        config.registerPlayerConfig();
		        configPlayers.add(config);
		        return true;
			}else {
				return false;
			}
		}
		
		public void removerConfigPlayer(String path) {
			for(int i=0;i<configPlayers.size();i++) {
				if(configPlayers.get(i).getPath().equals(path)) {
					configPlayers.remove(i);
				}
			}
		}
	
		public void cargarJugadores() {
			if(!MySQL.isEnabled(getConfig())) {
				for(PlayerConfig playerConfig : configPlayers) {
					FileConfiguration players = playerConfig.getConfig();
					String jugador = players.getString("name");
					int kills = 0;
					int wins = 0;
					int loses = 0;
					int ties = 0;
					int coins = 0;
					
					if(players.contains("kills")) {
						kills = Integer.parseInt(players.getString("kills"));
					}
					if(players.contains("wins")) {
						wins = Integer.parseInt(players.getString("wins"));
					}
					if(players.contains("loses")) {
						loses = Integer.parseInt(players.getString("loses"));
					}
					if(players.contains("ties")) {
						ties = Integer.parseInt(players.getString("ties"));
					}
					if(players.contains("coins")) {
						coins = Integer.parseInt(players.getString("coins"));
					}
					ArrayList<Perk> perks = new ArrayList<>();
					if(players.contains("perks")) {
						List<String> listaPerks = players.getStringList("perks");
                        for (String listaPerk : listaPerks) {
                            String[] separados = listaPerk.split(";");
                            Perk p = new Perk(separados[0], Integer.parseInt(separados[1]));
                            perks.add(p);
                        }
					}
					ArrayList<Hat> hats = new ArrayList<>();
					if(players.contains("hats")) {
						List<String> listaHats = players.getStringList("hats");
                        for (String listaHat : listaHats) {
                            String[] separados = listaHat.split(";");
                            Hat h = new Hat(separados[0], Boolean.parseBoolean(separados[1]));
                            hats.add(h);
                        }
					}
					
						
					this.agregarJugadorDatos(new JugadorDatos(jugador,playerConfig.getPath().replace(".yml", ""),wins,loses,ties,kills,coins,perks,hats));
				}
			}
		}
		
		public void guardarJugadores() {
			if(!MySQL.isEnabled(getConfig())) {
				for(JugadorDatos j : jugadoresDatos) {
					String jugador = j.getName();
					PlayerConfig playerConfig = getPlayerConfig(j.getUUID()+".yml");
					FileConfiguration players = playerConfig.getConfig();
					players.set("name", jugador);
					players.set("kills", j.getKills());
					players.set("wins", j.getWins());
					players.set("ties", j.getTies());
					players.set("loses", j.getLoses());
					players.set("coins", j.getCoins());
					
					List<String> listaPerks = new ArrayList<>();
					ArrayList<Perk> perks = j.getPerks();
					for(Perk p : perks) {
						listaPerks.add(p.getName()+";"+p.getNivel());
					}
					players.set("perks", listaPerks);
					
					List<String> listaHats = new ArrayList<>();
					ArrayList<Hat> hats = j.getHats();
					for(Hat h : hats) {
						listaHats.add(h.getName()+";"+h.isSelected());
					}
					players.set("hats", listaHats);
				}
				savePlayers();
			}
		}
		
		public void agregarJugadorDatos(JugadorDatos jugador) {
			jugadoresDatos.add(jugador);
		}
		
		public JugadorDatos getJugador(String jugador) {
			for(JugadorDatos j : jugadoresDatos) {
				if(j != null && j.getName() != null && j.getName().equals(jugador)) {
					return j;
				}
			}
			return null;
		}
		
		public ArrayList<JugadorDatos> getJugadores(){
			return this.jugadoresDatos;
		}
		
		public void registerHolograms(){
			  hologramsFile = new File(this.getDataFolder(), "holograms.yml");
			  if(!hologramsFile.exists()){
			    	this.getHolograms().options().copyDefaults(true);
					saveHolograms();
			    }
		  }

		public void saveHolograms() {
			try {
				holograms.save(hologramsFile);
			} catch (IOException e) {
				getLogger().log(java.util.logging.Level.SEVERE, "Could not save holograms.yml!", e);
			}
		}

		public FileConfiguration getHolograms() {
			if (holograms == null) {
				reloadHolograms();
			    }
			return holograms;
		}

		public void reloadHolograms() {
			if (holograms == null) {
				hologramsFile = new File(getDataFolder(), "holograms.yml");
			}
			holograms = YamlConfiguration.loadConfiguration(hologramsFile);

			java.io.InputStream resourceStream = this.getResource("holograms.yml");
			if (resourceStream != null) {
				Reader defConfigStream = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				holograms.setDefaults(defConfig);
			}
		}

		public void agregarTopHolograma(TopHologram topHologram) {
			this.topHologramas.add(topHologram);
		}

		public boolean eliminarTopHologama(String nombre) {
			for(int i=0;i<topHologramas.size();i++) {
				if(topHologramas.get(i).getName().equals(nombre)) {
					topHologramas.get(i).removeHologram();
					topHologramas.remove(i);
					return true;
				}
			}
			return false;
		}

			public TopHologram getTopHologram(String nombre) {
                for (TopHologram topHolograma : topHologramas) {
                    if (topHolograma.getName().equals(nombre)) {
                        return topHolograma;
                    }
                }
				return null;
			}

			public void guardarTopHologramas() {
				FileConfiguration holograms = getHolograms();
				holograms.set("Holograms", null);
                for (TopHologram topHolograma : topHologramas) {
                    Location l = topHolograma.getHologram().getLocation();
                    String name = topHolograma.getName();
                    String type = topHolograma.getType();
                    String period = topHolograma.getPeriod();
                    holograms.set("Holograms." + name + ".type", type);
                    holograms.set("Holograms." + name + ".period", period);
                    holograms.set("Holograms." + name + ".x", l.getX() + "");
                    holograms.set("Holograms." + name + ".y", topHolograma.getyOriginal() + "");
                    holograms.set("Holograms." + name + ".z", l.getZ() + "");
                    holograms.set("Holograms." + name + ".world", l.getWorld().getName());
                }
				saveHolograms();
			}

		public void cargarTopHologramas() {
			FileConfiguration holograms = getHolograms();
			org.bukkit.configuration.ConfigurationSection section = holograms.getConfigurationSection("Holograms");

			if (section != null) {
				for (String name : section.getKeys(false)) {
					String type = holograms.getString("Holograms." + name + ".type", "kills");
					double x = holograms.getDouble("Holograms." + name + ".x", 0.0);
					double y = holograms.getDouble("Holograms." + name + ".y", 0.0);
					double z = holograms.getDouble("Holograms." + name + ".z", 0.0);

					String worldName = holograms.getString("Holograms." + name + ".world");
					if (worldName == null) continue;

					World world = Bukkit.getWorld(worldName);
					if (world == null) continue;

					Location location = new Location(world, x, y, z);
					String period = holograms.getString("Holograms." + name + ".period", "global");

					TopHologram topHologram = new TopHologram(name, type, location, this, period);
					topHologram.spawnHologram(this);
					this.agregarTopHolograma(topHologram);
				}
			}
		}

		public ArrayList<TopHologram> getTopHologramas(){
			return this.topHologramas;
		}
}
