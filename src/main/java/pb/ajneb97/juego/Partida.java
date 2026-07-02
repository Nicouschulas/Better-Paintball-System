package pb.ajneb97.juego;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Partida {

	private final Equipo team1;
	private final Equipo team2;
	private final String nombre;
	private int cantidadMaximaJugadores;
	private int cantidadMinimaJugadores;
	private int cantidadActualJugadores;
	private EstadoPartida estado;
	private Location lobby;
	private int tiempo;
	private int tiempoMaximo;
	private int vidasIniciales;
	private boolean enNuke;

	public Partida(@NotNull String nombre, int tiempoMaximo, @NotNull String equipo1, @NotNull String equipo2, int vidasIniciales) {
		this.team1 = new Equipo(equipo1);
		this.team2 = new Equipo(equipo2);
		this.nombre = nombre;
		this.cantidadMaximaJugadores = 16;
		this.cantidadMinimaJugadores = 4;
		this.cantidadActualJugadores = 0;
		this.estado = EstadoPartida.DESACTIVADA;
		this.tiempo = 0;
		this.tiempoMaximo = tiempoMaximo;
		this.vidasIniciales = vidasIniciales;
		this.enNuke = false;
	}

	public boolean isEnNuke() {
		return enNuke;
	}

	public void setEnNuke(boolean enNuke) {
		this.enNuke = enNuke;
	}

	public void setVidasIniciales(int cantidad) {
		this.vidasIniciales = cantidad;
	}

	public int getVidasIniciales() {
		return this.vidasIniciales;
	}

	public void setTiempoMaximo(int tiempo) {
		this.tiempoMaximo = tiempo;
	}

	public int getTiempoMaximo() {
		return this.tiempoMaximo;
	}

	public void disminuirTiempo() {
		this.tiempo--;
	}

	public void aumentarTiempo() {
		this.tiempo++;
	}

	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}

	public int getTiempo() {
		return this.tiempo;
	}

	public @NotNull String getNombre() {
		return this.nombre;
	}

	public void agregarJugador(@NotNull JugadorPaintball player) {
		if(team1.agregarJugador(player)) {
			this.cantidadActualJugadores++;
		}
	}

	public void repartirJugadorTeam2(@NotNull JugadorPaintball player) {
		this.team1.removerJugador(player.getJugador().getName());
		this.team2.agregarJugador(player);
	}

	public void removerJugador(@NotNull String player) {
		if(team1.removerJugador(player) || team2.removerJugador(player)) {
			this.cantidadActualJugadores--;
		}
	}

	public @NotNull ArrayList<JugadorPaintball> getJugadores(){
		ArrayList<JugadorPaintball> jugadores = new ArrayList<>();

		jugadores.addAll(team1.getJugadores());
		jugadores.addAll(team2.getJugadores());

		return jugadores;
	}

	public @Nullable JugadorPaintball getJugador(@NotNull String jugador) {
		for (JugadorPaintball jp : getJugadores()) {
			if (jp.getJugador().getName().equals(jugador)) {
				return jp;
			}
		}
		return null;
	}

	public @Nullable Equipo getEquipoJugador(@NotNull String jugador) {
		for (JugadorPaintball jugadorPaintball : team1.getJugadores()) {
			if (jugadorPaintball.getJugador().getName().equals(jugador)) {
				return this.team1;
			}
		}
		for (JugadorPaintball jugadorPaintball : team2.getJugadores()) {
			if (jugadorPaintball.getJugador().getName().equals(jugador)) {
				return this.team2;
			}
		}

		return null;
	}

	public @NotNull Equipo getTeam1() {
		return this.team1;
	}

	public @NotNull Equipo getTeam2() {
		return this.team2;
	}

	public int getCantidadMaximaJugadores() {
		return this.cantidadMaximaJugadores;
	}

	public void setCantidadMaximaJugadores(int max) {
		this.cantidadMaximaJugadores = max;
	}

	public int getCantidadMinimaJugadores() {
		return this.cantidadMinimaJugadores;
	}

	public void setCantidadMinimaJugadores(int min) {
		this.cantidadMinimaJugadores = min;
	}

	public int getCantidadActualJugadores() {
		return this.cantidadActualJugadores;
	}

	public @NotNull EstadoPartida getEstado() {
		return this.estado;
	}

	public void setEstado(@NotNull EstadoPartida estado) {
		this.estado = estado;
	}

	public boolean estaIniciada() {
		return !this.estado.equals(EstadoPartida.ESPERANDO) && !this.estado.equals(EstadoPartida.COMENZANDO);
	}

	public boolean estaLlena() {
		return this.cantidadActualJugadores == this.cantidadMaximaJugadores;
	}

	public boolean estaActivada() {
		return !this.estado.equals(EstadoPartida.DESACTIVADA);
	}

	public void setLobby(@Nullable Location l) {
		this.lobby = l;
	}

	public @Nullable Location getLobby() {
		return this.lobby;
	}

	public @Nullable Equipo getGanador() {
		if(team1.getJugadores().isEmpty()) {
			return team2;
		}
		if(team2.getJugadores().isEmpty()) {
			return team1;
		}

		int vidasTeam1 = team1.getVidas();
		int vidasTeam2 = team2.getVidas();
		if(vidasTeam1 > vidasTeam2) {
			return team1;
		}else if(vidasTeam2 > vidasTeam1) {
			return team2;
		}else {
			return null; //tie
		}
	}

	public @NotNull ArrayList<JugadorPaintball> getJugadoresKills() {
		ArrayList<JugadorPaintball> nuevo = new ArrayList<>(getJugadores());

		for(int i=0;i<nuevo.size();i++) {
			for(int c=i+1;c<nuevo.size();c++) {
				if(nuevo.get(i).getAsesinatos() < nuevo.get(c).getAsesinatos()) {
					JugadorPaintball j = nuevo.get(i);
					nuevo.set(i, nuevo.get(c));
					nuevo.set(c, j);
				}
			}
		}

		return nuevo;
	}

	public boolean puedeSeleccionarEquipo(@NotNull String equipo) {
		int mitad;
		if(this.cantidadActualJugadores % 2 != 0) {
			mitad = (this.cantidadActualJugadores / 2) + 1;
		}else {
			mitad = this.cantidadActualJugadores / 2;
		}
		if(equipo.equals(this.team1.getTipo())) {
			int cantidadPreferenciaTeam1 = 0;
			for(JugadorPaintball j : this.getJugadores()) {
				if(j.getPreferenciaTeam() != null && j.getPreferenciaTeam().equals(this.team1.getTipo())) {
					cantidadPreferenciaTeam1++;
				}
			}

			if(this.cantidadActualJugadores == 1) {
				return true;
			}

			return cantidadPreferenciaTeam1 < mitad;
		}else {
			int cantidadPreferenciaTeam2 = 0;
			for(JugadorPaintball j : this.getJugadores()) {
				if(j.getPreferenciaTeam() != null &&  j.getPreferenciaTeam().equals(this.team2.getTipo())) {
					cantidadPreferenciaTeam2++;
				}
			}

			if(this.cantidadActualJugadores == 1) {
				return true;
			}
			return cantidadPreferenciaTeam2 < mitad;
		}
	}

	public void modificarTeams(@NotNull FileConfiguration config) {
		Equipo team1 = this.team1;
		Equipo team2 = this.team2;
		String nTeam1 = team1.getTipo();
		String nTeam2 = team2.getTipo();
		Random r = new Random();
		ArrayList<String> nombres = new ArrayList<>();

		ConfigurationSection section = config.getConfigurationSection("teams");
		if (section != null) {
			nombres.addAll(section.getKeys(false));
		}

		int max = nombres.size();
		if (max == 0) return;

		if(team1.esRandom() && !team2.esRandom()) {
			do {
				nTeam1 = nombres.get(r.nextInt(max));
			} while (nTeam1.equals(nTeam2));
			team1.setTipo(nTeam1);
		}else if(!team1.esRandom() && team2.esRandom()) {
			do {
				nTeam2 = nombres.get(r.nextInt(max));
			} while (nTeam2.equals(nTeam1));
			team2.setTipo(nTeam2);
		}else if(team1.esRandom() && team2.esRandom()) {
			nTeam1 = nombres.get(r.nextInt(max));
			do {
				nTeam2 = nombres.get(r.nextInt(max));
			} while (nTeam2.equals(nTeam1));
			team1.setTipo(nTeam1);
			team2.setTipo(nTeam2);
		}
	}
}