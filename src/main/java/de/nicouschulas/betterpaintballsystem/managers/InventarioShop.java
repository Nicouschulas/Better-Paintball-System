package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.List;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.realized.tokenmanager.api.TokenManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import de.nicouschulas.betterpaintballsystem.api.PaintballAPI;
import de.nicouschulas.betterpaintballsystem.database.JugadorDatos;
import de.nicouschulas.betterpaintballsystem.database.MySQL;
import de.nicouschulas.betterpaintballsystem.utils.UtilidadesItems;
import de.nicouschulas.betterpaintballsystem.utils.ValueOfPatch;


public class InventarioShop implements Listener{

	private final BetterPaintballSystem plugin;
	public InventarioShop(BetterPaintballSystem plugin) {
		this.plugin = plugin;
	}

	public static void crearInventarioPrincipal(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration shop = plugin.getShop();
		String title = shop.getString("shopInventoryTitle", "");
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
		ConfigurationSection shopItems = shop.getConfigurationSection("shop_items");
		if (shopItems != null) {
			for(String key : shopItems.getKeys(false)) {
				ItemStack item = UtilidadesItems.crearItem(shop, "shop_items."+key);
				int slot = Integer.parseInt(shop.getString("shop_items."+key+".slot", "-1"));
				if(slot != - 1) {
					inv.setItem(slot, item);
				}
			}
		}

		jugador.openInventory(inv);
	}

