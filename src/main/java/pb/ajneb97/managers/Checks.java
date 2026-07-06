package pb.ajneb97.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.utils.UtilidadesOtros;

public class Checks {

	public static boolean checkTodo(@NotNull PaintballBattle plugin, @NotNull CommandSender jugador){
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		String prefix = messages.getString("prefix", "[Paintball]");
		String nombre = ChatColor.translateAlternateColorCodes('&', prefix) + " ";
		String materialError = messages.getString("materialNameError", "Invalid material name: %material%");
		String mensaje = nombre + materialError;

		//Check config.yml
		if(!comprobarMaterial(config.getString("leave_item.item"), jugador, mensaje) &&
				!comprobarMaterial(config.getString("killstreaks_item.item"), jugador, mensaje)
				&& !comprobarMaterial(config.getString("play_again_item.item"), jugador, mensaje)){
			return false;
		}

		ConfigurationSection teamsSection = config.getConfigurationSection("teams");
		if (teamsSection != null) {
			for(String key : teamsSection.getKeys(false)) {
				if(!comprobarMaterial(config.getString("teams."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}

		ConfigurationSection killstreaksSection = config.getConfigurationSection("killstreaks_items");
		if (killstreaksSection != null) {
			for(String key : killstreaksSection.getKeys(false)) {
				if(!comprobarMaterial(config.getString("killstreaks_items."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}

		ConfigurationSection hatsSection = config.getConfigurationSection("hats_items");
		if (hatsSection != null) {
			for(String key : hatsSection.getKeys(false)) {
				if(!comprobarMaterial(config.getString("hats_items."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}

		FileConfiguration shop = plugin.getShop();
		ConfigurationSection shopItemsSection = shop.getConfigurationSection("shop_items");
		if (shopItemsSection != null) {
			for(String key : shopItemsSection.getKeys(false)) {
				if(!comprobarMaterial(shop.getString("shop_items."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}

		ConfigurationSection shopPerksSection = shop.getConfigurationSection("perks_items");
		if (shopPerksSection != null) {
			for(String key : shopPerksSection.getKeys(false)) {
				if(!comprobarMaterial(shop.getString("perks_items."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}

		ConfigurationSection shopHatsSection = shop.getConfigurationSection("hats_items");
		if (shopHatsSection != null) {
			for(String key : shopHatsSection.getKeys(false)) {
				if(!comprobarMaterial(shop.getString("hats_items."+key+".item"), jugador, mensaje)){
					return false;
				}
			}
		}
		return true;
	}

	public static boolean comprobarMaterial(@Nullable String key, @NotNull CommandSender jugador, @NotNull String mensaje){
		if (key == null) return false;
		try{
			if(key.contains(":")){
				String[] idsplit = key.split(":");
				String stringDataValue = idsplit[1];
				short dataValue = Short.parseShort(stringDataValue);
				Material mat = Material.getMaterial(idsplit[0].toUpperCase());
				if (mat == null) throw new IllegalArgumentException("Material not found");

				new ItemStack(mat, 1, dataValue);
			}else {
				Material mat = Material.getMaterial(key.toUpperCase());
				if (mat == null) throw new IllegalArgumentException("Material not found");
				new ItemStack(mat, 1);
			}

			return true;
		}catch(Exception e){
			jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', mensaje.replace("%material%", key)));
			return false;
		}
	}

	public static void checkearYModificar(@NotNull PaintballBattle plugin, boolean primeraVez) {
		if(UtilidadesOtros.isLegacy()) {
			FileConfiguration config = plugin.getConfig();
			FileConfiguration shop = plugin.getShop();
			if(primeraVez) {
				modificarPath(config,"teams.blue.item","WOOL:11");
				modificarPath(config,"teams.red.item","WOOL:14");
				modificarPath(config,"teams.yellow.item","WOOL:4");
				modificarPath(config,"teams.green.item","WOOL:13");
				modificarPath(config,"teams.orange.item","WOOL:1");
				modificarPath(config,"teams.purple.item","WOOL:10");
				modificarPath(config,"teams.black.item","WOOL:15");
				modificarPath(config,"teams.white.item","WOOL");
				modificarPath(config,"play_again_item.item","INK_SACK:12");
				modificarPath(config,"killedBySound","NOTE_PLING;10;0.1");
				modificarPath(config,"killSound","FIREWORK_BLAST;10;2");
				modificarPath(config,"expireKillstreakSound","NOTE_SNARE_DRUM;10;2");
				modificarPath(config,"snowballShootSound","SHOOT_ARROW;10;0.5");
				modificarPath(config,"shopUnlockSound","FIREWORK_BLAST;10;2");
				modificarPath(config,"hatAbilityActivatedSound","CHEST_OPEN;10;1.5");
				modificarPath(config,"explosiveHatSound","EXPLODE;10;1");
				modificarPath(config,"killstreaks_items.more_snowballs.item","SNOW_BALL");
				modificarPath(config,"killstreaks_items.lightning.item","GOLD_AXE");
				modificarPath(config,"hats_items.present_hat.item","SKULL_ITEM:3");
				modificarPath(config,"hats_items.assassin_hat.item","SKULL_ITEM:3");
				modificarPath(config,"hats_items.chicken_hat.item","SKULL_ITEM:3");
				modificarPath(config,"hats_items.time_hat.item","GOLD_HELMET");
				modificarPath(config,"killstreaks_items.more_snowballs.activateSound","VILLAGER_YES;10;1");
				modificarPath(config,"killstreaks_items.strong_arm.activateSound","ANVIL_USE;10;2");
				modificarPath(config,"killstreaks_items.triple_shoot.activateSound","ANVIL_USE;10;2");
				modificarPath(config,"killstreaks_items.3_lives.activateSound","BAT_TAKEOFF;10;1.5;global");
				modificarPath(config,"killstreaks_items.teleport.activateSound","ENDERMAN_TELEPORT;10;1");
				modificarPath(config,"killstreaks_items.lightning.activateSound","AMBIENCE_THUNDER;10;1");
				modificarPath(config,"killstreaks_items.nuke.activateSound","WOLF_HOWL;10;2;global");
				modificarPath(config,"killstreaks_items.nuke.finalSound","EXPLODE;10;1;global");
				modificarPath(config,"killstreaks_items.fury.activateSound","PISTON_EXTEND;10;1.5;global");
				plugin.saveConfig();

				modificarPath(shop,"perks_items.decorative_item.item","STAINED_GLASS_PANE:10");
				modificarPath(shop,"hats_items.present_hat.item","SKULL_ITEM:3");
				modificarPath(shop,"hats_items.assassin_hat.item","SKULL_ITEM:3");
				modificarPath(shop,"hats_items.chicken_hat.item","SKULL_ITEM:3");
				modificarPath(shop,"hats_items.time_hat.item","GOLD_HELMET");
				plugin.saveShop();
			}
		}
	}

	public static void modificarPath(@NotNull FileConfiguration config, @NotNull String path, @NotNull String idNueva) {
		if(config.contains(path)) {
			config.set(path, idNueva);
		}
	}
}