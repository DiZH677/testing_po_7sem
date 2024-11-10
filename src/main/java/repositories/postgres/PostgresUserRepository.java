package repositories.postgres;

import IRepositories.IUserRepository;
import exceptions.RepositoryException;
import logger.CustomLogger;
import user.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresUserRepository implements IUserRepository {
    private final PostgresConnectionManager connectionManager;

    public PostgresUserRepository(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Integer> getAllUsersId() throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        List<Integer> userIds = new ArrayList<>();
        try {
            // Prepare the SQL query
            String sql = "SELECT id FROM users";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // Execute the query and get the result set
            ResultSet rs = pstmt.executeQuery();

            // Iterate through the result set and add main.java.user ids to the list
            while(rs.next()) {
                userIds.add(rs.getInt("id"));
            }

            // Close resources
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting all User ids", this.getClass().getSimpleName());
            System.out.println("Error while getting all User ids...\n" + e.getMessage());
        }

        return userIds;
    }

    @Override
    public User getUser(int id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        User usr;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM users WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект DTP из результатов запроса
                usr = new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("role")
                );

                // Закроем ресурсы и вернем объект DTP
                rs.close();
                pstmt.close();
            }
            else usr = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting User by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting User by id...\n" + e.getMessage());
        }

        return usr;
    }

    @Override
    public User getUser(String login, String password) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        User usr;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM users WHERE login=? AND password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, login);
            pstmt.setString(2, password);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект пользователя из результатов запроса
                usr = new User(
                        rs.getInt("id"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("role")
                );

                // Закроем ресурсы и вернем объект пользователя
                rs.close();
                pstmt.close();
            }
            else usr = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting User by login and password", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting User by login and password...\n" + e.getMessage());
        }

        return usr;
    }

    @Override
    public String getRole(int id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        String role;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM users WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект DTP из результатов запроса
                role = rs.getString("role");

                // Закроем ресурсы и вернем объект DTP
                rs.close();
                pstmt.close();
            }
            else role = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting UserRole by id", this.getClass().getSimpleName());
            System.out.println("Error while getting UserRole by id...\n" + e.getMessage());
            role = null;
        }

        return role;
    }

    @Override
    public boolean saveUser(User usr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Определение максимального значения id
            String getMaxIdQuery = "SELECT MAX(id) FROM users";
            Statement getMaxIdStatement = conn.createStatement();
            ResultSet maxIdResult = getMaxIdStatement.executeQuery(getMaxIdQuery);
            int maxId = 0;
            if (maxIdResult.next()) {
                maxId = maxIdResult.getInt(1) + 1;
            }

            // Подготовим запрос
            // Формирование запроса
            if (usr.getId() == -1) { usr.setId(maxId); }
            String sql = "INSERT INTO users (id, login, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, usr.getId());
            pstmt.setString(2, usr.getLogin());
            pstmt.setString(3, usr.getPassword());
            pstmt.setString(4, usr.getRole());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while saving User", this.getClass().getSimpleName());
            System.out.println("Error while saving User...\n" + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean delUser(int del_id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, del_id);
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while deleting User", this.getClass().getSimpleName());
            System.out.println("Error while deleting User...\n" + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean editUser(User usr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "UPDATE users SET login=?, password=?, role=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, usr.getLogin());
            pstmt.setString(2, usr.getPassword());
            pstmt.setString(3, usr.getRole());
            pstmt.setInt(4, usr.getId());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while editing User", this.getClass().getSimpleName());
            System.out.println("Error while editing User...\n" + e.getMessage());
            return false;
        }

        return true;
    }
}
