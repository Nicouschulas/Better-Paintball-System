package de.nicouschulas.betterpaintballsystem.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.database.MySQLCallback;

public class UtilidadesHologramas {

	public static int getCantidadLineasHolograma(BetterPaintballSystem plugin) {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int lineas = messages.getStringList("topHologramFormat").size();
		lineas = lineas + config.getInt("top_hologram_number_of_players", 10);
		return lineas;
	}

	// location is used in another file
	public static double determinarY(Location location, int cantidadLineasHolograma) {
		return cantidadLineasHolograma*0.15;
	}

	// Only for monthly or weekly
	public static void getTopPlayersSQL(final BetterPaintballSystem plugin, final String tipo, final String periodo, final MySQLCallback callback){
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			final ArrayList<String> playersList = new ArrayList<>();

			ArrayList<JugadorDatos> jugadores;
			if(periodo.equals("monthly")) {
				jugadores = MySQL.getPlayerDataMonthly(plugin);
			}else if(periodo.equals("weekly")) {
				jugadores = MySQL.getPlayerDataWeekly(plugin);
			}else {
				jugadores = MySQL.getPlayerData(plugin);
			}
			for(JugadorDatos j : jugadores) {
				String name = j.getName();
				int total = 0;
				if(tipo.equals("kills")) {
					total = j.getKills();
				}else if(tipo.equals("wins")) {
					total = j.getWins();
				}
				playersList.add(name+";"+total);
			}
			for(int i=0;i<playersList.size();i++) {
				for(int k=i+1;k<playersList.size();k++) {
					String[] separadosI = playersList.get(i).split(";");
					int totalI = Integer.parseInt(separadosI[1]);
					String[] separadosK = playersList.get(k).split(";");
					int totalK = Integer.parseInt(separadosK[1]);
					if(totalI < totalK) {
						String aux = playersList.get(i);
						playersList.set(i, playersList.get(k));
						playersList.set(k, aux);
					}
				}
			}
			Bukkit.getScheduler().runTask(plugin, () -> {
				callback.alTerminar(playersList);
			});
		});

	}

	public static void getTopPlayers(final BetterPaintballSystem plugin, final ArrayList<JugadorDatos> jugadores, final String tipo, final MySQLCallback callback){
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			final ArrayList<String> playersList = new ArrayList<>();
			if(!MySQL.isEnabled(plugin.getConfig())) {
				for(JugadorDatos j : jugadores) {
					String name = j.getName();
					int total = 0;
					if(tipo.equals("kills")) {
						total = j.getKills();
					}else if(tipo.equals("wins")) {
						total = j.getWins();
					}
					playersList.add(name+";"+total);
				}
			}else {
				ArrayList<JugadorDatos> jugadores1 = MySQL.getPlayerData(plugin);
				for(JugadorDatos p : jugadores1) {
					String name = p.getName();
					int total = 0;
					if(tipo.equals("kills")) {
						total = p.getKills();
					}else if(tipo.equals("wins")) {
						total = p.getWins();
					}
					playersList.add(name+";"+total);
				}
			}

			for(int i=0;i<playersList.size();i++) {
				for(int k=i+1;k<playersList.size();k++) {
					String[] separadosI = playersList.get(i).split(";");
					int totalI = Integer.parseInt(separadosI[1]);
					String[] separadosK = playersList.get(k).split(";");
					int totalK = Integer.parseInt(separadosK[1]);
					if(totalI < totalK) {
						String aux = playersList.get(i);
						playersList.set(i, playersList.get(k));
						playersList.set(k, aux);
					}
				}
			}
			Bukkit.getScheduler().runTask(plugin, () -> {
				callback.alTerminar(playersList);
			});
		});

	}
}