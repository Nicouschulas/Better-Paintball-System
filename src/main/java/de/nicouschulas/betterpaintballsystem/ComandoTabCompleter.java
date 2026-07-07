package de.nicouschulas.betterpaintballsystem;

import de.nicouschulas.betterpaintballsystem.juego.Partida;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComandoTabCompleter implements TabCompleter {

    private final BetterPaintballSystem plugin;

    public ComandoTabCompleter(BetterPaintballSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NonNull List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String @NonNull [] args) {
        if (!cmd.getName().equalsIgnoreCase("paintball")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> sub = new ArrayList<>();
            sub.add("join");
            sub.add("joinrandom");
            sub.add("leave");
            sub.add("shop");
            sub.add("create");
            sub.add("delete");
            sub.add("enable");
            sub.add("disable");
            sub.add("edit");
            sub.add("setmainlobby");
            sub.add("createtophologram");
            sub.add("removetophologram");
            sub.add("givecoins");
            sub.add("reload");
            return sub.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2) {
            // Bei join, delete, enable, disable, edit → Arenanamen
            if (args[0].equalsIgnoreCase("join") ||
                    args[0].equalsIgnoreCase("delete") ||
                    args[0].equalsIgnoreCase("enable") ||
                    args[0].equalsIgnoreCase("disable") ||
                    args[0].equalsIgnoreCase("edit")) {
                List<String> arenaNames = plugin.getPartidas().stream()
                        .map(Partida::getNombre)
                        .toList();
                return arenaNames.stream().filter(a -> a.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }
        }

        // Bei createtophologram: Vorschläge für kills/wins, global/monthly/weekly
        if (args.length == 3 && args[0].equalsIgnoreCase("createtophologram")) {
            List<String> stats = new ArrayList<>();
            stats.add("kills"); stats.add("wins");
            return stats.stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("createtophologram")) {
            List<String> periods = new ArrayList<>();
            periods.add("global"); periods.add("monthly"); periods.add("weekly");
            return periods.stream().filter(s -> s.startsWith(args[3].toLowerCase())).toList();
        }

        return Collections.emptyList();
    }
}