package de.nicouschulas.betterpaintballsystem;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
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
				   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("configReloaded"))); 
			   }
		   }
		   return false;   	
	   }
        if(args.length >= 1) {
		   
		   if(args[0].equalsIgnoreCase("create")) {
			   // /paintball create <nombre>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   if(plugin.getPartida(args[1]) == null) {
						   FileConfiguration config = plugin.getConfig();
						   if(!config.contains("MainLobby")) {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noMainLobby")));  
							   return true;
						   }
						   String equipo1 = "";
						   String equipo2 = "";
						   int i=0;
						   for(String key : config.getConfigurationSection("teams").getKeys(false)) {
							   if(i==0) {
								   equipo1 = key;
							   }else {
								   equipo2 = key;
								   break;
							   }
							   i++;
						   }
						   
						   Partida partida = new Partida(args[1],Integer.parseInt(config.getString("arena_time_default")),equipo1,equipo2,Integer.parseInt(config.getString("team_starting_lives_default")));
						   plugin.agregarPartida(partida);
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaCreated").replace("%name%", args[1]))); 
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaCreatedExtraInfo").replace("%name%", args[1]))); 
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyExists")));  
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateErrorUse"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("delete")) {
			   // /paintball delete <nombre>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   if(plugin.getPartida(args[1]) != null) {
						   plugin.removerPartida(args[1]);
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDeleted").replace("%name%", args[1]))); 
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));  
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDeleteErrorUse"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
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
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("configReloaded"))); 
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("setmainlobby")) {
			   // /paintball setmainlobby
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   FileConfiguration config = plugin.getConfig();
				   
				   Location l = jugador.getLocation();
				   config.set("MainLobby.x", l.getX()+"");
				   config.set("MainLobby.y", l.getY()+"");
				   config.set("MainLobby.z", l.getZ()+"");
				   config.set("MainLobby.world", l.getWorld().getName());
				   config.set("MainLobby.pitch", l.getPitch());
				   config.set("MainLobby.yaw", l.getYaw());
				   plugin.saveConfig();
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("mainLobbyDefined"))); 
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
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
										   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorClearInventory"))); 
										   return true;
									   }
									   PartidaManager.jugadorEntra(partida, jugador,plugin);
								   }else {
									   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaIsFull"))); 
								   }
							   }else {
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyStarted"))); 
							   }
						   }else {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena"))); 
						   }
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabledError"))); 
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandJoinErrorUse"))); 
			   }
		   }else if(args[0].equalsIgnoreCase("joinrandom")) {
			   // /paintball joinrandom
			   if(plugin.getPartidaJugador(jugador.getName()) == null) {
				    Partida partidaNueva = PartidaManager.getPartidaDisponible(plugin);
					if(partidaNueva == null) {
						jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("noArenasAvailable")));
					}else {
						PartidaManager.jugadorEntra(partidaNueva, jugador, plugin);
					}
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena"))); 
			   }
		   }else if(args[0].equalsIgnoreCase("leave")) {
			   // /paintball leave
			   Partida partida = plugin.getPartidaJugador(jugador.getName());
			   if(partida != null) {  
				   PartidaManager.jugadorSale(partida, jugador, false, plugin, false);
               }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("notInAGame"))); 
			   }
		   }else if(args[0].equalsIgnoreCase("shop")) {   
			   // /paintball shop
			   if(!Checks.checkTodo(plugin, jugador)) {
				   return false;
			   }
			   InventarioShop.crearInventarioPrincipal(jugador, plugin);
		   }else if(args[0].equalsIgnoreCase("enable")) {
			   // /paintball enable <arena>
			   //Para activar una arena todo debe estar definido
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   Partida partida = plugin.getPartida(args[1]);
					   if(partida != null) {
						   if(partida.estaActivada()) {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyEnabled"))); 
						   }else {
							   if(partida.getLobby() == null) {
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaLobbyError"))); 
								   return true;
							   }
							   if(partida.getTeam1().getSpawn() == null) {
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaSpawnError").replace("%number%", "1"))); 
								   return true;
							   }
							   if(partida.getTeam2().getSpawn() == null) {
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaSpawnError").replace("%number%", "2"))); 
								   return true;
							   }
							   
							   partida.setEstado(EstadoPartida.ESPERANDO);
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaEnabled").replace("%name%", args[1]))); 
						   }
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists"))); 
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandEnableErrorUse"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("disable")) {
			   // /paintball disable <arena>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   Partida partida = plugin.getPartida(args[1]);
					   if(partida != null) {
						   if(!partida.estaActivada()) {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyDisabled"))); 
						   }else {
							   partida.setEstado(EstadoPartida.DESACTIVADA);
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabled").replace("%name%", args[1]))); 
						   }
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists"))); 
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDisableErrorUse"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
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
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaModifyingError"))); 
							   }
						   }else {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaMustBeDisabled")));  
						   }
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists"))); 
					   }
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandAdminErrorUse"))); 
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions"))); 
			   }
		   }else if(args[0].equalsIgnoreCase("createtophologram")) {
			   // /paintball createtophologram <name> kills/wins <global/monthly/weekly>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need DecentHolograms plugin to use this feature."));
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
									   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramPeriodSQLError")));  
									   return true;
								   }
								   TopHologram hologram = new TopHologram(args[1],args[2],jugador.getLocation(),plugin,period);
								   plugin.agregarTopHolograma(hologram);
								   hologram.spawnHologram(plugin);
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramCreated")));  
							   }else {
								   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));  
							   }					    
						   }else {
							   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramAlreadyExists")));  
						   }
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));   
					   }  
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("removetophologram")) {
			   // /paintball removetophologram <name>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need DecentHolograms plugin to use this feature."));
				        return true;
				   }
				   if(args.length >= 2) {
					   TopHologram topHologram = plugin.getTopHologram(args[1]);
					   if(topHologram != null) {
						   plugin.eliminarTopHologama(args[1]);
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramRemoved")));  
					   }else {
						   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramDoesNotExists")));  
					   }  
				   }else {
					   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandRemoveHologramErrorUse")));
				   }
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("givecoins")) {
			   // /paintball givecoins <player> <amount>
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   giveCoins(sender,args,messages,prefix);
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }
		   else {
			   // /paintball help /o cualquier otro comando
			   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
				   enviarAyuda(jugador);
			   }else {
				   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
			   
		   }
	   }else {
		   if(jugador.isOp() || jugador.hasPermission("paintball.admin")) {
			   enviarAyuda(jugador);
		   }else {
			   jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
		   }
	   }
	   
	   return true;
	   
	}
	
	public boolean giveCoins(CommandSender sender, String[] args, FileConfiguration messages, String prefix) {
		if(args.length >= 3) {
			   String player = args[1];
			   try {
				   int amount = Integer.parseInt(args[2]);
				   //Si el jugador no esta en la base de datos, o en un archivo, DEBE estar conectado para darle coins.
				   if(MySQL.isEnabled(plugin.getConfig())) {
					   if(MySQL.jugadorExiste(plugin, player)) {
						   MySQL.agregarCoinsJugadorAsync(plugin, player, amount);
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
						   Player p = Bukkit.getPlayer(player);
						   if(p != null) {
							   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+""))); 
						   } 
					   }else {
						   Player p = Bukkit.getPlayer(player);
						   if(p != null) {
							   MySQL.crearJugadorPartidaAsync(plugin, p.getUniqueId().toString(), p.getName(), "", 0, 0, 0, 0, amount, 1);
							   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
							   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+"")));
						   }else {
							   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline")));
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
						   jDatos.aumentarCoins(amount);
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
						   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+""))); 
					   }else {
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline")));
					   }
				   }
				   
			   }catch(NumberFormatException e) {
				   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
			   }
			   
		   }else {
			   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandGiveCoinsErrorUse")));
		   }
		   return true;
	}
	
	public void enviarAyuda(Player jugador) {
		jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&cPaintball&7]"));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
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
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		   jugador.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[&cPaintball&7]"));
	}
}
