package de.nicouschulas.betterpaintballsystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class ConexionDatabase {

	private HikariDataSource dataSource;
	private final String tablePlayerdata;
	private final String tablePlayerPerks;
	private final String tablePlayerHats;

	public ConexionDatabase(FileConfiguration config) {
		String host = config.getString("mysql-database.host", "localhost");
		int port = config.getInt("mysql-database.port", 3306);
		String database = config.getString("mysql-database.database", "paintball");
		String username = config.getString("mysql-database.username", "root");
		String password = config.getString("mysql-database.password", "");

		this.tablePlayerdata = "paintball_data";
		this.tablePlayerPerks = "paintball_perks";
		this.tablePlayerHats = "paintball_hats";

		setupHikariCP(host, port, database, username, password);

		if (this.dataSource != null && !this.dataSource.isClosed()) {
			MySQL.createTablePlayers(this);
			MySQL.createTablePerks(this);
			MySQL.createTableHats(this);
		}
	}

	private void setupHikariCP(String host, int port, String database, String username, String password) {
		try {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
			config.setUsername(username);
			config.setPassword(password);

			config.setMaximumPoolSize(10);
			config.setMinimumIdle(2);
			config.setConnectionTimeout(30000);
			config.setIdleTimeout(600000);
			config.setMaxLifetime(1800000);

			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

			this.dataSource = new HikariDataSource(config);
			Bukkit.getLogger().info("[Paintball] Successfully connected to the Database via HikariCP!");
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "[Paintball] Could not connect to MySQL database!", e);
		}
	}


	public Connection getConnection() throws SQLException {
		if (dataSource == null) {
			throw new SQLException("[Paintball] HikariDataSource is not initialized!");
		}
		return dataSource.getConnection();
	}


	public void close() {
		if (dataSource != null && !dataSource.isClosed()) {
			dataSource.close();
		}
	}

	public String getTablePlayers() {
		return this.tablePlayerdata;
	}

	public String getTablePerks() {
		return this.tablePlayerPerks;
	}

	public String getTableHats() {
		return this.tablePlayerHats;
	}
}