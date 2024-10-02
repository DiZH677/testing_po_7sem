package tests.integration.services;

import IRepositories.IUserRepository;
import configurator.Configurator;
import exceptions.RepositoryException;
import org.junit.jupiter.api.*;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresUserRepository;
import services.UserService;
import user.User;

import java.nio.file.AccessDeniedException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceITCase {
    private UserService userService;
    private IUserRepository userRepository;
    private static PostgresConnectionManager connectionManager;

    @BeforeAll
    public void setUp() {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        // Инициализация зависимостей
        userRepository = new PostgresUserRepository(connectionManager);
        userService = new UserService(userRepository);
    }

    // Удаление тестовых данных после тестов (по желанию)
    @AfterEach
    public void tearDown() {
        // Удаление тестовых данных после каждого теста
        connectionManager.executeSQLQuery("DELETE FROM users;");
    }

    @AfterAll
    public static void tearDownAll() {
        // Удаление всех данных и закрытие соединения после всех тестов
        connectionManager.executeSQLQuery("DELETE FROM users;");
        connectionManager.closeConnection();
    }

    @Test
    public void testGetAllUsersId_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (3, 'guest_user', 'guest_pass', 'Guest');");
        List<Integer> expectedUserIds = Arrays.asList(1, 2, 3);

        List<Integer> actualUserIds = userService.getAllUsersId(1); // 1 - admin_user

        assertEquals(expectedUserIds, actualUserIds);
    }

    @Test
    public void testGetAllUsersId_NonAdmin() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (3, 'guest_user', 'guest_pass', 'Guest');");

        assertThrows(AccessDeniedException.class, () -> userService.getAllUsersId(2)); // 2 - normal_user
    }

    @Test
    public void testGetUserLP_ValidCredentials() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");

        User actualUser = userService.getUserLP("normal_user", "user_pass");
        assertUser(new User(2, "normal_user", "user_pass", "User"), actualUser);
    }

    @Test
    public void testGetUserLP_InvalidCredentials() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");

        try {
            assertNull(userService.getUserLP("admin_user_2", "wrong_pass_2"));
        } catch (RepositoryException e) {
            fail("Error in repository " + e.getMessage());
        }
    }

    @Test
    public void testGetUserById_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");

        User actualUser = userService.getUserById(1, 2); // 1 - admin_user
        assertUser(new User(2, "normal_user", "user_pass", "User"), actualUser);
    }

    @Test
    public void testGetUserById_NonAdmin() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(2, 1)); // 2 - normal_user
    }

    @Test
    public void testAddUser_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        User newUser = new User(2, "new_user", "new_pass", "User");

        assertTrue(userService.addUser(1, newUser)); // 1 - admin_user

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 2;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        User addedUsr = new User(resultSet.getInt("id"), resultSet.getString("login"),
                resultSet.getString("password"), resultSet.getString("role"));
        assertUser(newUser, addedUsr);
    }

    @Test
    public void testAddUser_NonAdmin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");
        User newUser = new User(3, "new_user", "new_pass", "User");

        assertThrows(AccessDeniedException.class, () -> userService.addUser(2, newUser)); // 2 - normal_user
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 3;");
        assertFalse(resultSet.next(), "Result set should be empty");
    }

    @Test
    public void testDelUser_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");

        assertTrue(userService.delUser(1, 2)); // 1 - admin_user, удаляем normal_user
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 2;");
        assertFalse(resultSet.next(), "Result set should be empty");
    }

    @Test
    public void testDelUser_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");

        assertThrows(AccessDeniedException.class, () -> userService.delUser(2, 1)); // 2 - normal_user

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 2;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        User editedUsr = new User(resultSet.getInt("id"), resultSet.getString("login"),
                resultSet.getString("password"), resultSet.getString("role"));
        assertUser(new User(2, "normal_user", "user_pass", "User"), editedUsr);
    }

    @Test
    public void testEditUser_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");
        User updatedUser = new User(2, "updated_user", "new_pass", "User");

        assertTrue(userService.editUser(1, updatedUser)); // 1 - admin_user

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 2;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        User editedUsr = new User(resultSet.getInt("id"), resultSet.getString("login"),
                resultSet.getString("password"), resultSet.getString("role"));
        assertUser(updatedUser, editedUsr);
    }

    @Test
    public void testEditUser_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'User');");
        User updatedUser = new User(2, "updated_user", "new_pass", "User");

        assertThrows(AccessDeniedException.class, () -> userService.editUser(2, updatedUser)); // 2 - normal_user

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 2;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        User editedUsr = new User(resultSet.getInt("id"), resultSet.getString("login"),
                resultSet.getString("password"), resultSet.getString("role"));
        assertUser(new User(2, "normal_user", "user_pass", "User"), editedUsr);
    }

    private void assertUser(User u1, User u2) {
        assertEquals(u1.getId(), u2.getId());
        assertEquals(u1.getLogin(), u2.getLogin());
        assertEquals(u1.getPassword(), u2.getPassword());
        assertEquals(u1.getRole(), u2.getRole());
    }
}

