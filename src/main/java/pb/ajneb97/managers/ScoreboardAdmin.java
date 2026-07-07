package pb.ajneb97.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import me.clip.placeholderapi.PlaceholderAPI;
import pb.ajneb97.PaintballBattle;
import pb.ajneb97.juego.Equipo;
import pb.ajneb97.juego.EstadoPartida;
import pb.ajneb97.juego.JugadorPaintball;
import pb.ajneb97.juego.Partida;
import pb.ajneb97.lib.fastboard.FastBoard;
import pb.ajneb97.utils.UtilidadesOtros;

public class ScoreboardAdmin {

	private int taskID;
	private final PaintballBattle plugin;
	private final Map<UUID, FastBoard> boards = new HashMap<>();

	public ScoreboardAdmin(PaintballBattle plugin){
		this.plugin = plugin;
	}

	public int getTaskID() {
		return this.taskID;
	}

	public void crearScoreboards() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();

		taskID = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				actualizarScoreboard(player, messages, config);
			}
		}, 0, 20L);
	}

	protected void actualizarScoreboard(final Player player, final FileConfiguration messages, final FileConfiguration config) {
		Partida partida = plugin.getPartidaJugador(player.getName());
		FastBoard board = boards.get(player.getUniqueId());

		if(partida != null) {
			JugadorPaintball jugador = partida.getJugador(player.getName());

			if(board == null) {
				board = new FastBoard(player);
				String title = messages.getString("gameScoreboardTitle", "&c&lPaintball");
				board.updateTitle(org.bukkit.ChatColor.translateAlternateColorCodes('&', title));
				boards.put(player.getUniqueId(), board);
			}

			List<String> lista = messages.getStringList("gameScoreboardBody");
			Equipo equipo1 = partida.getTeam1();
			Equipo equipo2 = partida.getTeam2();

			String equipo1Nombre;
			String equipo2Nombre;

            equipo1Nombre = config.getString("teams." + equipo1.getTipo() + ".name", equipo1.getTipo());
            equipo2Nombre = config.getString("teams." + equipo2.getTipo() + ".name", equipo2.getTipo());

            for(int i = 0; i < lista.size(); i++) {
				String originalLine = lista.get(i);
				if (originalLine == null) continue;

				int vidas1 = equipo1.getVidas();
				int vidas2 = equipo2.getVidas();
				int kills = (jugador != null) ? jugador.getAsesinatos() : 0;

				String message = org.bukkit.ChatColor.translateAlternateColorCodes('&', originalLine
						.replace("%status%", getEstado(partida, messages))
						.replace("%team_1%", equipo1Nombre)
						.replace("%team_2%", equipo2Nombre)
						.replace("%team_1_lives%", String.valueOf(vidas1))
						.replace("%team_2_lives%", String.valueOf(vidas2))
						.replace("%kills%", String.valueOf(kills))
						.replace("%arena%", partida.getNombre())
						.replace("%current_players%", String.valueOf(partida.getCantidadActualJugadores()))
						.replace("%max_players%", String.valueOf(partida.getCantidadMaximaJugadores())));

				if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
					message = PlaceholderAPI.setPlaceholders(player, message);
				}
				board.updateLine(i, message);
			}
		} else {
			if(board != null) {
				boards.remove(player.getUniqueId());
				board.delete();
			}
		}
	}

	private String getEstado(Partida partida, FileConfiguration messages) {
		if (partida == null || messages == null) return "";

		EstadoPartida estado = partida.getEstado();

        if(estado == EstadoPartida.ESPERANDO) {
			return messages.getString("statusWaiting", "Waiting...");
		} else if(estado == EstadoPartida.COMENZANDO) {
			int tiempo = partida.getTiempo();
			String msg = messages.getString("statusStarting", "Starting in %time%");
			return msg.replace("%time%", UtilidadesOtros.getTiempo(tiempo));
		} else if(estado == EstadoPartida.TERMINANDO) {
			int tiempo = partida.getTiempo();
			String msg = messages.getString("statusFinishing", "Ending in %time%");
			return msg.replace("%time%", UtilidadesOtros.getTiempo(tiempo));
		} else {
			int tiempo = partida.getTiempo();
			String msg = messages.getString("statusIngame", "Playing: %time%");
			return msg.replace("%time%", UtilidadesOtros.getTiempo(tiempo));
		}
	}
}