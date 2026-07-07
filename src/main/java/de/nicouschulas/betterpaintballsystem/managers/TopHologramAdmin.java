package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

public class TopHologramAdmin {

	int taskID;
	private final BetterPaintballSystem plugin;
	public TopHologramAdmin(BetterPaintballSystem plugin){
		this.plugin = plugin;
	}

	public int getTaskID() {
		return this.taskID;
	}

	public void actualizarHologramas() {
		FileConfiguration config = plugin.getConfig();
		long ticks = config.getLong("top_hologram_update_time", 300L) * 20;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskID = scheduler.scheduleSyncRepeatingTask(plugin, this::ejecutarActualizarHologramas, ticks, ticks);
	}

	protected void ejecutarActualizarHologramas() {
		ArrayList<TopHologram> hologramas = plugin.getTopHologramas();
		for (TopHologram holograma : hologramas) {
			holograma.actualizar(plugin);

		}
	}
}