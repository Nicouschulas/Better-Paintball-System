package de.nicouschulas.betterpaintballsystem.managers;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitScheduler;

import de.nicouschulas.betterpaintballsystem.utils.UtilidadesOtros;

public class CooldownSnowballParticle {

	int taskID;
	private final BetterPaintballSystem plugin;
	private final Projectile snowball;
	private final String particula;

	public CooldownSnowballParticle(BetterPaintballSystem plugin, Projectile snowball, String particula){
		this.plugin = plugin;
		this.snowball = snowball;
		this.particula = particula;
	}

	public void cooldown(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			if(!ejecutar()){
				Bukkit.getScheduler().cancelTask(taskID);
			}
		}, 0L, 3L);
	}

	protected boolean ejecutar() {
		if(snowball != null && !snowball.isDead()) {
			Location l = snowball.getLocation();
			UtilidadesOtros.generarParticula(particula, l, 0.01F, 0.01F, 0.01F, 0.01F, 1);
			return true;
		}else {
			return false;
		}
	}
}