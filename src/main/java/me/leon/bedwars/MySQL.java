package me.leon.bedwars;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

    private final BedWars plugin;
    private Connection connection;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public MySQL(BedWars plugin) {
        this.plugin = plugin;

        this.host = plugin.getConfig().getString("mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("mysql.port", 3306);
        this.database = plugin.getConfig().getString("mysql.database", "bedwars");
        this.username = plugin.getConfig().getString("mysql.username", "root");
        this.password = plugin.getConfig().getString("mysql.password", "");
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?autoReconnect=true&useSSL=false";

            connection = DriverManager.getConnection(url, username, password);

            plugin.getLogger().info("MySQL Verbindung hergestellt!");

            createTables();

        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("MySQL Verbindung fehlgeschlagen!");
            e.printStackTrace();
        }
    }

    private void createTables() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS `bedwars_stats` (\n" +
                "  `uuid` VARCHAR(36) NOT NULL PRIMARY KEY,\n" +
                "  `kills` INT DEFAULT 0,\n" +
                "  `deaths` INT DEFAULT 0,\n" +
                "  `wins` INT DEFAULT 0,\n" +
                "  `losses` INT DEFAULT 0,\n" +
                "  `beds_destroyed` INT DEFAULT 0,\n" +
                "  `games_played` INT DEFAULT 0,\n" +
                "  INDEX `idx_wins` (`wins`),\n" +
                "  INDEX `idx_kills` (`kills`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            plugin.getLogger().info("Datentabelle erstellt oder existiert bereits!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Tabelle!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL Verbindung geschlossen!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}