	@EventHandler
	public void clickInventarioPrincipal(InventoryClickEvent event){
		FileConfiguration shop = plugin.getShop();
		String title = shop.getString("shopInventoryTitle", "");
		String pathInventory = ChatColor.translateAlternateColorCodes('&', title);
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			Player jugador = (Player) event.getWhoClicked();
			event.setCancelled(true);
			if(event.getClickedInventory() != null && event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
				int slot = event.getSlot();
				ConfigurationSection shopItems = shop.getConfigurationSection("shop_items");
				if (shopItems != null) {
					for(String key : shopItems.getKeys(false)) {
						if(slot == Integer.parseInt(shop.getString("shop_items."+key+".slot", "-1"))) {
							if(key.equals("perks_items")) {
								crearInventarioPerks(jugador,plugin);
							}else if(key.equals("hats_items")) {
								crearInventarioHats(jugador,plugin);
							}
							return;
						}
					}
				}
			}
		}
	}

	public static void crearInventarioPerks(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration shop = plugin.getShop();
		FileConfiguration config = plugin.getConfig();
		String title = shop.getString("shopPerksInventoryTitle", "");
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));

		ConfigurationSection perksItems = shop.getConfigurationSection("perks_items");
		if (perksItems != null) {
			for(String key : perksItems.getKeys(false)) {
				ItemStack item = UtilidadesItems.crearItem(shop, "perks_items."+key);
				if(key.equals("coins_info")) {
					ItemMeta meta = item.getItemMeta();
					if (meta != null) {
						String ecoUsed = config.getString("economy_used", "");
                        meta.getDisplayName();
                        String displayName = meta.getDisplayName();
						if("vault".equals(ecoUsed)) {
							Economy econ = plugin.getEconomy();
							int coins = econ != null ? (int) econ.getBalance(jugador) : 0;
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", coins + "")));
						}else if("token_manager".equals(ecoUsed)) {
							TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
							int tokens = Math.toIntExact(tokenManager != null ? tokenManager.getTokens(jugador).orElse(0) : 0);
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", tokens + "")));
						}else {
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", PaintballAPI.getCoins(jugador) + "")));
						}

						item.setItemMeta(meta);
					}
				}
				if(shop.contains("perks_items."+key+".slot")) {
					int slot = Integer.parseInt(shop.getString("perks_items."+key+".slot", "-1"));
					if(slot != -1) {
						inv.setItem(slot, item);
					}
				}
			}
		}

		ItemStack item = UtilidadesItems.crearItem(shop, "perks_items.decorative_item");
		for(int i=0;i<=8;i++) {
			inv.setItem(i, item);
		}
		for(int i=36;i<=44;i++) {
			inv.setItem(i, item);
		}

		int levelExtraLives = PaintballAPI.getPerkLevel(jugador, "extra_lives");
		List<String> lista = shop.getStringList("perks_upgrades.extra_lives");
		for(int i=0;i<lista.size();i++) {
			if(i > levelExtraLives-1) {
				item = UtilidadesItems.crearItem(shop, "perks_items.extra_lives_perk_item");
			}else {
				item = UtilidadesItems.crearItem(shop, "perks_items.extra_lives_bought_perk_item");
			}
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				String[] separados = lista.get(i).split(";");
				if (separados.length >= 3) {
                    meta.getDisplayName();
                    String displayName = meta.getDisplayName();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%name%", separados[2])));
					List<String> lore = meta.getLore();
					if (lore != null) {
						lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
						meta.setLore(lore);
					}
					item.setItemMeta(meta);
				}
			}
			inv.setItem(9+i, item);

			if(i==8) {
				break;
			}
		}

		int levelInitialKillcoins = PaintballAPI.getPerkLevel(jugador, "initial_killcoins");
		lista = shop.getStringList("perks_upgrades.initial_killcoins");
		for(int i=0;i<lista.size();i++) {
			if(i > levelInitialKillcoins-1) {
				item = UtilidadesItems.crearItem(shop, "perks_items.initial_killcoins_perk_item");
			}else {
				item = UtilidadesItems.crearItem(shop, "perks_items.initial_killcoins_bought_perk_item");
			}
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				String[] separados = lista.get(i).split(";");
				if (separados.length >= 3) {
                    meta.getDisplayName();
                    String displayName = meta.getDisplayName();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%name%", separados[2])));
					List<String> lore = meta.getLore();
					if (lore != null) {
						lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
						meta.setLore(lore);
					}
					item.setItemMeta(meta);
				}
			}
			inv.setItem(18+i, item);

			if(i==8) {
				break;
			}
		}

		int levelExtraKillcoins = PaintballAPI.getPerkLevel(jugador, "extra_killcoins");
		lista = shop.getStringList("perks_upgrades.extra_killcoins");
		for(int i=0;i<lista.size();i++) {
			if(i > levelExtraKillcoins-1) {
				item = UtilidadesItems.crearItem(shop, "perks_items.extra_killcoins_perk_item");
			}else {
				item = UtilidadesItems.crearItem(shop, "perks_items.extra_killcoins_bought_perk_item");
			}
			ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				String[] separados = lista.get(i).split(";");
				if (separados.length >= 3) {
                    meta.getDisplayName();
                    String displayName = meta.getDisplayName();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%name%", separados[2])));
					List<String> lore = meta.getLore();
					if (lore != null) {
						lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
						meta.setLore(lore);
					}
					item.setItemMeta(meta);
				}
			}
			inv.setItem(27+i, item);

			if(i==8) {
				break;
			}
		}

		jugador.openInventory(inv);
	}

	@EventHandler
	public void clickInventarioPerks(InventoryClickEvent event){
		FileConfiguration shop = plugin.getShop();
		String rawTitle = shop.getString("shopPerksInventoryTitle", "&9Paintball Shop &7- &9Perks");
		String pathInventory = ChatColor.translateAlternateColorCodes('&', rawTitle);
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();
		String rawPrefix = messages.getString("prefix", "&7[&cPaintball&7]");
		String prefix = ChatColor.translateAlternateColorCodes('&', rawPrefix)+" ";

		if(pathInventoryM != null && pathInventoryM.equals(ChatColor.stripColor(event.getView().getTitle()))){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			final Player jugador = (Player) event.getWhoClicked();
			event.setCancelled(true);
			if(event.getClickedInventory() != null && event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
				FileConfiguration config = plugin.getConfig();
				if(!event.getCurrentItem().getType().equals(Material.AIR)) {
					int slot = event.getSlot();
					if(slot >= 9 && slot <= 17 || slot >= 18 && slot <= 26 || slot >= 27 && slot <= 35) {
						int slotSum;
						String perk;
						if(slot <= 17) {
							//ExtraLives
							slotSum = 9;
							perk = "extra_lives";
						}else if(slot <= 26) {
							//Initial KillCoins
							slotSum = 18;
							perk = "initial_killcoins";
						}else {
							//Extra KillCoins
							slotSum = 27;
							perk = "extra_killcoins";
						}

						List<String> lista = shop.getStringList("perks_upgrades."+perk);
						for(int i=0;i<lista.size();i++) {
							String[] separados = lista.get(i).split(";");
							if(slot == slotSum+i) {
								//If it's level 1, it means the next level to unlock is slot 10.
								int nivel = PaintballAPI.getPerkLevel(jugador, perk);
								int slotADesbloquear = nivel+slotSum;
								if(slot == slotADesbloquear) {
									int cost = Integer.parseInt(separados[1]);
									double dinero;
									String ecoUsed = config.getString("economy_used", "");
									String noSuffMsg = messages.getString("buyNoSufficientCoins", "&cYou don't have enough coins to buy that.");
									String noSuffFormatted = ChatColor.translateAlternateColorCodes('&', noSuffMsg);

									if("vault".equals(ecoUsed)) {
										Economy econ = plugin.getEconomy();
										dinero = econ != null ? econ.getBalance(jugador) : 0;
										if(dinero < cost) {
											jugador.sendMessage(prefix + noSuffFormatted);
											return;
										}
										if (econ != null) {
											econ.withdrawPlayer(jugador, cost);
										}
									}else if("token_manager".equals(ecoUsed)) {
										TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
										float dineroF = tokenManager != null ? tokenManager.getTokens(jugador).orElse(0) : 0;
										if(dineroF < cost) {
											jugador.sendMessage(prefix + noSuffFormatted);
											return;
										}
										if (tokenManager != null) {
											tokenManager.removeTokens(jugador, cost);
										}
									}
									else {
										dinero = PaintballAPI.getCoins(jugador);
										if(dinero < cost) {
											jugador.sendMessage(prefix + noSuffFormatted);
											return;
										}
										PaintballAPI.removeCoins(jugador, cost);
									}
									if(MySQL.isEnabled(config)) {
										MySQL.setPerkJugadorAsync(plugin, jugador.getUniqueId().toString(), jugador.getName(), perk, nivel+1);
									}else {
										plugin.registerPlayer(jugador.getUniqueId() +".yml");
										if(plugin.getJugador(jugador.getName()) == null) {
											plugin.agregarJugadorDatos(new JugadorDatos(jugador.getName(),jugador.getUniqueId().toString(),0,0,0,0,0, new ArrayList<>(), new ArrayList<>()));
										}
										JugadorDatos jDatos = plugin.getJugador(jugador.getName());
										if (jDatos != null) {
											jDatos.setPerk(perk, nivel+1);
										}
									}
									String unlockedMsg = messages.getString("perkUnlocked", "&aPerk %name% &aUnlocked!");
									String unlockedFormatted = ChatColor.translateAlternateColorCodes('&', unlockedMsg).replace("%name%", separados[2]);
									jugador.sendMessage(prefix + unlockedFormatted);

									String shopSound = config.getString("shopUnlockSound");
									if (shopSound != null) {
										String[] separadosSound = shopSound.split(";");
										if (separadosSound.length >= 3) {
											try {
												Sound sound = ValueOfPatch.valueOf(separadosSound[0]);
												jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separadosSound[1]), Float.parseFloat(separadosSound[2]));
											}catch(Exception ex) {
												Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separadosSound[0]+" &7is not valid."));
											}
										}
									}
									Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> InventarioShop.crearInventarioPerks(jugador, plugin), 5L);
								}else if(slot > slotADesbloquear) {
									String errPrevMsg = messages.getString("perkErrorPrevious", "");
									jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', errPrevMsg));
									return;
								}else {
									String errUnlMsg = messages.getString("perkErrorUnlocked", "");
									jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', errUnlMsg));
									return;
								}

								return;
							}
						}
					}else {
						String menuSlotStr = shop.getString("perks_items.go_to_menu.slot");
						if(menuSlotStr != null && slot == Integer.parseInt(menuSlotStr)) {
							InventarioShop.crearInventarioPrincipal(jugador, plugin);
						}
					}
				}
			}
		}
	}

	public static void crearInventarioHats(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration shop = plugin.getShop();
		FileConfiguration config = plugin.getConfig();
		String title = shop.getString("shopHatsInventoryTitle", "&9Paintball Shop &7- &9Hats");
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));

		ConfigurationSection hatsItems = shop.getConfigurationSection("hats_items");
		if (hatsItems != null) {
			for(String key : hatsItems.getKeys(false)) {
				ItemStack item = UtilidadesItems.crearItem(shop, "hats_items."+key);
				if(key.equals("coins_info")) {
					ItemMeta meta = item.getItemMeta();
					if (meta != null) {
						String ecoUsed = config.getString("economy_used", "");
                        meta.getDisplayName();
                        String displayName = meta.getDisplayName();
						if("vault".equals(ecoUsed)) {
							Economy econ = plugin.getEconomy();
							int coins = econ != null ? (int) econ.getBalance(jugador) : 0;
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", coins + "")));
						}else if("token_manager".equals(ecoUsed)) {
							TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
							int tokens = Math.toIntExact(tokenManager != null ? tokenManager.getTokens(jugador).orElse(0) : 0);
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", tokens + "")));
						}
						else {
							meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName.replace("%coins%", PaintballAPI.getCoins(jugador) + "")));
						}

						item.setItemMeta(meta);
					}
				}else {
					if(!key.equals("go_to_menu")) {
						if(PaintballAPI.hasHat(jugador, key)) {
							ItemMeta meta = item.getItemMeta();
							if (meta != null) {
								List<String> lore = shop.getStringList("hats_items."+key+".bought_lore");
								lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
								meta.setLore(lore);
								item.setItemMeta(meta);
							}
						}
					}
				}

				if(shop.contains("hats_items."+key+".skull_id")) {
					String id = shop.getString("hats_items."+key+".skull_id");
					String textura = shop.getString("hats_items."+key+".skull_texture");
					item = UtilidadesItems.getCabeza(item, id, textura);
				}

				if(shop.contains("hats_items."+key+".slot")) {
					int slot = Integer.parseInt(shop.getString("hats_items."+key+".slot", "-1"));
					if(slot != -1) {
						inv.setItem(slot, item);
					}
				}

			}
		}

		jugador.openInventory(inv);
	}

	@EventHandler
	public void clickInventarioHats(InventoryClickEvent event){
		FileConfiguration shop = plugin.getShop();
		String rawTitle = shop.getString("shopHatsInventoryTitle", "&9Paintball Shop &7- &9Hats");
		String pathInventory = ChatColor.translateAlternateColorCodes('&', rawTitle);
		String pathInventoryM = ChatColor.stripColor(pathInventory);

		FileConfiguration messages = plugin.getMessages();
		String rawPrefix = messages.getString("prefix", "&7[&cPaintball&7]");
		String prefix = ChatColor.translateAlternateColorCodes('&', rawPrefix) + " ";

		String viewTitle = event.getView().getTitle();
		String strippedViewTitle = ChatColor.stripColor(viewTitle);

		if(pathInventoryM != null && pathInventoryM.equals(strippedViewTitle)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			final Player jugador = (Player) event.getWhoClicked();
			event.setCancelled(true);

			if(event.getClickedInventory() != null && event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
				FileConfiguration config = plugin.getConfig();
				if(!event.getCurrentItem().getType().equals(Material.AIR)) {
					int slot = event.getSlot();
					ConfigurationSection hatsSection = shop.getConfigurationSection("hats_items");
					if (hatsSection != null) {
						for(String key : hatsSection.getKeys(false)) {
							String slotStr = shop.getString("hats_items."+key+".slot");
							if (slotStr == null) continue;
							int itemSlot = Integer.parseInt(slotStr);

							if("go_to_menu".equals(key)) {
								if(slot == itemSlot) {
									InventarioShop.crearInventarioPrincipal(jugador, plugin);
									return;
								}
							}else if(!"coins_info".equals(key)) {
								if(slot == itemSlot) {
									if(PaintballAPI.hasHat(jugador, key)) {
										String hatErrBought = messages.getString("hatErrorBought", "&cYou've already bought that hat!");
										jugador.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', hatErrBought));
										return;
									}

									String costStr = shop.getString("hats_items."+key+".cost", "0");
									int cost = Integer.parseInt(costStr);
									double dinero;

									String ecoUsed = config.getString("economy_used", "");
									String noCoinsMsg = messages.getString("buyNoSufficientCoins", "&cYou don't have enough coins to buy that.");
									String noCoinsFormatted = ChatColor.translateAlternateColorCodes('&', noCoinsMsg);

									if("vault".equals(ecoUsed)) {
										Economy econ = plugin.getEconomy();
										dinero = econ != null ? econ.getBalance(jugador) : 0;
										if(dinero < cost) {
											jugador.sendMessage(prefix + noCoinsFormatted);
											return;
										}
										if (econ != null) {
											econ.withdrawPlayer(jugador, cost);
										}
									}else if("token_manager".equals(ecoUsed)) {
										TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
										float dineroF = tokenManager != null ? tokenManager.getTokens(jugador).orElse(0) : 0;
										if(dineroF < cost) {
											jugador.sendMessage(prefix + noCoinsFormatted);
											return;
										}
										if (tokenManager != null) {
											tokenManager.removeTokens(jugador, cost);
										}
									}
									else {
										dinero = PaintballAPI.getCoins(jugador);
										if(dinero < cost) {
											jugador.sendMessage(prefix + noCoinsFormatted);
											return;
										}
										PaintballAPI.removeCoins(jugador, cost);
									}

									if(MySQL.isEnabled(config)) {
										MySQL.agregarJugadorHatAsync(plugin, jugador.getUniqueId().toString(), jugador.getName(), key);
									}else {
										plugin.registerPlayer(jugador.getUniqueId() +".yml");
										if(plugin.getJugador(jugador.getName()) == null) {
											plugin.agregarJugadorDatos(new JugadorDatos(jugador.getName(),jugador.getUniqueId().toString(),0,0,0,0,0, new ArrayList<>(), new ArrayList<>()));
										}
										JugadorDatos jDatos = plugin.getJugador(jugador.getName());
										if (jDatos != null) {
											jDatos.agregarHat(key);
										}
									}

									String hatBoughtMsg = messages.getString("hatBought", "&aHat %name% &abought!");
									String hatName = shop.getString("hats_items."+key+".name", "ERROR! CHECK YOUR HATS CONFIG!");
									String hatBoughtFormatted = ChatColor.translateAlternateColorCodes('&', hatBoughtMsg.replace("%name%", hatName));
									jugador.sendMessage(prefix + hatBoughtFormatted);

									String shopSound = config.getString("shopUnlockSound");
									if (shopSound != null) {
										String[] separadosSound = shopSound.split(";");
										if (separadosSound.length >= 3) {
											try {
												Sound sound = ValueOfPatch.valueOf(separadosSound[0]);
												jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separadosSound[1]), Float.parseFloat(separadosSound[2]));
											}catch(Exception ex) {
												Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separadosSound[0]+" &7is not valid."));
											}
										}
									}

									Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> InventarioShop.crearInventarioHats(jugador, plugin), 5L);
									return;
								}
							}
						}
					}
				}
			}
		}
	}
}
