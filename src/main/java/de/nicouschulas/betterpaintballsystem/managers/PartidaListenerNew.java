package de.nicouschulas.betterpaintballsystem.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.juego.Partida;

public class PartidaListenerNew implements Listener{

	BetterPaintballSystem plugin;
	public PartidaListenerNew(BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void alCambiarDeMano(PlayerSwapHandItemsEvent event) {
		Player jugador = event.getPlayer();
		Partida partida = plugin.getPartidaJugador(jugador.getName());
		if(partida != null) {
			event.setCancelled(true);
		}
	}
}
