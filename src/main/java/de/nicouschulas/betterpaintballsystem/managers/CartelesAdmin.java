package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.juego.EstadoPartida;
import de.nicouschulas.betterpaintballsystem.juego.Partida;

public class CartelesAdmin {

    private int taskID;
    private final BetterPaintballSystem plugin;

    public CartelesAdmin(@NotNull BetterPaintballSystem plugin){
        this.plugin = plugin;
    }

    public int getTaskID() {
        return this.taskID;
    }

    public void actualizarCarteles() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(plugin, this::ejecutarActualizarCarteles, 0, 30L);
    }

    protected void ejecutarActualizarCarteles() {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration messages = plugin.getMessages();

        ConfigurationSection signsSection = config.getConfigurationSection("Signs");
        if (signsSection != null) {
            for (String arena : signsSection.getKeys(false)) {
                Partida partida = plugin.getPartida(arena);
                if (partida != null) {
                    List<String> listaCarteles = new ArrayList<>();
                    if (config.contains("Signs." + arena)) {
                        listaCarteles = config.getStringList("Signs." + arena);
                    }
                    for (String listaCartele : listaCarteles) {
                        String[] separados = listaCartele.split(";");
                        if (separados.length < 4) continue;

                        int x = Integer.parseInt(separados[0]);
                        int y = Integer.parseInt(separados[1]);
                        int z = Integer.parseInt(separados[2]);
                        World world = Bukkit.getWorld(separados[3]);

                        if (world != null) {
                            int chunkX = x >> 4;
                            int chunkZ = z >> 4;
                            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                                continue;
                            }
                            Block block = world.getBlockAt(x, y, z);

                            if (block.getType().name().contains("SIGN")) {
                                Sign sign = (Sign) block.getState();

                                String estado = "UNKNOWN";
                                if (partida.getEstado().equals(EstadoPartida.JUGANDO)) {
                                    estado = messages.getString("signStatusIngame", "Ingame");
                                } else if (partida.getEstado().equals(EstadoPartida.COMENZANDO)) {
                                    estado = messages.getString("signStatusStarting", "Starting");
                                } else if (partida.getEstado().equals(EstadoPartida.ESPERANDO)) {
                                    estado = messages.getString("signStatusWaiting", "Waiting");
                                } else if (partida.getEstado().equals(EstadoPartida.DESACTIVADA)) {
                                    estado = messages.getString("signStatusDisabled", "Disabled");
                                } else if (partida.getEstado().equals(EstadoPartida.TERMINANDO)) {
                                    estado = messages.getString("signStatusFinishing", "Finishing");
                                }

                                List<String> lista = messages.getStringList("signFormat");
                                for (int c = 0; c < lista.size(); c++) {
                                    String lineText = lista.get(c)
                                            .replace("%arena%", arena)
                                            .replace("%current_players%", String.valueOf(partida.getCantidadActualJugadores()))
                                            .replace("%max_players%", String.valueOf(partida.getCantidadMaximaJugadores()))
                                            .replace("%status%", estado);

                                    sign.setLine(c, ChatColor.translateAlternateColorCodes('&', lineText));
                                }

                                sign.update();
                            }
                        }
                    }
                }
            }
        }
    }
}