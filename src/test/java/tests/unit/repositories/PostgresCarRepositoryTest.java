package tests.unit.repositories;
//
import entities.Car;
import exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import params.CarParams;
import repositories.postgres.PostgresCarRepository;
import repositories.postgres.PostgresConnectionManager;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresCarRepositoryTest {

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
    private PostgresCarRepository repository;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT MAX(id) FROM vehicle")).thenReturn(resultSet);
    }

    @Test
    public void testGetCarPositive() throws Exception {
        // Arrange
        int carId = 1;
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(carId);
        when(resultSet.getInt("dtp_id")).thenReturn(100);
        when(resultSet.getString("marka_ts")).thenReturn("Toyota");
        when(resultSet.getString("m_ts")).thenReturn("Camry");
        when(resultSet.getInt("car_year")).thenReturn(2019);
        when(resultSet.getString("color")).thenReturn("Black");
        when(resultSet.getString("type_ts")).thenReturn("Sedan");

        // Act
        Car result = repository.getCar(carId);

        // Assert
        assertNotNull(result);
        assertEquals(carId, result.getId());
        assertEquals("Toyota", result.getMarka());
        assertEquals("Camry", result.getModel());
        verify(preparedStatement, times(1)).setInt(1, carId);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetCarNoResult() throws Exception {
        // Arrange
        int carId = 1;
        when(resultSet.next()).thenReturn(false); // no data in ResultSet

        // Act
        Car result = repository.getCar(carId);

        // Assert
        assertNull(result);
        verify(preparedStatement, times(1)).setInt(1, carId);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetCarConnectionNull() {
        // Arrange
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        assertThrows(RepositoryException.class, () -> repository.getCar(1));
    }

    @Test
    public void testSaveCarPositive() throws Exception {
        // Arrange
        Car car = new Car(-1, 100, "Toyota", "Camry", 2019, "Black", "Sedan");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(123);  // максимальный ID + 1

        // Act
        boolean result = repository.saveCar(car);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testSaveCarConnectionNull() {
        // Arrange
        Car car = new Car(1, 100, "Toyota", "Camry", 2019, "Black", "Sedan");
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        assertThrows(RepositoryException.class, () -> repository.saveCar(car));
    }

    @Test
    public void testSaveCarSQLException() throws SQLException {
        // Arrange
        Car car = new Car(-1, 100, "Toyota", "Camry", 2019, "Black", "Sedan");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(123);
        doThrow(new SQLException("Database error")).when(preparedStatement).executeUpdate();

        // Act & Assert
        assertThrows(RepositoryException.class, () -> repository.saveCar(car));
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteCarPositive() throws Exception {
        // Arrange
        int carIdToDelete = 123;
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act
        boolean result = repository.delCar(carIdToDelete);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
        verify(preparedStatement).setInt(1, carIdToDelete);
    }

    @Test
    public void testDeleteCarNegativeConnectionNull() {
        // Arrange
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.delCar(123);
        });
    }

    @Test
    public void testEditCarPositive() throws Exception {
        // Arrange
        Car car = new Car(123, 1, "Toyota", "Camry", 2015, "Red", "Sedan");
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act
        boolean result = repository.editCar(car);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
        verify(preparedStatement).setInt(7, car.getId());
    }

    @Test
    public void testEditCarNegativeSQLException() throws Exception {
        // Arrange
        Car car = new Car(123, 1, "Toyota", "Camry", 2015, "Red", "Sedan");
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.editCar(car);
        });
    }

    @Test
    public void testGetCarsByParamsPositive() throws Exception {
        // Arrange
        CarParams params = new CarParams();
        params.marka = "Toyota";
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(123);
        when(resultSet.getString("marka_ts")).thenReturn("Toyota");

        // Act
        List<Car> result = repository.getCarsByParams(params);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Toyota", result.get(0).getMarka());
    }

    @Test
    public void testGetCarsByParamsNegativeSQLException() throws Exception {
        // Arrange
        CarParams params = new CarParams();
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.getCarsByParams(params);
        });
    }

}

