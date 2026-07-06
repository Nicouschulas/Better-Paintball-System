package pb.ajneb97.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.juego.JugadorPaintball;
import pb.ajneb97.juego.Killstreak;
import pb.ajneb97.juego.Partida;
import pb.ajneb97.lib.actionbarapi.ActionBarAPI;

public class CooldownKillstreaksActionbar {

	int taskID;
	private final PaintballBattle plugin;

	public CooldownKillstreaksActionbar(@NotNull PaintballBattle plugin){
		this.plugin = plugin;
	}

	public void crearActionbars() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				actualizarActionbars(player, messages, config);
			}
		}, 0L, 20L);
	}

	protected void actualizarActionbars(final Player player, final FileConfiguration messages, final FileConfiguration config) {
		Partida partida = plugin.getPartidaJugador(player.getName());
		if(partida != null) {
			JugadorPaintball jugador = partida.getJugador(player.getName());
			if(jugador != null) {
				Killstreak ultima = jugador.getUltimaKillstreak();
				if(ultima != null) {
					String configName = config.getString("killstreaks_items." + ultima.getTipo() + ".name", ultima.getTipo());
					String actionbarMsg = messages.getString("killstreakActionbar", "&e%killstreak% &7expires in &c%time%s");

					int tiempo = ultima.getTiempo();

					String finalMessage = actionbarMsg
							.replace("%killstreak%", configName)
							.replace("%time%", String.valueOf(tiempo));

					ActionBarAPI.sendActionBar(jugador.getJugador(), ChatColor.translateAlternateColorCodes('&', finalMessage));
				}
			}
		}
	}
}