package repositories.postgres;

import IRepositories.IParticipantRepository;
import entities.Participant;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.ParticipantParams;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresParticipantRepository implements IParticipantRepository{
    private final PostgresConnectionManager connectionManager;

    public PostgresParticipantRepository(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }


    @Override
    public Participant getParticipant(int id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        Participant prt;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM participant WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект DTP из результатов запроса
                prt = new Participant(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("category"),
                        rs.getString("health"),
                        rs.getString("pol"),
                        rs.getBoolean("safety_belt")
                );

                // Закроем ресурсы и вернем объект DTP
                rs.close();
                pstmt.close();
            }
            else prt = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting Participant by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Participant by id...\n" + e.getMessage());
        }

        return prt;
    }

    @Override
    public boolean saveParticipant(Participant pr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Определение максимального значения id
            String getMaxIdQuery = "SELECT MAX(id) FROM participant";
            Statement getMaxIdStatement = conn.createStatement();
            ResultSet maxIdResult = getMaxIdStatement.executeQuery(getMaxIdQuery);
            int maxId = 0;
            if (maxIdResult.next()) {
                maxId = maxIdResult.getInt(1) + 1;
            }

            // Подготовим запрос
            // Формирование запроса
            if (pr.getId() == -1) { pr.setId(maxId); }
            String sql = "INSERT INTO participant (id, vehicle_id, category, safety_belt, pol, health) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pr.getId());
            pstmt.setInt(2, pr.getCarId());
            pstmt.setString(3, pr.getCategory());
            pstmt.setBoolean(4, pr.getSafetyBelt());
            pstmt.setString(5, pr.getPol());
            pstmt.setString(6, pr.getHealth());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while saving Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving Participant...\n" + e.getMessage());
        }

        return true;
    }

    @Override
    public boolean delParticipant(int del_id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "DELETE FROM participant WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, del_id);
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while deleting Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting Participant...\n" + e.getMessage());
        }

        return true;
    }

    @Override
    public boolean editParticipant(Participant pr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "UPDATE participant SET vehicle_id=?, category=?, safety_belt=?, pol=?, health=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pr.getCarId());
            pstmt.setString(2, pr.getCategory());
            pstmt.setBoolean(3, pr.getSafetyBelt());
            pstmt.setString(4, pr.getPol());
            pstmt.setString(5, pr.getHealth());
            pstmt.setInt(6, pr.getId());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while editing Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing Participant...\n" + e.getMessage());
        }

        return true;
    }

    @Override
    public List<Participant> getParticByParams(ParticipantParams params) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        List<Participant> result = new ArrayList<>();
        try {
            String query = getQuery(params);
            PreparedStatement pstmt = conn.prepareStatement(query);
            int parameterIndex = 1;

            setQueryParameters(pstmt, parameterIndex, params);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(mapToParticipant(rs));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Participant by params...\n" + e.getMessage());
        }

        return result;
    }

    private void setQueryParameters(PreparedStatement pstmt, int parameterIndex, ParticipantParams params) throws SQLException {
        if (params.prIdBegin != null) {
            pstmt.setInt(parameterIndex++, params.prIdBegin);
        }
        if (params.prIdEnd != null) {
            pstmt.setInt(parameterIndex++, params.prIdEnd);
        }
        if (params.category != null) {
            pstmt.setString(parameterIndex++, params.category);
        }
        if (params.pol != null) {
            pstmt.setString(parameterIndex++, params.pol);
        }
        if (params.health != null) {
            pstmt.setString(parameterIndex++, params.health);
        }
        if (params.safety_belt != null) {
            pstmt.setBoolean(parameterIndex++, params.safety_belt);
        }
        // Добавьте аналогичные блоки для других параметров
    }

    private Participant mapToParticipant(ResultSet rs) throws SQLException {
        return new Participant(
                rs.getInt("id"),
                rs.getInt("vehicle_id"),
                rs.getString("category"),
                rs.getString("health"),
                rs.getString("pol"),
                rs.getBoolean("safety_belt")
        );
    }

    private static String getQuery(ParticipantParams params) {
        String query = "SELECT * FROM participant WHERE 1=1"; // начало запроса

        if (params.prIdBegin != null) {
            query += " AND id >= ?";
        }
        if (params.prIdEnd != null) {
            query += " AND id <= ?";
        }
        if (params.category != null) {
            query += " AND category = ?";
        }
        if (params.pol != null) {
            query += " AND pol = ?";
        }
        if (params.health != null) {
            query += " AND health = ?";
        }
        if (params.safety_belt != null) {
            query += " AND safety_belt = ?";
        }
        query += " ORDER BY ID";
        return query;
    }
}
