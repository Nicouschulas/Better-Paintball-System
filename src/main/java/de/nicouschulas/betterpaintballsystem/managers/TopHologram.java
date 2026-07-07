package de.nicouschulas.betterpaintballsystem.managers;

import java.util.List;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesHologramas;

public class TopHologram {

	private final String name;
	private final String type;
	private Hologram hologram;
	private final double yOriginal;
	private final String period;

	public TopHologram(String name, String type, Location location, BetterPaintballSystem plugin, String period) {
		this.type = type;
		this.name = name;
		this.period = period;
		this.yOriginal = location.getY();
		Location nuevaLoc = location.clone();
		nuevaLoc.setY(nuevaLoc.getY() + UtilidadesHologramas.determinarY(nuevaLoc, UtilidadesHologramas.getCantidadLineasHolograma(plugin)) + 1.4);
		this.hologram = DHAPI.createHologram(name, nuevaLoc, true);
	}

	public String getPeriod() {
		return this.period;
	}

	public double getyOriginal() {
		return yOriginal;
	}

	public void removeHologram() {
		this.hologram.delete();
	}

	public void spawnHologram(BetterPaintballSystem plugin) {
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		final int topPlayersMax = config.getInt("top_hologram_number_of_players", 10);
		List<String> lineas = messages.getStringList("topHologramFormat");

		hologram.setDefaultVisibleState(true);

		String typeName = type.equals("kills") ? messages.getString("topHologramTypeKills", "Kills") : messages.getString("topHologramTypeWins", "Wins");

		String periodName;
		if (period.equals("monthly")) {
			periodName = messages.getString("topHologramPeriodMonthly", "Monthly");
		} else if (period.equals("weekly")) {
			periodName = messages.getString("topHologramPeriodWeekly", "Weekly");
		} else {
			periodName = messages.getString("topHologramPeriodGlobal", "Global");
		}

		final String lineaMessage = messages.getString("topHologramScoreboardLine", "&7%position%. &a%name% &7- &e%points%");
		for (String s : lineas) {
			if (s == null) continue;
			String linea = s.replace("%type%", typeName).replace("%period%", periodName);
			if (linea.contains("%scoreboard_lines%")) {
				if (MySQL.isEnabled(config) && !period.equals("global")) {
					UtilidadesHologramas.getTopPlayersSQL(plugin, type, period, playersList -> {
						for (int c = 0; c < topPlayersMax; c++) {
							int num = c + 1;
							try {
								String[] separados = playersList.get(c).split(";");
								DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', lineaMessage
										.replace("%position%", String.valueOf(num))
										.replace("%name%", separados[0])
										.replace("%points%", separados[1])));
							} catch (Exception e) {
								break;
							}
						}
					});
				} else {
					UtilidadesHologramas.getTopPlayers(plugin, plugin.getJugadores(), type, playersList -> {
						for (int c = 0; c < topPlayersMax; c++) {
							int num = c + 1;
							try {
								String[] separados = playersList.get(c).split(";");
								DHAPI.addHologramLine(hologram, (ChatColor.translateAlternateColorCodes('&', lineaMessage
										.replace("%position%", String.valueOf(num))
										.replace("%name%", separados[0])
										.replace("%points%", separados[1]))));
							} catch (Exception e) {
								break;
							}
						}
					});
				}
			} else {
				DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', linea));
			}
		}
	}

	public void actualizar(BetterPaintballSystem plugin) {
		Location loc = this.hologram.getLocation();
		removeHologram();
		loc.setY(yOriginal);
		Location nuevaLoc = loc.clone();
		nuevaLoc.setY(nuevaLoc.getY() + UtilidadesHologramas.determinarY(nuevaLoc, UtilidadesHologramas.getCantidadLineasHolograma(plugin)) + 1.4);
		this.hologram = DHAPI.createHologram(name,nuevaLoc);
		spawnHologram(plugin);
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Hologram getHologram() {
		return hologram;
	}
}