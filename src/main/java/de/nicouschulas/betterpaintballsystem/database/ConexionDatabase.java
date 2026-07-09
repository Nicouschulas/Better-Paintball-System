package de.nicouschulas.betterpaintballsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class ConexionDatabase {

	private Connection connection;
	private final String host;
	private final String database;
	private final String username;
	private final String password;
	private final String tablePlayerdata;
	private final String tablePlayerPerks;
	private final String tablePlayerHats;
	private final int port;

	public ConexionDatabase(FileConfiguration config){
		this.host = config.getString("mysql-database.host", "localhost");
		this.port = config.getInt("mysql-database.port", 3306);
		this.database = config.getString("mysql-database.database", "paintball");
		this.username = config.getString("mysql-database.username", "root");
		this.password = config.getString("mysql-database.password", "");
		this.tablePlayerdata = "paintball_data";
		this.tablePlayerPerks = "paintball_perks";
		this.tablePlayerHats = "paintball_hats";

		mySqlAbrirConexion();
		MySQL.createTablePlayers(this);
		MySQL.createTablePerks(this);
		MySQL.createTableHats(this);
	}

	public String getTablePlayers(){
		return this.tablePlayerdata;
	}

	public String getTablePerks(){
		return this.tablePlayerPerks;
	}

	public String getTableHats(){
		return this.tablePlayerHats;
	}

	public String getDatabase() {
		return this.database;
	}

	private void mySqlAbrirConexion(){
		try {
			synchronized(this){
				if(getConnection() != null && !getConnection().isClosed()){
					Bukkit.getLogger().warning("Error while connecting to the Database: Connection already open!");
					return;
				}

				setConnection(DriverManager.getConnection("jdbc:mysql://"+this.host+":"+this.port+"/"+this.database,this.username,this.password));

				Bukkit.getLogger().info("Successfully connected to the Database!");
			}
		} catch (SQLException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not connect to MySQL database!", e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}