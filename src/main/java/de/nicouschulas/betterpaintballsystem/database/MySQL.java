package de.nicouschulas.betterpaintballsystem.database;

import de.nicouschulas.betterpaintballsystem.BetterPaintballSystem;
import de.nicouschulas.betterpaintballsystem.api.Hat;
import de.nicouschulas.betterpaintballsystem.api.Perk;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MySQL {

	public static boolean isEnabled(FileConfiguration config) {
		return config.getBoolean("mysql-database.enabled", false);
	}

	public static void createTablePlayers(ConexionDatabase conexion) {
		String query = "CREATE TABLE IF NOT EXISTS " + conexion.getTablePlayers()
				+ " (`UUID` varchar(200), `Name` varchar(40), `Date` varchar(100), `Year` INT(10), `Month` INT(5), `Week` INT(5), `Day` INT(5), `Arena` varchar(40), `Win` INT(2), `Tie` INT(2), `Lose` INT(2), `Kills` INT(5), `Coins` INT(10), `Global_Data` INT(2) )";

		try (Connection conn = conexion.getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTablePerks(ConexionDatabase conexion) {
		String query = "CREATE TABLE IF NOT EXISTS " + conexion.getTablePerks()
				+ " (`UUID` varchar(200), `Name` varchar(40), `Perk` varchar(40), `Level` INT(2) )";

		try (Connection conn = conexion.getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTableHats(ConexionDatabase conexion) {
		String query = "CREATE TABLE IF NOT EXISTS " + conexion.getTableHats()
				+ " (`UUID` varchar(200), `Name` varchar(40), `Hat` varchar(40), `Selected` INT(2) )";

		try (Connection conn = conexion.getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static int getStatsTotales(BetterPaintballSystem plugin, String name, String tipo) {
		int cantidad = 0;
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Name=? AND Global_Data=1)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					cantidad = resultado.getInt(tipo);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cantidad;
	}

	public static boolean jugadorExiste(BetterPaintballSystem plugin, String player) {
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Name=? AND Global_Data=1)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, player);
			try (ResultSet resultado = statement.executeQuery()) {
				if (resultado.next()) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void actualizarJugadorPartidaAsync(final BetterPaintballSystem plugin, final String uuid, final String player, final int wins, final int loses, final int ties, final int kills) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "UPDATE " + plugin.getConexionDatabase().getTablePlayers() + " SET Win=?, Tie=?, Lose=?, Kills=? WHERE (Name=? AND Global_Data=1)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setInt(1, wins);
				statement.setInt(2, ties);
				statement.setInt(3, loses);
				statement.setInt(4, kills);
				statement.setString(5, player);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void agregarCoinsJugadorAsync(final BetterPaintballSystem plugin, final String player, final int coins) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "UPDATE " + plugin.getConexionDatabase().getTablePlayers() + " SET Coins=`Coins`+? WHERE (Name=? AND Global_Data=1)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setInt(1, coins);
				statement.setString(2, player);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void removerCoinsJugadorAsync(final BetterPaintballSystem plugin, final String player, final int coins) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "UPDATE " + plugin.getConexionDatabase().getTablePlayers() + " SET Coins=`Coins`-? WHERE (Name=? AND Global_Data=1)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setInt(1, coins);
				statement.setString(2, player);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void crearJugadorPartidaAsync(final BetterPaintballSystem plugin, final String uuid, final String name, final String arena, final int win, final int tie, final int lose, final int kills, final int coins, final int global) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			int mes = calendar.get(Calendar.MONTH);
			int año = calendar.get(Calendar.YEAR);
			int dia = calendar.get(Calendar.DAY_OF_MONTH);
			int dia_semana = calendar.get(Calendar.WEEK_OF_MONTH);

			String query = "INSERT INTO " + plugin.getConexionDatabase().getTablePlayers() + " (UUID,Name,Date,Year,Month,Week,Day,Arena,Win,Tie,Lose,Kills,Coins,Global_Data) VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement insert = conn.prepareStatement(query)) {

				insert.setString(1, uuid);
				insert.setString(2, name);
				insert.setString(3, String.valueOf(date.getTime()));
				insert.setInt(4, año);
				insert.setInt(5, mes);
				insert.setInt(6, dia_semana);
				insert.setInt(7, dia);
				insert.setString(8, arena);
				insert.setInt(9, win);
				insert.setInt(10, tie);
				insert.setInt(11, lose);
				insert.setInt(12, kills);
				insert.setInt(13, coins);
				insert.setInt(14, global);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean jugadorTieneHat(BetterPaintballSystem plugin, String player, String hat) {
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTableHats() + " WHERE (Name=? AND Hat=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, player);
			statement.setString(2, hat);
			try (ResultSet resultado = statement.executeQuery()) {
				if (resultado.next()) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void agregarJugadorHatAsync(final BetterPaintballSystem plugin, final String uuid, final String name, final String hat) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "INSERT INTO " + plugin.getConexionDatabase().getTableHats() + " (UUID,Name,Hat,Selected) VALUE (?,?,?,?)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement insert = conn.prepareStatement(query)) {

				insert.setString(1, uuid);
				insert.setString(2, name);
				insert.setString(3, hat);
				insert.setInt(4, 0);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean jugadorTieneHatSeleccionado(BetterPaintballSystem plugin, String player, String hat) {
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTableHats() + " WHERE (Name=? AND Hat=? AND Selected=1)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, player);
			statement.setString(2, hat);
			try (ResultSet resultado = statement.executeQuery()) {
				if (resultado.next()) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<Hat> getHatsJugador(BetterPaintballSystem plugin, String name) {
		ArrayList<Hat> hats = new ArrayList<>();
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTableHats() + " WHERE (Name=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					String hat = resultado.getString("Hat");
					int selected = resultado.getInt("Selected");
					hats.add(new Hat(hat, selected == 1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hats;
	}

	public static void deseleccionarHats(final BetterPaintballSystem plugin, final String player) {
		String query = "UPDATE " + plugin.getConexionDatabase().getTableHats() + " SET Selected=0 WHERE (Name=? AND Selected=1)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, player);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void seleccionarHatAsync(final BetterPaintballSystem plugin, final String player, final String hat) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			deseleccionarHats(plugin, player);
			String query = "UPDATE " + plugin.getConexionDatabase().getTableHats() + " SET Selected=1 WHERE (Name=? AND Hat=?)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement statement = conn.prepareStatement(query)) {

				statement.setString(1, player);
				statement.setString(2, hat);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void crearJugadorPerkAsync(final BetterPaintballSystem plugin, final String uuid, final String name, final String perk) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "INSERT INTO " + plugin.getConexionDatabase().getTablePerks() + " (UUID,Name,Perk,Level) VALUE (?,?,?,?)";

			try (Connection conn = plugin.getConexionDatabase().getConnection();
			     PreparedStatement insert = conn.prepareStatement(query)) {

				insert.setString(1, uuid);
				insert.setString(2, name);
				insert.setString(3, perk);
				insert.setInt(4, 1);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static int getNivelPerk(BetterPaintballSystem plugin, String name, String perk) {
		int level = 0;
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePerks() + " WHERE (Name=? AND Perk=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			statement.setString(2, perk);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					level = resultado.getInt("Level");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return level;
	}

	public static boolean jugadorPerkExiste(BetterPaintballSystem plugin, String player, String perk) {
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePerks() + " WHERE (Name=? AND Perk=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, player);
			statement.setString(2, perk);
			try (ResultSet resultado = statement.executeQuery()) {
				if (resultado.next()) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<Perk> getPerksJugador(BetterPaintballSystem plugin, String name) {
		ArrayList<Perk> perks = new ArrayList<>();
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePerks() + " WHERE (Name=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					String perk = resultado.getString("Perk");
					int level = resultado.getInt("Level");
					perks.add(new Perk(perk, level));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return perks;
	}

	public static void setPerkJugadorAsync(final BetterPaintballSystem plugin, final String uuid, final String player, final String perk, final int level) {
		if (jugadorPerkExiste(plugin, player, perk)) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				String query = "UPDATE " + plugin.getConexionDatabase().getTablePerks() + " SET Level=? WHERE (Name=? AND Perk=?)";

				try (Connection conn = plugin.getConexionDatabase().getConnection();
				     PreparedStatement statement = conn.prepareStatement(query)) {

					statement.setInt(1, level);
					statement.setString(2, player);
					statement.setString(3, perk);
					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		} else {
			crearJugadorPerkAsync(plugin, uuid, player, perk);
		}
	}

	public static JugadorDatos getJugador(BetterPaintballSystem plugin, String name) {
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Global_Data=1 AND Name=?)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					int wins = resultado.getInt("Win");
					int loses = resultado.getInt("Lose");
					int ties = resultado.getInt("Tie");
					int kills = resultado.getInt("Kills");
					int coins = resultado.getInt("Coins");
					return new JugadorDatos(name, "", wins, loses, ties, kills, coins, null, null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<JugadorDatos> getPlayerDataMonthly(BetterPaintballSystem plugin) {
		ArrayList<JugadorDatos> players = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		calendar.setTime(date);
		int mes = calendar.get(Calendar.MONTH);
		int año = calendar.get(Calendar.YEAR);

		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Year=? AND Month=? AND Global_Data=0)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setInt(1, año);
			statement.setInt(2, mes);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					String name = resultado.getString("Name");
					if (!yaContieneJugador(players, name)) {
						int[] stats = getStatsTotalesMonthly(plugin, name, mes, año);
						JugadorDatos p = new JugadorDatos(name, "", stats[0], stats[1], stats[2], stats[3], 0, null, null);
						players.add(p);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}

	public static ArrayList<JugadorDatos> getPlayerDataWeekly(BetterPaintballSystem plugin) {
		ArrayList<JugadorDatos> players = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		calendar.setTime(date);
		int mes = calendar.get(Calendar.MONTH);
		int año = calendar.get(Calendar.YEAR);
		int semana = calendar.get(Calendar.WEEK_OF_MONTH);

		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Year=? AND Month=? AND Week=? AND Global_Data=0)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setInt(1, año);
			statement.setInt(2, mes);
			statement.setInt(3, semana);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					String name = resultado.getString("Name");
					if (!yaContieneJugador(players, name)) {
						int[] stats = getStatsTotalesWeekly(plugin, name, mes, año, semana);
						JugadorDatos p = new JugadorDatos(name, "", stats[0], stats[1], stats[2], stats[3], 0, null, null);
						players.add(p);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}

	private static boolean yaContieneJugador(ArrayList<JugadorDatos> players, String player) {
		for (JugadorDatos p : players) {
			if (p.getName().equals(player)) {
				return true;
			}
		}
		return false;
	}

	public static int[] getStatsTotalesWeekly(BetterPaintballSystem plugin, String name, int mes, int año, int semana) {
		int[] cantidades = {0, 0, 0, 0}; // Wins, Loses, Ties, Kills
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Name=? AND Year=? AND Month=? AND Week=? AND Global_Data=0)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			statement.setInt(2, año);
			statement.setInt(3, mes);
			statement.setInt(4, semana);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					cantidades[0] += resultado.getInt("Win");
					cantidades[1] += resultado.getInt("Lose");
					cantidades[2] += resultado.getInt("Tie");
					cantidades[3] += resultado.getInt("Kills");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cantidades;
	}

	public static int[] getStatsTotalesMonthly(BetterPaintballSystem plugin, String name, int mes, int año) {
		int[] cantidades = {0, 0, 0, 0}; // Wins, Loses, Ties, Kills
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE (Name=? AND Year=? AND Month=? AND Global_Data=0)";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			statement.setString(1, name);
			statement.setInt(2, año);
			statement.setInt(3, mes);
			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					cantidades[0] += resultado.getInt("Win");
					cantidades[1] += resultado.getInt("Lose");
					cantidades[2] += resultado.getInt("Tie");
					cantidades[3] += resultado.getInt("Kills");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cantidades;
	}

	public static ArrayList<JugadorDatos> getPlayerData(BetterPaintballSystem plugin) {
		ArrayList<JugadorDatos> players = new ArrayList<>();
		String query = "SELECT * FROM " + plugin.getConexionDatabase().getTablePlayers() + " WHERE Global_Data=1";

		try (Connection conn = plugin.getConexionDatabase().getConnection();
		     PreparedStatement statement = conn.prepareStatement(query)) {

			try (ResultSet resultado = statement.executeQuery()) {
				while (resultado.next()) {
					String name = resultado.getString("Name");
					if (!yaContieneJugador(players, name)) {
						int wins = resultado.getInt("Win");
						int loses = resultado.getInt("Lose");
						int ties = resultado.getInt("Tie");
						int kills = resultado.getInt("Kills");
						int coins = resultado.getInt("Coins");
						JugadorDatos p = new JugadorDatos(name, "", wins, loses, ties, kills, coins, null, null);
						players.add(p);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}
}