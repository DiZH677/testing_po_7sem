package tests.integration.repositories;

import configurator.Configurator;
import entities.Car;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import params.CarParams;
import repositories.postgres.PostgresCarRepository;
import repositories.postgres.PostgresConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresCarRepositoryIT {

    private PostgresConnectionManager connectionManager;
    private PostgresCarRepository repository;

    @BeforeAll
    public void setUp() throws SQLException {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        String schemaName = System.getProperty("testSchema");
        connectionManager.setSearchPath(schemaName);

        repository = new PostgresCarRepository(connectionManager);
        // Очистка таблицы перед тестами
        if (connectionManager.getConnection() == null) {
            fail("Error testing while connection");
        }
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'Test Description', '2024-09-29 10:00:00+03', 55.0, 37.0, 'Main road', 'Accident', 2, 3);");
    }

    @AfterAll
    public void tearDown() {
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.closeConnection();
    }

    @Test
    public void testSaveCar() throws Exception {
        // Arrange
        Car car = new Car(1, 1, "Toyota", "Camry", 2019, "Black", "Sedan");

        // Act
        boolean result = repository.saveCar(car);

        // Assert
        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals("Toyota", resultSet.getString("marka_ts"));

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM vehicle WHERE id = 1;");
    }

    @Test
    public void testGetCar() throws Exception {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO vehicle " +
                "VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");

        // Act
        Car retrievedCar = repository.getCar(1);

        // Assert
        assertNotNull(retrievedCar);
        assertEquals(1, retrievedCar.getId());

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM vehicle WHERE id = 1;");
    }

    @Test
    public void testEditCar() throws Exception {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO vehicle " +
                "VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        Car editedCar = new Car(1, 1, "Toyota", "Camry", 2020, "Blue", "Sedan");

        // Act
        boolean result = repository.editCar(editedCar);

        // Assert
        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(2020, resultSet.getInt("car_year"));
        assertEquals("Blue", resultSet.getString("color"));

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM vehicle WHERE id = 1;");
    }

    @Test
    public void testDeleteCar() throws Exception {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");

        // Act
        boolean result = repository.delCar(1);

        // Assert
        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertFalse(resultSet.next());

        connectionManager.executeSQLQuery("DELETE FROM vehicle WHERE id = 1;");
    }

    @Test
    public void testGetCarsByParams() throws Exception {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (2, 1, 'Honda', 'Accord', 'r', 'Sedan', 2019, 'Black');");
        CarParams params = new CarParams();
        params.carIdBegin = 0;
        params.carIdEnd = 3;

        // Act
        List<Car> result = repository.getCarsByParams(params);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Toyota", result.get(0).getMarka());

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM vehicle WHERE id IN (1, 2);");
    }

    @Test
    public void testGetCarsByParamsNoResults() throws Exception {
        // Arrange
        CarParams params = new CarParams();
        params.marka = "Nonexistent";

        // Act
        List<Car> result = repository.getCarsByParams(params);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
