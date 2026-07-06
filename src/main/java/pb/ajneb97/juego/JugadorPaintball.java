package pb.ajneb97.juego;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JugadorPaintball {

	private final Player jugador;
	private int asesinatos;
	private int muertes;
	private final ElementosGuardados guardados;
	private boolean asesinadoRecientemente;
	private String preferenciaTeam;
	private int coins;
	private final ArrayList<Killstreak> killstreaks;
	private Location deathLocation;

	private String selectedHat;
	private boolean efectoHatActivado;
	private boolean efectoHatEnCooldown;
	private int tiempoEfectoHat;
	private String lastKilledBy;

	public JugadorPaintball(@NotNull Player jugador) {
		this.jugador = jugador;
		this.guardados = new ElementosGuardados(jugador.getInventory().getContents().clone(), jugador.getEquipment().getArmorContents().clone(), jugador.getGameMode()
				, jugador.getExp(), jugador.getLevel(), jugador.getFoodLevel(), jugador.getHealth(), jugador.getMaxHealth(), jugador.getAllowFlight(), jugador.isFlying());
		this.asesinadoRecientemente = false;
		this.muertes = 0;
		this.asesinatos = 0;
		this.coins = 0;
		this.killstreaks = new ArrayList<>();
		this.efectoHatActivado = false;
		this.efectoHatEnCooldown = false;
		this.tiempoEfectoHat = 0;
		this.selectedHat = "";
	}

	public @Nullable String getLastKilledBy() {
		return lastKilledBy;
	}

	public void setLastKilledBy(@Nullable String lastKilledBy) {
		this.lastKilledBy = lastKilledBy;
	}

	public boolean isEfectoHatActivado() {
		return efectoHatActivado;
	}

	public void setEfectoHatActivado(boolean efectoHatActivado) {
		this.efectoHatActivado = efectoHatActivado;
	}

	public boolean isEfectoHatEnCooldown() {
		return efectoHatEnCooldown;
	}

	public void setEfectoHatEnCooldown(boolean efectoHatEnCooldown) {
		this.efectoHatEnCooldown = efectoHatEnCooldown;
	}

	public int getTiempoEfectoHat() {
		return tiempoEfectoHat;
	}

	public void setTiempoEfectoHat(int tiempoEfectoHat) {
		this.tiempoEfectoHat = tiempoEfectoHat;
	}

	public void setSelectedHat(@NotNull String hat) {
		this.selectedHat = hat;
	}

	public @NotNull String getSelectedHat() {
		return this.selectedHat;
	}

	public void setDeathLocation(@Nullable Location l) {
		this.deathLocation = l;
	}

	public @Nullable Location getDeathLocation() {
		return this.deathLocation;
	}

	public void agregarKillstreak(@NotNull Killstreak k) {
		this.killstreaks.add(k);
	}

	public @Nullable Killstreak getKillstreak(@NotNull String tipo) {
		for(Killstreak k : this.killstreaks) {
			if(k.getTipo().equals(tipo)) {
				return k;
			}
		}
		return null;
	}

	public void removerKillstreak(@NotNull String tipo) {
		this.killstreaks.removeIf(k -> k.getTipo().equals(tipo));
	}

	public @Nullable Killstreak getUltimaKillstreak() {
		if(killstreaks.isEmpty()) {
			return null;
		}else {
			return killstreaks.getLast();
		}
	}

	public int getCoins() {
		return this.coins;
	}

	public void agregarCoins(int cantidad) {
		this.coins = this.coins + cantidad;
	}

	public void disminuirCoins(int cantidad) {
		this.coins = this.coins - cantidad;
	}

	public void setPreferenciaTeam(@Nullable String team) {
		this.preferenciaTeam = team;
	}

	public @Nullable String getPreferenciaTeam() {
		return this.preferenciaTeam;
	}

	public @NotNull ElementosGuardados getGuardados() {
		return this.guardados;
	}

	public void aumentarAsesinatos() {
		this.asesinatos++;
	}

	public void aumentarMuertes() {
		this.muertes++;
	}

	public int getAsesinatos() {
		return this.asesinatos;
	}

	public int getMuertes() {
		return this.muertes;
	}

	public @NotNull Player getJugador() {
		return this.jugador;
	}

	public void setAsesinadoRecientemente(boolean asesinadoRecientemente) {
		this.asesinadoRecientemente = asesinadoRecientemente;
	}

	public boolean haSidoAsesinadoRecientemente() {
		return this.asesinadoRecientemente;
	}
}