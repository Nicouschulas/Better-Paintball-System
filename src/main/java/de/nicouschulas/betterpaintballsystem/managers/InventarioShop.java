package de.nicouschulas.betterpaintballsystem.managers;

import java.util.ArrayList;
import java.util.List;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', shop.getString("shopInventoryTitle")));
		for(String key : shop.getConfigurationSection("shop_items").getKeys(false)) {
			ItemStack item = UtilidadesItems.crearItem(shop, "shop_items."+key);
			int slot = Integer.parseInt(shop.getString("shop_items."+key+".slot"));
			if(slot != - 1) {
				inv.setItem(slot, item);
			}	
		}
		
		jugador.openInventory(inv);
	}
	
	@EventHandler
	public void clickInventarioPrincipal(InventoryClickEvent event){
		FileConfiguration shop = plugin.getShop();
		String pathInventory = ChatColor.translateAlternateColorCodes('&', shop.getString("shopInventoryTitle"));
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
            Player jugador = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if(event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
                int slot = event.getSlot();
                for(String key : shop.getConfigurationSection("shop_items").getKeys(false)) {
                    if(slot == Integer.parseInt(shop.getString("shop_items."+key+".slot"))) {
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
	
	public static void crearInventarioPerks(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration shop = plugin.getShop();
		FileConfiguration config = plugin.getConfig();
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', shop.getString("shopPerksInventoryTitle")));
		for(String key : shop.getConfigurationSection("perks_items").getKeys(false)) {
			ItemStack item = UtilidadesItems.crearItem(shop, "perks_items."+key);
			if(key.equals("coins_info")) {
				ItemMeta meta = item.getItemMeta();
				if(config.getString("economy_used").equals("vault")) {
					Economy econ = plugin.getEconomy();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", (int) econ.getBalance(jugador)+"")));
				}else if(config.getString("economy_used").equals("token_manager")) {
					TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", tokenManager.getTokens(jugador).orElse(0)+"")));
				}
				else {
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", (int) PaintballAPI.getCoins(jugador)+"")));
				}

				item.setItemMeta(meta);
			}
			if(shop.contains("perks_items."+key+".slot")) {
				int slot = Integer.valueOf(shop.getString("perks_items."+key+".slot"));
				if(slot != - 1) {
					inv.setItem(slot, item);
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
			String[] separados = lista.get(i).split(";");
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%name%", separados[2])));
			List<String> lore = meta.getLore();
            lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
			meta.setLore(lore);
			item.setItemMeta(meta);
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
			String[] separados = lista.get(i).split(";");
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%name%", separados[2])));
			List<String> lore = meta.getLore();
            lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
			meta.setLore(lore);
			item.setItemMeta(meta);
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
			String[] separados = lista.get(i).split(";");
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%name%", separados[2])));
			List<String> lore = meta.getLore();
            lore.replaceAll(s -> s.replace("%amount%", separados[0]).replace("%cost%", separados[1]));
			meta.setLore(lore);
			item.setItemMeta(meta);
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
		String pathInventory = ChatColor.translateAlternateColorCodes('&', shop.getString("shopPerksInventoryTitle"));
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
            final Player jugador = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if(event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
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
                                    if(config.getString("economy_used").equals("vault")) {
                                        Economy econ = plugin.getEconomy();
                                        dinero = econ.getBalance(jugador);
                                        if(dinero < cost) {
                                            jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
                                            return;
                                        }
                                        econ.withdrawPlayer(jugador, cost);
                                    }else if(config.getString("economy_used").equals("token_manager")) {
                                        TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
                                        float dineroF = tokenManager.getTokens(jugador).orElse(0);
                                        if(dineroF < cost) {
                                            jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
                                            return;
                                        }
                                        tokenManager.removeTokens(jugador, cost);
                                    }
                                    else {
                                        dinero = PaintballAPI.getCoins(jugador);
                                        if(dinero < cost) {
                                            jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
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
                                        jDatos.setPerk(perk, nivel+1);
                                    }
                                    jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("perkUnlocked").replace("%name%", separados[2])));
                                    String[] separadosSound = config.getString("shopUnlockSound").split(";");
                                    try {
                                        Sound sound = ValueOfPatch.valueOf(separadosSound[0]);
                                        jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separadosSound[1]), Float.parseFloat(separadosSound[2]));
                                    }catch(Exception ex) {
                                        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separadosSound[0]+" &7is not valid."));
                                    }
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        public void run() {
                                            InventarioShop.crearInventarioPerks(jugador, plugin);
                                        }
                                    }, 5L);
                                }else if(slot > slotADesbloquear) {
                                    jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("perkErrorPrevious")));
                                    return;
                                }else {
                                    jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("perkErrorUnlocked")));
                                    return;
                                }

                                return;
                            }
                        }
                    }else if(slot == Integer.parseInt(shop.getString("perks_items.go_to_menu.slot"))) {
                        InventarioShop.crearInventarioPrincipal(jugador, plugin);
                    }
                }
            }
        }
	}
	
	public static void crearInventarioHats(Player jugador, BetterPaintballSystem plugin) {
		FileConfiguration shop = plugin.getShop();
		FileConfiguration config = plugin.getConfig();
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', shop.getString("shopHatsInventoryTitle")));
		for(String key : shop.getConfigurationSection("hats_items").getKeys(false)) {
			ItemStack item = UtilidadesItems.crearItem(shop, "hats_items."+key);
			if(key.equals("coins_info")) {
				ItemMeta meta = item.getItemMeta();
				if(config.getString("economy_used").equals("vault")) {
					Economy econ = plugin.getEconomy();
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", (int) econ.getBalance(jugador)+"")));
				}else if(config.getString("economy_used").equals("token_manager")) {
					TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", tokenManager.getTokens(jugador).orElse(0)+"")));
				}
				else {
					meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName().replace("%coins%", (int) PaintballAPI.getCoins(jugador)+"")));
				}

				item.setItemMeta(meta);
			}else {
				if(!key.equals("go_to_menu")) {
					if(PaintballAPI.hasHat(jugador, key)) {
						ItemMeta meta = item.getItemMeta();
						List<String> lore = shop.getStringList("hats_items."+key+".bought_lore");
                        lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
						meta.setLore(lore);
						item.setItemMeta(meta);
					}
				}
			}
			
			if(shop.contains("hats_items."+key+".skull_id")) {
				String id = shop.getString("hats_items."+key+".skull_id");
				String textura = shop.getString("hats_items."+key+".skull_texture");
				item = UtilidadesItems.getCabeza(item, id, textura);
			}
			
			if(shop.contains("hats_items."+key+".slot")) {
				int slot = Integer.parseInt(shop.getString("hats_items."+key+".slot"));
				if(slot != - 1) {
					inv.setItem(slot, item);
				}
			}
				
		}
		
		jugador.openInventory(inv);
	}
	
	@EventHandler
	public void clickInventarioHats(InventoryClickEvent event){
		FileConfiguration shop = plugin.getShop();
		String pathInventory = ChatColor.translateAlternateColorCodes('&', shop.getString("shopHatsInventoryTitle"));
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
            final Player jugador = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if(event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
                FileConfiguration config = plugin.getConfig();
                if(!event.getCurrentItem().getType().equals(Material.AIR)) {
                    int slot = event.getSlot();
                    for(String key : shop.getConfigurationSection("hats_items").getKeys(false)) {
                        if(key.equals("go_to_menu")) {
                            if(slot == Integer.parseInt(shop.getString("hats_items."+key+".slot"))) {
                                InventarioShop.crearInventarioPrincipal(jugador, plugin);
                                return;
                            }
                        }else if(!key.equals("coins_info")) {
                            if(slot == Integer.parseInt(shop.getString("hats_items."+key+".slot"))) {
                                if(PaintballAPI.hasHat(jugador, key)) {
                                    jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("hatErrorBought")));
                                    return;
                                }
                                int cost = Integer.valueOf(shop.getString("hats_items."+key+".cost"));
                                double dinero;
                                if(config.getString("economy_used").equals("vault")) {
                                    Economy econ = plugin.getEconomy();
                                    dinero = econ.getBalance(jugador);
                                    if(dinero < cost) {
                                        jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
                                        return;
                                    }
                                    econ.withdrawPlayer(jugador, cost);
                                }else if(config.getString("economy_used").equals("token_manager")) {
                                    TokenManager tokenManager = (TokenManager) Bukkit.getPluginManager().getPlugin("TokenManager");
                                    float dineroF = tokenManager.getTokens(jugador).orElse(0);
                                    if(dineroF < cost) {
                                        jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
                                        return;
                                    }
                                    tokenManager.removeTokens(jugador, cost);
                                }
                                else {
                                    dinero = PaintballAPI.getCoins(jugador);
                                    if(dinero < cost) {
                                        jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("buyNoSufficientCoins")));
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
                                    jDatos.agregarHat(key);
                                }
                                jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("hatBought").replace("%name%", shop.getString("hats_items."+key+".name"))));
                                String[] separadosSound = config.getString("shopUnlockSound").split(";");
                                try {
                                    Sound sound = ValueOfPatch.valueOf(separadosSound[0]);
                                    jugador.playSound(jugador.getLocation(), sound, Float.parseFloat(separadosSound[1]), Float.parseFloat(separadosSound[2]));
                                }catch(Exception ex) {
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', BetterPaintballSystem.prefix+"&7Sound Name: &c"+separadosSound[0]+" &7is not valid."));
                                }
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        InventarioShop.crearInventarioHats(jugador, plugin);
                                    }
                                }, 5L);
                                return;
                            }
                        }
                    }
                }
            }
        }
	}
}
