package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatColor;
import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.api.Hat;
import de.nicouschulas.betterpaintballsystem.api.PaintballAPI;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesItems;

public class InventarioHats implements Listener {

	private final BetterPaintballSystem plugin;

	public InventarioHats(@NotNull BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	public static void crearInventario(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration config = plugin.getConfig();
		String invTitle = config.getString("hats_inventory_title", "&8Hats");
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', invTitle));
		ArrayList<Hat> hats = PaintballAPI.getHats(jugador);

		if(hats.isEmpty()) {
			ItemStack item = UtilidadesItems.crearItem(config, "hats_items.no_hats");
			inv.setItem(13, item);
		} else {
			FileConfiguration messages = plugin.getMessages();
			int slot = 0;
			for(Hat h : hats) {
				String name = h.getName();
				ItemStack item = UtilidadesItems.crearItem(config, "hats_items." + name);
				ItemMeta meta = item.getItemMeta();

				if(meta != null) {
					List<String> lore = meta.getLore();
					if(lore != null) {
						String status;
						if(h.isSelected()) {
							status = messages.getString("hatStatusSelected", "&aSelected");
						} else {
							status = messages.getString("hatStatusNotSelected", "&cNot Selected");
						}
						lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s.replace("%status%", status)));
						meta.setLore(lore);
						item.setItemMeta(meta);
					}
				}

				if(config.contains("hats_items." + name + ".skull_id")) {
					String id = config.getString("hats_items." + name + ".skull_id");
					String textura = config.getString("hats_items." + name + ".skull_texture");
					item = UtilidadesItems.getCabeza(item, id, textura);
				}

				inv.setItem(slot, item);
				slot++;
			}

			ItemStack item = UtilidadesItems.crearItem(config, "hats_items.remove_hat");
			inv.setItem(26, item);
		}

		jugador.openInventory(inv);
	}

	@EventHandler
	public void clickInventario(InventoryClickEvent event) {
		FileConfiguration config = plugin.getConfig();
		String pathInventory = ChatColor.translateAlternateColorCodes('&', config.getString("hats_inventory_title", "&8Hats"));
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();

		String prefixStr = messages.getString("prefix", "&7[&cPaintball&7]&r");
		String prefix = ChatColor.translateAlternateColorCodes('&', prefixStr) + " ";

		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)) {
			if(event.getCurrentItem() == null) {
				event.setCancelled(true);
				return;
			}

			final Player jugador = (Player) event.getWhoClicked();
			event.setCancelled(true);

			if(event.getClickedInventory() != null && event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
				ArrayList<Hat> hats = PaintballAPI.getHats(jugador);
				ItemStack item = event.getCurrentItem();

				if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					if(event.getSlot() == 26) {
						if(MySQL.isEnabled(config)) {
							MySQL.deseleccionarHats(plugin, jugador.getName());
						} else {
							JugadorDatos jDatos = plugin.getJugador(jugador.getName());
							jDatos.deseleccionarHats();
						}

						String hatRemovedMsg = messages.getString("hatRemoved", "&aHat removed successfully.");
						jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', hatRemovedMsg));

						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> InventarioHats.crearInventario(jugador, plugin), 5L);
						return;
					}

					for(Hat h : hats) {
						ItemStack itemConfig = UtilidadesItems.crearItem(config, "hats_items." + h.getName());
						ItemMeta meta = item.getItemMeta();
						ItemMeta metaConfig = itemConfig.getItemMeta();

						if(metaConfig != null && item.getType().equals(itemConfig.getType()) && meta.getDisplayName().equals(metaConfig.getDisplayName())) {
							if(PaintballAPI.hasHatSelected(jugador, h.getName())) {
								String alreadySelectedMsg = messages.getString("hatAlreadySelected", "&cYou already have this hat selected.");
								jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', alreadySelectedMsg));
								return;
							}

							if(MySQL.isEnabled(config)) {
								MySQL.seleccionarHatAsync(plugin, jugador.getName(), h.getName());
							} else {
								JugadorDatos jDatos = plugin.getJugador(jugador.getName());
								jDatos.seleccionarHat(h.getName());
							}

							String hatNameFallback = config.getString("hats_items." + h.getName() + ".name", h.getName());
							String hatSelectedMsg = messages.getString("hatSelected", "&aYou selected the %name% hat.");
							jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', hatSelectedMsg.replace("%name%", hatNameFallback)));

							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> InventarioHats.crearInventario(jugador, plugin), 5L);
							return;
						}
					}
				}
			}
		}
	}
}