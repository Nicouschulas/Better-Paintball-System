package pb.ajneb97.juego;

import org.jetbrains.annotations.NotNull;

public class Killstreak {

	private String tipo;
	private int tiempo;

	public Killstreak(@NotNull String tipo, int tiempo) {
		this.tipo = tipo;
		this.tiempo = tiempo;
	}

	public @NotNull String getTipo() {
		return tipo;
	}

	@SuppressWarnings("unused")
	public void setTipo(@NotNull String tipo) {
		this.tipo = tipo;
	}

	public int getTiempo() {
		return tiempo;
	}

	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}
}