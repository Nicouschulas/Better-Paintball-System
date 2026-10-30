package de.nicouschulas.betterpaintballsystem.lib.titleapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TitleAPI implements Listener {

    @Deprecated
    public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(player, fadeIn, stay, fadeOut, null, message);
    }

    @Deprecated
    public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }

    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        TitleSendEvent titleSendEvent = new TitleSendEvent(player, title, subtitle);
        Bukkit.getPluginManager().callEvent(titleSendEvent);
        if (titleSendEvent.isCancelled()) {
            return;
        }

        if (title == null || title.isEmpty()) {
            title = " ";
        } else {
            title = ChatColor.translateAlternateColorCodes('&', title.replace("%player%", player.getDisplayName()));
        }

        if (subtitle == null || subtitle.isEmpty()) {
            subtitle = " ";
        } else {
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle.replace("%player%", player.getDisplayName()));
        }

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void clearTitle(Player player) {
        sendTitle(player, 0, 0, 0, "", "");
    }

    public static void sendTabTitle(Player player, String header, String footer) {
        if (header == null) header = "";
        if (footer == null) footer = "";

        TabTitleSendEvent tabTitleSendEvent = new TabTitleSendEvent(player, header, footer);
        Bukkit.getPluginManager().callEvent(tabTitleSendEvent);
        if (tabTitleSendEvent.isCancelled()) {
            return;
        }

        header = ChatColor.translateAlternateColorCodes('&', header.replace("%player%", player.getDisplayName()));
        footer = ChatColor.translateAlternateColorCodes('&', footer.replace("%player%", player.getDisplayName()));

        player.setPlayerListHeaderFooter(header, footer);
    }
}