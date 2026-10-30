package de.nicouschulas.betterpaintballsystem.managers;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Checks {

	public static boolean checkTodo(@NotNull BetterPaintballSystem plugin, @NotNull CommandSender jugador){
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

	public static void checkearYModificar(@NotNull BetterPaintballSystem plugin, boolean primeraVez) {
	}

	public static void modificarPath(@NotNull FileConfiguration config, @NotNull String path, @NotNull String idNueva) {
		if(config.contains(path)) {
			config.set(path, idNueva);
		}
	}
}