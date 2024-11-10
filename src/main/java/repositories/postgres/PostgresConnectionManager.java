package repositories.postgres;

import java.sql.*;


public class PostgresConnectionManager {
    private static volatile PostgresConnectionManager instance;
    private Connection connection;
    private boolean readOnly = false;

    private PostgresConnectionManager(String serverAddress, String dbName, String username, String password) {
        this.connection = connectToPostgres(serverAddress, dbName, username, password);
    }

    public static PostgresConnectionManager getInstance(String serverAddress, String dbName, String username, String password) {
        if (instance == null) {
            instance = new PostgresConnectionManager(serverAddress, dbName, username, password);
        }
        return instance;
    }


    private Connection connectToPostgres(String serverAddress, String dbName, String username, String password) {
        Connection conn = null;
        String url = "jdbc:postgresql://" + serverAddress + "/" + dbName;
        try {
            conn = DriverManager.getConnection(url, username, password);
            String onlyReadFlag = System.getProperty("OnlyRead");
            if ("true".equalsIgnoreCase(onlyReadFlag)) {
                readOnly = true;
                conn.setReadOnly(true);  // Устанавливаем режим только для чтения, если флаг true
                System.out.println("Connected to the PostgreSQL server with read-only permissions.");
            }
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // Выполнение запроса query
    public ResultSet executeSQLQuery(String query) {
        ResultSet result = null;
        if (query.trim().toUpperCase().startsWith("SELECT")) {  // Проверка, что запрос является SELECT
            System.out.println(query.trim().toUpperCase());
            System.out.println("ReadOnly " + readOnly);
            try {
                Statement statement = connection.createStatement();
                boolean hasResults = statement.execute(query);
                if (hasResults) {
                    result = statement.getResultSet();
                }
            } catch (SQLException e) {
                System.out.println("Error executing SQL query: " + e.getMessage());
            }
        } else {
            System.out.println("Only SELECT queries are allowed.");
        }
//        try {
//            Statement statement = connection.createStatement();
//            boolean hasResults = statement.execute(query);
//            if (hasResults) {
//                result = statement.getResultSet();
//            }
//        } catch (SQLException e) {
//            System.out.println("Error executing SQL query: " + e.getMessage());
//        }
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
