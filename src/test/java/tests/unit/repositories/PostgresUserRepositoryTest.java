package tests.unit.repositories;

import exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresUserRepository;
import user.User;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostgresUserRepositoryTest {

    @Mock
    private PostgresConnectionManager connectionManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Statement statement;

    @InjectMocks
    private PostgresUserRepository userRepository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT MAX(id) FROM users")).thenReturn(resultSet);
    }

    @Test
    void testGetAllUsersId_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT id FROM users")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);

        List<Integer> result = userRepository.getAllUsersId();

        assertEquals(Arrays.asList(1, 2), result);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    void testGetAllUsersId_NullConnection() {
        when(connectionManager.getConnection()).thenReturn(null);

        assertThrows(RepositoryException.class, () -> userRepository.getAllUsersId());
    }

    @Test
    void testGetUserById_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE id=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("login")).thenReturn("testLogin");
        when(resultSet.getString("password")).thenReturn("testPass");
        when(resultSet.getString("role")).thenReturn("admin");

        User result = userRepository.getUser(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("testLogin", result.getLogin());
        assertEquals("testPass", result.getPassword());
        assertEquals("admin", result.getRole());
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE id=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User result = userRepository.getUser(1);

        assertNull(result);
    }

    @Test
    void testGetUserByLoginAndPassword_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE login=? AND password=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("login")).thenReturn("testLogin");
        when(resultSet.getString("password")).thenReturn("testPass");
        when(resultSet.getString("role")).thenReturn("admin");

        User result = userRepository.getUser("testLogin", "testPass");

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetUserByLoginAndPassword_NotFound() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE login=? AND password=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User result = userRepository.getUser("testLogin", "testPass");

        assertNull(result);
    }

    @Test
    void testGetRole_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE id=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("role")).thenReturn("admin");

        String role = userRepository.getRole(1);

        assertEquals("admin", role);
    }

    @Test
    void testGetRole_NotFound() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT * FROM users WHERE id=?")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String role = userRepository.getRole(1);

        assertNull(role);
    }

    @Test
    void testSaveUser_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT MAX(id) FROM users")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        when(connection.prepareStatement("INSERT INTO users (id, login, password, role) VALUES (?, ?, ?, ?)")).thenReturn(preparedStatement);

        User user = new User(1, "testLogin", "testPass", "admin");

        boolean result = userRepository.saveUser(user);

        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testSaveUser_Failure() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("INSERT INTO users (id, login, password, role) VALUES (?, ?, ?, ?)")).thenReturn(preparedStatement);
        doThrow(SQLException.class).when(preparedStatement).executeUpdate();

        User user = new User(1, "testLogin", "testPass", "admin");

        boolean result = userRepository.saveUser(user);

        assertFalse(result);
    }

    @Test
    void testDelUser_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM users WHERE id=?")).thenReturn(preparedStatement);

        boolean result = userRepository.delUser(1);

        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testDelUser_Failure() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("DELETE FROM users WHERE id=?")).thenReturn(preparedStatement);
        doThrow(SQLException.class).when(preparedStatement).executeUpdate();

        boolean result = userRepository.delUser(1);

        assertFalse(result);
    }

    @Test
    void testEditUser_Positive() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("UPDATE users SET login=?, password=?, role=? WHERE id=?")).thenReturn(preparedStatement);

        User user = new User(1, "testLogin", "testPass", "admin");

        boolean result = userRepository.editUser(user);

        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testEditUser_Failure() throws Exception {
        when(connectionManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("UPDATE users SET login=?, password=?, role=? WHERE id=?")).thenReturn(preparedStatement);
        doThrow(SQLException.class).when(preparedStatement).executeUpdate();

        User user = new User(1, "testLogin", "testPass", "admin");

        boolean result = userRepository.editUser(user);

        assertFalse(result);
    }
}
