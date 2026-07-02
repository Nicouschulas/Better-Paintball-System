package pb.ajneb97.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull; // JetBrains für PAPI-Kompatibilität
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import pb.ajneb97.PaintballBattle;

/**
 * This class will automatically register as a placeholder expansion
 * when a jar including this class is added to the directory
 * {@code /plugins/PlaceholderAPI/expansions} on your server.
 */
public class ExpansionPaintballBattle extends PlaceholderExpansion {

    private final PaintballBattle plugin;

    public ExpansionPaintballBattle(PaintballBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return "Ajneb97";
    }

    @Override
    public @NotNull String getIdentifier(){
        return "paintball";
    }

    @Override
    public @NotNull String getVersion(){
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier){

        if(player == null){
            return "";
        }

        switch (identifier) {
            case "wins" -> {
                return String.valueOf(PaintballAPI.getWins(player));
            }
            case "loses" -> {
                return String.valueOf(PaintballAPI.getLoses(player));
            }
            case "ties" -> {
                return String.valueOf(PaintballAPI.getTies(player));
            }
            case "coins" -> {
                return String.valueOf(PaintballAPI.getCoins(player));
            }
            case "kills" -> {
                return String.valueOf(PaintballAPI.getKills(player));
            }
        }

        if(identifier.startsWith("arenaplayers_count_")){
            String arena = identifier.replace("arenaplayers_count_", "");
            return String.valueOf(PaintballAPI.getPlayersArena(arena));
        }

        if(identifier.startsWith("arena_status_")){
            String arena = identifier.replace("arena_status_", "");
            return String.valueOf(PaintballAPI.getStatusArena(arena));
        }

        return null;
    }
}