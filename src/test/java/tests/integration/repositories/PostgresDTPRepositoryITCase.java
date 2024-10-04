package tests.integration.repositories;

import configurator.Configurator;
import entities.DTP;
import exceptions.RepositoryException;
import org.junit.jupiter.api.*;
import params.DTPParams;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresDTPRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostgresDTPRepositoryITCase {
    private static PostgresConnectionManager connectionManager;
    private PostgresDTPRepository repository;

    @BeforeAll
    public void setUp() {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        String schemaName = System.getProperty("testSchema");
        connectionManager.setSearchPath(schemaName);

        repository = new PostgresDTPRepository(connectionManager);
    }

    @AfterAll
    public void tearDown() {
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.closeConnection();
    }

    @Test
    public void testSaveDTP() throws RepositoryException, SQLException {
        // Arrange
        DTP dtp = new DTP(1, "Test Description", "2024-09-29 10:00:00", 55.0, 37.0, "Main road", "Accident", 2, 3);

        // Act
        boolean isSaved = repository.saveDTP(dtp);

        // Assert
        assertTrue(isSaved, "DTP should be saved successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        DTP newDTP = new DTP(resultSet.getInt(1), resultSet.getString(2),
                resultSet.getString(3), resultSet.getDouble(4),
                resultSet.getDouble(5), resultSet.getString(6),
                resultSet.getString(7), resultSet.getInt(8),
                resultSet.getInt(9));
        assertDTP(dtp, newDTP);
        resultSet.close();

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM dtp WHERE id = 1;");
    }

    @Test
    public void testGetDTP() throws RepositoryException {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'Test Description', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");

        // Act
        DTP retrievedDTP = repository.getDTP(1);

        // Assert
        assertNotNull(retrievedDTP, "Retrieved DTP should not be null");
        assertEquals(retrievedDTP.getDescription(), "Test Description");
        DTP newDTP = new DTP(1, "Test Description", "2024-09-29 10:00:00", 55.0, 37.0, "Main road", "Accident", 2, 1);
        assertDTP(retrievedDTP, newDTP);

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM dtp WHERE id = 1;");
    }

    @Test
    public void testEditDTP() throws RepositoryException, SQLException {
        // Arrange
        DTP dtp = new DTP(1, "Test Description", "2024-09-29 10:00:00", 55.0, 37.0, "Main road", "Accident", 2, 3);
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'Test Description', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2, 3)");

        // Act
        dtp.setDescription("Updated Description");
        Boolean res = repository.editDTP(dtp);

        // Assert
        assertTrue(res);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT COUNT(*) as cnt FROM dtp WHERE id = 1 AND description = 'Updated Description'");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(1, resultSet.getInt("cnt"), "Count should be 1 for updated description");
        resultSet.close();

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM dtp WHERE id = 1;");
    }

    @Test
    public void testDelDTP() throws RepositoryException, SQLException {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO dtp " +
                "VALUES (1, 'Test Description', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");

        // Act
        boolean isDeleted = repository.delDTP(1);

        // Assert
        assertTrue(isDeleted, "DTP should be deleted successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertFalse(resultSet.next());
        resultSet.close();

        // Clean up (in case it was not deleted)
        connectionManager.executeSQLQuery("DELETE FROM dtp WHERE id = 1;");
    }

    @Test
    public void testGetDTPByParams() throws RepositoryException {
        // Arrange
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (2, 'DTP Two', '2024-09-30 10:00:00', 56.0, 38.0, 'Side road', 'Accident', 1)");

        DTPParams params = new DTPParams();
        params.dtpIdBegin = 0;
        params.dtpIdEnd = 3;

        // Act
        List<DTP> dtpList = repository.getDTPByParams(params);

        // Assert
        assertEquals(2, dtpList.size(), "There should be 2 DTPs retrieved");

        // Clean up
        connectionManager.executeSQLQuery("DELETE FROM dtp WHERE id IN (1, 2);");
    }

    private void assertDTP(DTP d1, DTP d2) {
        assertEquals(d1.getId(), d2.getId());
        assertEquals(d1.getDescription(), d2.getDescription());
        assertEquals(d1.getDatetime(), d2.getDatetime());
        assertEquals(d1.getCoords(), d2.getCoords());
        assertEquals(d1.getDor(), d2.getDor());
        assertEquals(d1.getOsv(), d2.getOsv());
        assertEquals(d1.getCountTs(), d2.getCountTs());
    }
}

