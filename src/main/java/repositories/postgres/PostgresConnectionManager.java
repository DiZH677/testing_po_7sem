package repositories.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresConnectionManager {
    private static volatile PostgresConnectionManager instance;
    private HikariDataSource dataSource;
    private Statement statement;
    Connection connection;

    private PostgresConnectionManager(String serverAddress, String dbName, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + serverAddress + "/" + dbName);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(100); // Максимальное количество соединений в пуле
        config.setConnectionTimeout(4000); // Время ожидания получения соединения
//        config.setIdleTimeout(250); // Время ожидания перед закрытием неактивных соединений

        dataSource = new HikariDataSource(config);
    }

    public static PostgresConnectionManager getInstance(String serverAddress, String dbName, String username, String password) {
        if (instance == null || instance.isClosed()) {
            synchronized (PostgresConnectionManager.class) {
                if (instance == null || instance.isClosed()) {
                    instance = new PostgresConnectionManager(serverAddress, dbName, username, password);
                }
            }
        }
        return instance;
    }

    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }

    // Выполнение запроса query
    public ResultSet executeSQLQuery(String query) {
        ResultSet resultSet = null;

        try {
            if (connection == null) {
                connection = dataSource.getConnection();
            }
            if (statement == null) {
                statement = connection.createStatement();
            }
            boolean hasResults = statement.execute(query);
            if (hasResults) {
                resultSet = statement.getResultSet();
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query: " + e.getMessage());
        } finally {
            // Закрываем Connection, если он не нужен
            // Не закрываем Statement, так как мы можем вернуть ResultSet
        }

        // Возвращаем ресурсы, которые должны быть закрыты пользователем
        return resultSet;
    }

    public void setSearchPath(String schemaName) {
        try {
            if (connection == null) {
                connection = dataSource.getConnection();
            }
            String query = String.format("SET search_path TO %s;", schemaName);
            connection.createStatement().execute(query);
            System.out.println("Set search_path TO " + schemaName);
        } catch (SQLException e) {
            System.out.println("Error setting search path: " + e.getMessage());
        }
    }


    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("Connection pool closed successfully.");
        }
    }
}
