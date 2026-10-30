package de.nicouschulas.betterpaintballsystem.utils;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import de.nicouschulas.betterpaintballsystem.juego.JugadorPaintball;

public class UtilidadesItems {

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
		if(meta != null) {
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

	public static ItemStack getCabeza(ItemStack item, String id, String textura){
		if (item == null) return null;
		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof SkullMeta skullMeta)) return item;

		UUID uuid;
		if (id != null && !id.isEmpty()) {
			try {
				uuid = UUID.fromString(id);
			} catch (IllegalArgumentException e) {
				uuid = UUID.nameUUIDFromBytes(textura.getBytes());
			}
		} else {
			uuid = UUID.nameUUIDFromBytes(textura.getBytes());
		}

		PlayerProfile profile = Bukkit.createProfile(uuid, "");
		profile.setProperty(new ProfileProperty("textures", textura));
		skullMeta.setPlayerProfile(profile);

		item.setItemMeta(skullMeta);

		return item;
	}
}