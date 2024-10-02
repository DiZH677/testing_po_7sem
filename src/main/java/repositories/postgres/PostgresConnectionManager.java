package repositories.postgres;

import java.sql.*;


public class PostgresConnectionManager {
    private static volatile PostgresConnectionManager instance;
    private Connection connection;

    private PostgresConnectionManager(String serverAddress, String dbName, String username, String password) {
        this.connection = connectToPostgres(serverAddress, dbName, username, password);
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


    private Connection connectToPostgres(String serverAddress, String dbName, String username, String password) {
        Connection conn = null;
        String url = "jdbc:postgresql://" + serverAddress + "/" + dbName;
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean isClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    // Выполнение запроса query
    public ResultSet executeSQLQuery(String query) {
        ResultSet result = null;
        try {
            Statement statement = connection.createStatement();
            boolean hasResults = statement.execute(query);
            if (hasResults) {
                result = statement.getResultSet();
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL query: " + e.getMessage());
        }
        return result;
    }



    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection to the PostgreSQL server closed successfully.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
