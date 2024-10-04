package tests.integration.repositories;

import configurator.Configurator;
import entities.Participant;
import exceptions.RepositoryException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import params.ParticipantParams;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresParticipantRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostgresParticipantRepositoryITCase {

    private PostgresConnectionManager connectionManager;
    private PostgresParticipantRepository repository;

    @BeforeAll
    public void setUp() throws SQLException {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        String schemaName = System.getProperty("testSchema");
        connectionManager.setSearchPath(schemaName);

        repository = new PostgresParticipantRepository(connectionManager);
        // Очистка таблицы перед тестами
        connectionManager.executeSQLQuery("DELETE FROM participant;");
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'Test Description', '2024-09-29 10:00:00+03', 55.0, 37.0, 'Main road', 'Accident', 2, 3);");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
    }

    @AfterAll
    public void tearDown() {
        connectionManager.executeSQLQuery("DELETE FROM participant;");
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.closeConnection();
    }

    @Test
    public void testSaveParticipant() throws RepositoryException, SQLException {
        Participant participant = new Participant(5, 1, "Category1", "Healthy", "Male", true);
        boolean isSaved = repository.saveParticipant(participant);

        assertTrue(isSaved, "DTP should be saved successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 5;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals("Category1", resultSet.getString("category"));

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 5;");
    }

    @Test
    public void testGetParticipant() throws RepositoryException {
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");

        Participant retrievedParticipant = repository.getParticipant(1);

        assertNotNull(retrievedParticipant);
        assertEquals(retrievedParticipant.getCategory(), "Driver");

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 1;");
    }

    @Test
    public void testEditParticipant() throws RepositoryException, SQLException {
        Participant participant = new Participant(1, 1, "Driver", "Healthy", "Male", true);
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");
        participant.setCategory("Passenger");

        boolean isEdited = repository.editParticipant(participant);

        assertTrue(isEdited);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(1, resultSet.getInt("vehicle_id"));
        assertEquals("Passenger", resultSet.getString("category"));

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 1;");
    }

    @Test
    public void testDelParticipant() throws RepositoryException, SQLException {
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");

        boolean isDeleted = repository.delParticipant(1);

        assertTrue(isDeleted);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertFalse(resultSet.next());

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 1;");
    }

    @Test
    public void testGetParticByParams() throws RepositoryException {
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (2, 1, 'Passenger', 'No', true, 'Male', 'Healthy');");
        ParticipantParams params = new ParticipantParams();
        params.category = "Passenger";

        List<Participant> participants = repository.getParticByParams(params);

        assertEquals(1, participants.size());
        assertEquals(params.category, participants.get(0).getCategory());

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 1;");
        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 2;");
    }
}

