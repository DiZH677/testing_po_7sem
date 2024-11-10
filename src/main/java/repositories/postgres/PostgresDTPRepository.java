package repositories.postgres;
import entities.DTP;
import IRepositories.IDTPRepository;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.DTPParams;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PostgresDTPRepository implements IDTPRepository {
    private final PostgresConnectionManager connectionManager;

    public PostgresDTPRepository(PostgresConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }



    @Override
    public DTP getDTP(int id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        DTP dtp;
        try {
            // Подготовим запрос
            String sql = "SELECT * FROM dtp WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            // Выполним запрос и получим результат
            ResultSet rs = pstmt.executeQuery();
            // Проверим, есть ли результаты
            if (rs.next()) {
                // Создадим объект DTP из результатов запроса
                dtp = new DTP(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getString("datetime"),
                        rs.getDouble("coord_W"),
                        rs.getDouble("coord_L"),
                        rs.getString("dor"),
                        rs.getString("osv"),
                        rs.getInt("count_Ts"),
                        rs.getInt("count_Parts")
                );

                // Закроем ресурсы и вернем объект DTP
                rs.close();
                pstmt.close();
            }
            else dtp = null;
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting DTP by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting DTP by id..." + e.getMessage());
        }

        CustomLogger.logInfo("SQL getting DTP by id was executed", this.getClass().getSimpleName());
        return dtp;
    }

    @Override
    public boolean saveDTP(DTP dtp) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Определение максимального значения id
            String getMaxIdQuery = "SELECT MAX(id) FROM dtp";
            Statement getMaxIdStatement = conn.createStatement();
            ResultSet maxIdResult = getMaxIdStatement.executeQuery(getMaxIdQuery);
            int maxId = 0;
            if (maxIdResult.next()) {
                maxId = maxIdResult.getInt(1) + 1;
            }

            // Подготовим запрос
            Timestamp timestamp = Timestamp.valueOf(dtp.getDatetime());
            // Формирование запроса
            if (dtp.getId() == -1) { dtp.setId(maxId); }
            String sql = "INSERT INTO dtp (id, description, datetime, coord_W, coord_L, dor, osv, count_Ts, count_Parts) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dtp.getId());
            pstmt.setString(2, dtp.getDescription());
            pstmt.setTimestamp(3, timestamp);
            pstmt.setDouble(4, dtp.getCoords().get(0));
            pstmt.setDouble(5, dtp.getCoords().get(1));
            pstmt.setString(6, dtp.getDor());
            pstmt.setString(7, dtp.getOsv());
            pstmt.setInt(8, dtp.getCountTs());
            pstmt.setInt(9, dtp.getCountParts());
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while saving DTP by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving DTP by id..." + e.getMessage());
        }

        CustomLogger.logInfo("SQL saving DTP by id was executed", this.getClass().getSimpleName());
        return true;
    }

    @Override
    public boolean delDTP(int del_id) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        try {
            // Подготовим запрос
            String sql = "DELETE FROM dtp WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, del_id);
            // Выполним запрос
            pstmt.executeUpdate();
            // Закроем ресурсы
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while deleting DTP by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting DTP by id..." + e.getMessage());
        }

        CustomLogger.logInfo("SQL deleting DTP by id was executed", this.getClass().getSimpleName());
        return true;
    }

    @Override
    public boolean editDTP(DTP dtp) throws RepositoryException {
    Connection conn = connectionManager.getConnection();
    if (conn == null)
        throw new RepositoryException("Connection is null");

    try {
        // Подготовим запрос
        String sql = "UPDATE dtp SET description=?, datetime=?, coord_W=?, coord_L=?, dor=?, osv=?, count_Ts=?, count_Parts=? WHERE id=?";
        Timestamp timestamp = Timestamp.valueOf(dtp.getDatetime());
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, dtp.getDescription());
        pstmt.setTimestamp(2, timestamp);
        pstmt.setDouble(3, dtp.getCoords().get(0));
        pstmt.setDouble(4, dtp.getCoords().get(1));
        pstmt.setString(5, dtp.getDor());
        pstmt.setString(6, dtp.getOsv());
        pstmt.setInt(7, dtp.getCountTs());
        pstmt.setInt(8, dtp.getCountParts());
        pstmt.setInt(9, dtp.getId());
        // Выполним запрос
        pstmt.executeUpdate();
        // Закроем ресурсы
        pstmt.close();
    } catch (SQLException e) {
        CustomLogger.logError("Error while editing DTP", this.getClass().getSimpleName());
        throw new RepositoryException("Error while editing DTP...\n" + e.getMessage());
    }

    CustomLogger.logInfo("SQL Editing DTP was executed", this.getClass().getSimpleName());
    return true;
}

    @Override
    public List<DTP> getDTPByParams(DTPParams params) throws RepositoryException {
        Connection conn = connectionManager.getConnection();
        if (conn == null) {
            CustomLogger.logError("Connection for DB is null", this.getClass().getSimpleName());
            throw new RepositoryException("Connection is null");
        }

        List<DTP> result = new ArrayList<>();
        try {
            String query = getQuery(params);

            PreparedStatement pstmt = conn.prepareStatement(query);

            int parameterIndex = 1;
            if (params.dtpBegin != null) {
                pstmt.setTimestamp(parameterIndex++, new java.sql.Timestamp(params.dtpBegin.getTime()));
            }
            if (params.dtpEnd != null) {
                pstmt.setTimestamp(parameterIndex++, new java.sql.Timestamp(params.dtpEnd.getTime()));
            }
            if (params.dtpIdBegin != null) {
                pstmt.setInt(parameterIndex++, params.dtpIdBegin);
            }
            if (params.dtpIdEnd != null) {
                pstmt.setInt(parameterIndex++, params.dtpIdEnd);
            }
            if (params.countTs != null) {
                pstmt.setInt(parameterIndex++, params.countTs);
            }

            // добавьте аналогичные блоки для остальных параметров

            ResultSet rs = pstmt.executeQuery();
            DTP dtp;
            while (rs.next()) {
                dtp = new DTP(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getString("datetime"),
                        rs.getDouble("coord_W"),
                        rs.getDouble("coord_L"),
                        rs.getString("dor"),
                        rs.getString("osv"),
                        rs.getInt("count_Ts"),
                        rs.getInt("count_Parts")
                );
                result.add(dtp);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            CustomLogger.logError("Error while getting DTP by main.java.params", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting DTP by main.java.params....\n" + e.getMessage());
        }

        CustomLogger.logInfo("SQL getting DTP by main.java.params was executed", this.getClass().getSimpleName());
        return result;
    }

    private static String getQuery(DTPParams params) {
        String query = "SELECT * FROM dtp WHERE 1=1"; // начало запроса

        if (params.dtpBegin != null) {
            query += " AND datetime >= ?";
        }
        if (params.dtpEnd != null) {
            query += " AND datetime <= ?";
        }
        if (params.dtpIdBegin != null) {
            query += " AND id >= ?";
        }
        if (params.dtpIdEnd != null) {
            query += " AND id <= ?";
        }
        if (params.countTs != null) {
            query += " AND count_Ts = ?";
        }
        query += " ORDER BY ID";
        return query;
    }
}
