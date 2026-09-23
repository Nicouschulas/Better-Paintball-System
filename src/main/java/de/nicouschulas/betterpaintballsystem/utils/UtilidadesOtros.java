package de.nicouschulas.betterpaintballsystem.utils;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class UtilidadesOtros {

	public static String getTiempo(int tiempo) {
		int minutos = tiempo/60;
		int segundos = tiempo - (minutos*60);
		String segundosMsg;
		String minutosMsg;
		if(segundos >= 0 && segundos <= 9) {
			segundosMsg = "0"+segundos;
		}else {
			segundosMsg = segundos+"";
		}

		if(minutos >= 0 && minutos <= 9) {
			minutosMsg = "0"+minutos;
		}else {
			minutosMsg = minutos+"";
		}

		return minutosMsg+":"+segundosMsg;
	}

	public static int coinsGanados(Player jugador,FileConfiguration config) {
		String coinsString = config.getString("coins_per_kill");
		if(coinsString != null && coinsString.contains("-")) {
			String[] separados = coinsString.split("-");
			int num1 = Integer.parseInt(separados[0]);
			int num2 = Integer.parseInt(separados[1]);
			return getNumeroAleatorio(num1,num2);
		}else if(coinsString != null) {
			return Integer.parseInt(coinsString);
		}
		return 0;
	}

	public static int getNumeroAleatorio(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public static void generarParticula(String particle, Location l, float xOffset, float yOffset, float zOffset, float speed, int count) {
		if (l != null && l.getWorld() != null) {
			l.getWorld().spawnParticle(Particle.valueOf(particle), l, count, xOffset, yOffset, zOffset, speed);
		}
	}

	public static boolean pasaConfigInventario(Player jugador,FileConfiguration config) {
		if("true".equals(config.getString("empty_inventory_to_join"))) {
			PlayerInventory inv = jugador.getInventory();
			for(ItemStack item : inv.getContents()) {
				if(item != null && item.getType() != Material.AIR) {
					return false;
				}
			}
			for(ItemStack item : inv.getArmorContents()) {
				if(item != null && item.getType() != Material.AIR) {
					return false;
				}
			}
		}
		return true;
	}

	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
				return x;
			}

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					x = switch (func) {
						case "sqrt" -> Math.sqrt(x);
						case "sin" -> Math.sin(Math.toRadians(x));
						case "cos" -> Math.cos(Math.toRadians(x));
						case "tan" -> Math.tan(Math.toRadians(x));
						default -> throw new RuntimeException("Unknown function: " + func);
					};
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}
}