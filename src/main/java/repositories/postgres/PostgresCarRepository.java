package repositories.postgres;

import IRepositories.ICarRepository;
import entities.Car;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.CarParams;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresCarRepository implements ICarRepository {
    private final PostgresConnectionManager connectionManager;

    public PostgresCarRepository(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }


    @Override
    public Car getCar(int id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        Car car;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM vehicle WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект DTP из результатов запроса
                car = new Car(
                        rs.getInt("id"),
                        rs.getInt("dtp_id"),
                        rs.getString("marka_ts"),
                        rs.getString("m_ts"),
                        rs.getInt("car_year"),
                        rs.getString("color"),
                        rs.getString("type_ts")
                );

                // Закроем ресурсы и вернем объект DTP
                rs.close();
                pstmt.close();
            }
            else car = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting Car by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Car by id... " + e.getMessage());
        }

        CustomLogger.logInfo("Successful getting Car by id", this.getClass().getSimpleName());
        return car;
    }

    @Override
    public boolean saveCar(Car cr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Определение максимального значения id
            String getMaxIdQuery = "SELECT MAX(id) FROM vehicle";
            Statement getMaxIdStatement = conn.createStatement();
            ResultSet maxIdResult = getMaxIdStatement.executeQuery(getMaxIdQuery);
            int maxId = 0;
            if (maxIdResult.next()) {
                maxId = maxIdResult.getInt(1) + 1;
            }

            // Подготовим запрос
            // Формирование запроса
            if (cr.getId() == -1) { cr.setId(maxId); }
            String sql = "INSERT INTO vehicle (id, dtp_id, marka_ts, m_ts, car_year, color, type_ts) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cr.getId());
            pstmt.setInt(2, cr.getDtpId());
            pstmt.setString(3, cr.getMarka());
            pstmt.setString(4, cr.getModel());
            pstmt.setInt(5, cr.getCarYear());
            pstmt.setString(6, cr.getColor());
            pstmt.setString(7, cr.getTypeTS());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while saving Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving Car..." + e.getMessage());
        }

        CustomLogger.logInfo("Successful saving Car", this.getClass().getSimpleName());
        return true;
    }

    @Override
    public boolean delCar(int del_id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "DELETE FROM vehicle WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, del_id);
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while deleting Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting Car..." + e.getMessage());
        }

        CustomLogger.logInfo("Successful deleting Car", this.getClass().getSimpleName());
        return true;
    }

    @Override
    public boolean editCar(Car cr) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "UPDATE vehicle SET dtp_id=?, marka_ts=?, m_ts=?, car_year=?, color=?, type_ts=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, cr.getDtpId());
            pstmt.setString(2, cr.getMarka());
            pstmt.setString(3, cr.getModel());
            pstmt.setInt(4, cr.getCarYear());
            pstmt.setString(5, cr.getColor());
            pstmt.setString(6, cr.getTypeTS());
            pstmt.setInt(7, cr.getId());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while editing Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing Car..." + e.getMessage());
        }

        CustomLogger.logInfo("Successful editing Car", this.getClass().getSimpleName());
        return true;
    }

    @Override
    public List<Car> getCarsByParams(CarParams params) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        List<Car> result = new ArrayList<>();
        try {
            String query = getQuery(params);
            PreparedStatement pstmt = conn.prepareStatement(query);
            int parameterIndex = 1;

            setQueryParameters(pstmt, parameterIndex, params);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(mapToCar(rs));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting Car by params", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Car by params...\n" + e.getMessage());
        }

        CustomLogger.logInfo("Successful getting Car by params", this.getClass().getSimpleName());
        return result;
    }

    private void setQueryParameters(PreparedStatement pstmt, int parameterIndex, CarParams params) throws SQLException {
        if (params.carIdBegin != null) {
            pstmt.setInt(parameterIndex++, params.carIdBegin);
        }
        if (params.carIdEnd != null) {
            pstmt.setInt(parameterIndex++, params.carIdEnd);
        }
        if (params.color != null) {
            pstmt.setString(parameterIndex++, params.color);
        }
        if (params.marka != null) {
            pstmt.setString(parameterIndex++, params.marka);
        }
        if (params.model != null) {
            pstmt.setString(parameterIndex++, params.model);
        }
        // Аналогичные блоки для других параметров
    }

    private Car mapToCar(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("id"),
                rs.getInt("dtp_id"),
                rs.getString("marka_ts"),
                rs.getString("m_ts"),
                rs.getInt("car_year"),
                rs.getString("color"),
                rs.getString("type_ts")
        );
    }

    private static String getQuery(CarParams params) {
        String query = "SELECT * FROM vehicle WHERE 1=1"; // начало запроса

        if (params.carIdBegin != null) {
            query += " AND id >= ?";
        }
        if (params.carIdEnd != null) {
            query += " AND id <= ?";
        }
        if (params.color != null) {
            query += " AND color = ?";
        }
        if (params.marka != null) {
            query += " AND marka_ts = ?";
        }
        if (params.model != null) {
            query += " AND m_ts = ?";
        }
        query += " ORDER BY ID";
        return query;
    }
}
