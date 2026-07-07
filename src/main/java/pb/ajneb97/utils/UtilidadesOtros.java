package pb.ajneb97.utils;

import pb.ajneb97.PaintballBattle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class UtilidadesOtros {

	public static boolean isChatNew() {
		ServerVersion serverVersion = PaintballBattle.serverVersion;
		return serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_19_R1);
	}

	public static boolean isNew() {
		ServerVersion serverVersion = PaintballBattle.serverVersion;
		return serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_16_R1);
	}

	public static boolean isLegacy() {
		ServerVersion serverVersion = PaintballBattle.serverVersion;
		return !serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_13_R1);
	}

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
		if(UtilidadesOtros.isLegacy()) {
			float red = 0;
			float green = 0;
			float blue = 0;
			boolean redstone = false;
			if(particle.startsWith("REDSTONE;")) {
				redstone = true;
				String[] sep = particle.split(";");
				int rgb = Integer.parseInt(sep[1]);
				particle = sep[0];
				Color color = Color.fromRGB(rgb);
				red = (float) color.getRed()/255;
				green = (float) color.getGreen()/255;
				blue = (float) color.getBlue()/255;
			}
			try {
				Class<?> packetEnumParticle = getNMSClass("EnumParticle");
				Method packetEnumMethod = packetEnumParticle.getMethod("valueOf", String.class);
				Object enumParticle = packetEnumMethod.invoke(null,particle);
				Class<?> packetClass = getNMSClass("PacketPlayOutWorldParticles");

				Constructor<?> packetConstructor = null;
				for(Constructor<?> c : packetClass.getConstructors()) {
					if(c.toGenericString().contains("EnumParticle")) {
						packetConstructor = c;
					}
				}

				if (packetConstructor != null) {
					Object packet;
					if(redstone) {
						packet = packetConstructor.newInstance(enumParticle, true, (float)l.getX(), (float)l.getY(), (float)l.getZ(), red, green, blue, count, 0, null);
					}else {
						packet = packetConstructor.newInstance(enumParticle, false, (float)l.getX(), (float)l.getY(), (float)l.getZ(), xOffset, yOffset, yOffset, speed, count, null);
					}
					Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", getNMSClass("Packet"));
					for(Player player : Bukkit.getOnlinePlayers()) {
						sendPacket.invoke(getConnection(player), packet);
					}
				}

			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			         | SecurityException | NoSuchMethodException | NoSuchFieldException | InstantiationException _) {
			}
		}else {
			if (l.getWorld() != null) {
				l.getWorld().spawnParticle(Particle.valueOf(particle),l,count,xOffset,yOffset,zOffset,speed);
			}
		}
	}

	private static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "net.minecraft.server." + version + nmsClassString;
		return Class.forName(name);
	}

	private static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method getHandle = player.getClass().getMethod("getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		return conField.get(nmsPlayer);
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