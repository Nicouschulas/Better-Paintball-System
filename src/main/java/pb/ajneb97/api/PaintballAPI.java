package pb.ajneb97.api;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.JugadorDatos;
import pb.ajneb97.database.MySQL;
import pb.ajneb97.juego.EstadoPartida;
import pb.ajneb97.juego.Partida;

public class PaintballAPI {

	private static PaintballBattle plugin;

	public PaintballAPI(PaintballBattle plugin) {
		PaintballAPI.plugin = plugin;
	}

	public static int getCoins(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getCoins();
			}else {
				return 0;
			}
		}else {
			return MySQL.getStatsTotales(plugin, player.getName(),"Coins");
		}
	}

	public static void addCoins(@NotNull Player player, int coins) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				j.aumentarCoins(coins);
			}
		}else {
			MySQL.agregarCoinsJugadorAsync(plugin, player.getName(), coins);
		}
	}

	public static void removeCoins(@NotNull Player player, int coins) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				j.disminuirCoins(coins);
			}
		}else {
			MySQL.removerCoinsJugadorAsync(plugin, player.getName(), coins);
		}
	}

	public static int getWins(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getWins();
			}else {
				return 0;
			}
		}else {
			return MySQL.getStatsTotales(plugin, player.getName(),"Win");
		}
	}

	public static int getLoses(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getLoses();
			}else {
				return 0;
			}
		}else {
			return MySQL.getStatsTotales(plugin, player.getName(),"Lose");
		}
	}

	public static int getTies(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getTies();
			}else {
				return 0;
			}
		}else {
			return MySQL.getStatsTotales(plugin, player.getName(),"Tie");
		}
	}

	public static int getKills(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getKills();
			}else {
				return 0;
			}
		}else {
			return MySQL.getStatsTotales(plugin, player.getName(),"Kills");
		}
	}

	public static int getPerkLevel(@NotNull Player player, @NotNull String perk) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getNivelPerk(perk);
			}else {
				return 0;
			}
		}else {
			return MySQL.getNivelPerk(plugin, player.getName(), perk);
		}
	}

	public static boolean hasHat(@NotNull Player player, @NotNull String hat) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.tieneHat(hat);
			}else {
				return false;
			}
		}else {
			return MySQL.jugadorTieneHat(plugin, player.getName(), hat);
		}
	}

	public static boolean hasHatSelected(@NotNull Player player, @NotNull String hat) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.tieneHatSeleccionado(hat);
			}else {
				return false;
			}
		}else {
			return MySQL.jugadorTieneHatSeleccionado(plugin, player.getName(), hat);
		}
	}

	public static ArrayList<Perk> getPerks(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null) {
				return j.getPerks();
			}else {
				return new ArrayList<>();
			}
		}else {
			return MySQL.getPerksJugador(plugin, player.getName());
		}
	}

	public static ArrayList<Hat> getHats(@NotNull Player player) {
		if(!MySQL.isEnabled(plugin.getConfig())) {
			JugadorDatos j = plugin.getJugador(player.getName());
			if(j != null && j.getHats() != null) {
				return j.getHats();
			}else {
				return new ArrayList<>();
			}
		}else {
			return MySQL.getHatsJugador(plugin, player.getName());
		}
	}

	public static int getPlayersArena(@NotNull String arena) {
		Partida partida = plugin.getPartida(arena);
		if(partida != null) {
			return partida.getCantidadActualJugadores();
		}else {
			return 0;
		}
	}

	public static @Nullable String getStatusArena(@NotNull String arena) {
		Partida partida = plugin.getPartida(arena);
		FileConfiguration messages = plugin.getMessages();
		if(partida != null) {
			if(partida.getEstado().equals(EstadoPartida.COMENZANDO)) {
				return messages.getString("signStatusStarting");
			}else if(partida.getEstado().equals(EstadoPartida.ESPERANDO)) {
				return messages.getString("signStatusWaiting");
			}else if(partida.getEstado().equals(EstadoPartida.JUGANDO)) {
				return messages.getString("signStatusIngame");
			}else if(partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
				return messages.getString("signStatusFinishing");
			}else {
				return messages.getString("signStatusDisabled");
			}
		}else {
			return null;
		}
	}
}