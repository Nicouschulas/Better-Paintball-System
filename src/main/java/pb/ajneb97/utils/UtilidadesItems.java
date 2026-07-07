package pb.ajneb97.utils;

import pb.ajneb97.PaintballBattle;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import pb.ajneb97.juego.JugadorPaintball;

public class UtilidadesItems {

	@SuppressWarnings("deprecation")
	public static ItemStack crearItem(FileConfiguration config,String path){
		String id = config.getString(path+".item");
		if (id == null) {
			return new ItemStack(Material.STONE);
		}
		String[] idsplit;
		int DataValue;
		ItemStack stack;
		if(id.contains(":")){
			idsplit = id.split(":");
			String stringDataValue = idsplit[1];
			DataValue = Integer.parseInt(stringDataValue);
			Material mat = Material.getMaterial(idsplit[0].toUpperCase());
			stack = new ItemStack(mat != null ? mat : Material.STONE,1,(short)DataValue);
		}else{
			Material mat = Material.getMaterial(id.toUpperCase());
			stack = new ItemStack(mat != null ? mat : Material.STONE,1);
		}
		ItemMeta meta = stack.getItemMeta();
		if(meta != null && config.contains(path+".name")) {
			String name = config.getString(path+".name");
			if (name != null) {
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
			}
		}
		if(meta != null && config.contains(path+".lore")) {
			List<String> lore = config.getStringList(path+".lore");
			lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
			meta.setLore(lore);

		}
		if (meta != null && Arrays.stream(ItemFlag.values()).anyMatch(f -> f.name().equals("HIDE_POTION_EFFECTS"))) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
		} else if (meta != null) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
		}
		if(meta != null && (Bukkit.getVersion().contains("1.15") || UtilidadesOtros.isNew())) {
			meta.setUnbreakable(true);
		}
		stack.setItemMeta(meta);

		return stack;
	}

	public static void crearItemKillstreaks(JugadorPaintball jugador,FileConfiguration config) {
		if("true".equals(config.getString("killstreaks_item_enabled"))) {
			int coins = jugador.getCoins();
			ItemStack item = UtilidadesItems.crearItem(config, "killstreaks_item");
			ItemMeta meta = item.getItemMeta();

			if (meta != null) {
				String itemName = config.getString("killstreaks_item.name");
				if (itemName != null) {
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName.replace("%amount%", coins+"")));
				}
				item.setItemMeta(meta);
			}
			if(coins <= 1) {
				item.setAmount(1);
			}else item.setAmount(Math.min(coins, 64));
			jugador.getJugador().getInventory().setItem(8, item);
		}
	}

	public static ItemStack getCabeza(ItemStack item, String id,String textura){
		SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
		if (skullMeta == null) return item;

		ServerVersion serverVersion = PaintballBattle.serverVersion;
		if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R2)){
			UUID uuid = id != null ? UUID.fromString(id) : UUID.randomUUID();
			PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
			PlayerTextures textures = profile.getTextures();
			URL url;
			try {
				String decoded = new String(Base64.getDecoder().decode(textura));
				String decodedFormatted = decoded.replaceAll("\\s", "");
				JsonObject jsonObject = new Gson().fromJson(decodedFormatted, JsonObject.class);
				String urlText = jsonObject.get("textures").getAsJsonObject().get("SKIN")
						.getAsJsonObject().get("url").getAsString();

				url = new URL(urlText);
			} catch (Exception error) {
				Plugin plugin = Bukkit.getPluginManager().getPlugin("Paintball");
				Logger logger = plugin != null ? plugin.getLogger() : Bukkit.getLogger();
				logger.log(Level.SEVERE, "Could not load skull texture profile", error);
				return null;
			}
			textures.setSkin(url);
			profile.setTextures(textures);
			skullMeta.setOwnerProfile(profile);
		}else{
			GameProfile profile;
			if(id == null) {
				profile = new GameProfile(UUID.randomUUID(), "");
			}else {
				profile = new GameProfile(UUID.fromString(id), "");
			}
			profile.getProperties().put("textures", new Property("textures", textura));

			try {
				Field profileField = skullMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				profileField.set(skullMeta, profile);
			} catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
				Plugin plugin = Bukkit.getPluginManager().getPlugin("Paintball");
				Logger logger = plugin != null ? plugin.getLogger() : Bukkit.getLogger();
				logger.log(Level.SEVERE, "Could not set skull profile field via reflection", error);
			}
		}

		item.setItemMeta(skullMeta);

		return item;
	}
}