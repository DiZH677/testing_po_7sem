// * ТЕСТ С МОКАМИ
package tests.unit.repositories;


import entities.DTP;
import exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import params.DTPParams;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresDTPRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresDTPRepositoryTest {

    @Mock
    private PostgresConnectionManager connectionManager;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private PostgresDTPRepository repository;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getConnection()).thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT MAX(id) FROM dtp")).thenReturn(resultSet);
    }

    @Test
    public void testGetDTPPositive() throws Exception {
        // Arrange
        int dtpId = 123;
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(dtpId);
        when(resultSet.getString("description")).thenReturn("Accident Description");
        when(resultSet.getString("datetime")).thenReturn("2023-09-17 12:00:00");
        when(resultSet.getDouble("coord_W")).thenReturn(55.7558);
        when(resultSet.getDouble("coord_L")).thenReturn(37.6173);
        when(resultSet.getString("dor")).thenReturn("Highway");
        when(resultSet.getString("osv")).thenReturn("Clear");
        when(resultSet.getInt("count_Ts")).thenReturn(2);
        when(resultSet.getInt("count_Parts")).thenReturn(4);

        // Act
        DTP result = repository.getDTP(dtpId);

        // Assert
        assertNotNull(result);
        assertEquals(dtpId, result.getId());
        assertEquals("Accident Description", result.getDescription());
    }

    @Test
    public void testGetDTPConnectionNull() {
        // Arrange
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.getDTP(123));
        assertEquals("Connection is null", exception.getMessage());
    }

    @Test
    public void testGetDTPSQLException() throws SQLException {
        // Arrange
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("SQL error"));

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.getDTP(123));
        assertTrue(exception.getMessage().contains("Error while getting DTP by id"));
    }

    @Test
    public void testSaveDTPPositive() throws Exception {
        // Arrange
        DTP dtp = new DTP();
        dtp.setId(-1);  // В случае вставки нового объекта
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(123);  // Максимальный ID + 1

        // Act
        boolean result = repository.saveDTP(dtp);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testSaveDTPConnectionNull() {
        // Arrange
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.saveDTP(new DTP()));
        assertEquals("Connection is null", exception.getMessage());
    }

    @Test
    public void testSaveDTPSQLException() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error"));

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.saveDTP(new DTP()));
        assertTrue(exception.getMessage().contains("Error while saving DTP by id"));
    }

    @Test
    public void testDelDTPPositive() throws Exception {
        // Arrange
        int dtpId = 123;

        // Act
        boolean result = repository.delDTP(dtpId);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDelDTPSQLException() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error"));

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.delDTP(123));
        assertTrue(exception.getMessage().contains("Error while deleting DTP by id"));
    }

    @Test
    public void testEditDTPPositive() throws Exception {
        // Arrange
        DTP dtp = new DTP();
        dtp.setId(123);

        // Act
        boolean result = repository.editDTP(dtp);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testEditDTPSQLException() throws SQLException {
        // Arrange
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("SQL error"));

        // Act & Assert
        RepositoryException exception = assertThrows(RepositoryException.class, () -> repository.editDTP(new DTP()));
        assertTrue(exception.getMessage().contains("Error while editing DTP"));
    }

    @Test
    public void testGetDTPByParamsPositive() throws Exception {
        // Arrange
        DTPParams params = new DTPParams(1, 10, Date.valueOf("2024-01-01"), Date.valueOf("2024-12-31"), 5);

        DTP dtp = new DTP(1, "Description", "2024-01-01 12:00:00", 50.0, 50.0, "Road 1", "OSV 1", 3, 2);
        List<DTP> expectedList = new ArrayList<>();
        expectedList.add(dtp);

        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("description")).thenReturn("Description");
        when(resultSet.getString("datetime")).thenReturn("2024-01-01 12:00:00");
        when(resultSet.getDouble("coord_W")).thenReturn(50.0);
        when(resultSet.getDouble("coord_L")).thenReturn(50.0);
        when(resultSet.getString("dor")).thenReturn("Road 1");
        when(resultSet.getString("osv")).thenReturn("OSV 1");
        when(resultSet.getInt("count_Ts")).thenReturn(3);
        when(resultSet.getInt("count_Parts")).thenReturn(2);

        // Act
        List<DTP> result = repository.getDTPByParams(params);

        // Assert
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0).getId(), result.get(0).getId());
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetDTPByParamsNegative() throws Exception {
        // Arrange
        DTPParams params = new DTPParams(1, 10, Date.valueOf("2024-01-01"), Date.valueOf("2024-12-31"), 5);

        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> repository.getDTPByParams(params));
        verify(preparedStatement, times(1)).executeQuery();
    }
}

