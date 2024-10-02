package tests.unit.repositories;

import entities.Participant;
import exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import params.ParticipantParams;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresParticipantRepository;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostgresParticipantRepositoryTest {
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
    private PostgresParticipantRepository repository;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getConnection()).thenReturn(connection);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT MAX(id) FROM participant")).thenReturn(resultSet);
    }

    @Test
    public void testGetParticipantPositive() throws Exception {
        // Arrange
        int participantId = 123;
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(participantId);
        when(resultSet.getInt("vehicle_id")).thenReturn(1);
        when(resultSet.getString("category")).thenReturn("A");
        when(resultSet.getString("health")).thenReturn("Healthy");
        when(resultSet.getString("pol")).thenReturn("M");
        when(resultSet.getBoolean("safety_belt")).thenReturn(true);

        // Act
        Participant participant = repository.getParticipant(participantId);

        // Assert
        assertNotNull(participant);
        assertEquals(participantId, participant.getId());
        verify(preparedStatement, times(1)).setInt(1, participantId);
    }

    @Test
    public void testGetParticipantNegativeNotFound() throws Exception {
        // Arrange
        int participantId = 123;
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        Participant participant = repository.getParticipant(participantId);

        // Assert
        assertNull(participant);
    }

    @Test
    public void testSaveParticipantPositive() throws Exception {
        // Arrange
        Participant participant = new Participant(-1, 1, "A", "Healthy", "M", true);
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(123);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act
        boolean result = repository.saveParticipant(participant);

        // Assert
        assertTrue(result);
        assertEquals(124, participant.getId());
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testSaveParticipantNegativeSQLException() throws Exception {
        // Arrange
        Participant participant = new Participant(-1, 1, "A", "Healthy", "M", true);
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.saveParticipant(participant);
        });
    }

    @Test
    public void testDelParticipantPositive() throws Exception {
        // Arrange
        int participantId = 123;
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act
        boolean result = repository.delParticipant(participantId);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).setInt(1, participantId);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDelParticipantNegativeConnectionNull() {
        // Arrange
        when(connectionManager.getConnection()).thenReturn(null);

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.delParticipant(123);
        });
    }

    @Test
    public void testEditParticipantPositive() throws Exception {
        // Arrange
        Participant participant = new Participant(123, 1, "A", "Healthy", "M", true);
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act
        boolean result = repository.editParticipant(participant);

        // Assert
        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testEditParticipantNegativeSQLException() throws SQLException {
        // Arrange
        Participant participant = new Participant(123, 1, "A", "Healthy", "M", true);
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.editParticipant(participant);
        });
    }

    @Test
    public void testGetParticByParamsPositive() throws Exception {
        // Arrange
        ParticipantParams params = new ParticipantParams();
        params.category = "A";
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(123);
        when(resultSet.getInt("vehicle_id")).thenReturn(1);
        when(resultSet.getString("category")).thenReturn("A");

        // Act
        List<Participant> result = repository.getParticByParams(params);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getCategory());
    }

    @Test
    public void testGetParticByParamsNegativeSQLException() throws Exception {
        // Arrange
        ParticipantParams params = new ParticipantParams();
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        assertThrows(RepositoryException.class, () -> {
            repository.getParticByParams(params);
        });
    }
}
