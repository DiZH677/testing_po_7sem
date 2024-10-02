package tests.integration.repositories;

import configurator.Configurator;
import exceptions.RepositoryException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import repositories.postgres.PostgresConnectionManager;
import repositories.postgres.PostgresUserRepository;
import user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostgresUserRepositoryITCase {

    private PostgresConnectionManager connectionManager;
    private PostgresUserRepository repository;

    @BeforeAll
    public void setUp() throws SQLException {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        repository = new PostgresUserRepository(connectionManager);
        // Очистка таблицы перед тестами
        connectionManager.executeSQLQuery("DELETE FROM users;");
    }

    @AfterAll
    public void tearDown() {
        connectionManager.executeSQLQuery("DELETE FROM users;");
        connectionManager.closeConnection();
    }

    @Test
    public void testSaveUser() throws RepositoryException {
        User user = new User(1, "new_user", "password456", "user");
        boolean isSaved = repository.saveUser(user);
        assertTrue(isSaved);

        // Проверка сохранения
        User retrievedUser = repository.getUser(user.getId());
        assertNotNull(retrievedUser);
        assertEquals(user.getLogin(), retrievedUser.getLogin());

        connectionManager.executeSQLQuery("DELETE FROM users WHERE id = 1;");
    }

    @Test
    public void testGetUserById() throws RepositoryException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'test_user', 'password123', 'admin');");

        User retrievedUser = repository.getUser(1);

        assertNotNull(retrievedUser);
        assertEquals("test_user", retrievedUser.getLogin());

        connectionManager.executeSQLQuery("DELETE FROM users WHERE id = 1;");
    }

    @Test
    public void testGetUserByLoginAndPassword() throws RepositoryException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'test_user', 'password123', 'admin');");

        User retrievedUser = repository.getUser("test_user", "password123");

        assertNotNull(retrievedUser);
        assertEquals("admin", retrievedUser.getRole());

        connectionManager.executeSQLQuery("DELETE FROM users WHERE id = 1;");
    }

    @Test
    public void testEditUser() throws RepositoryException, SQLException {
        // Вставляем пользователя через SQL-запрос
        User user = new User(1, "new_user", "new_password", "user");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'new_user', 'new_password', 'user');");
        user.setRole("admin");

        // Выполняем редактирование пользователя через репозиторий
        boolean isEdited = repository.editUser(user);

        assertTrue(isEdited);

        // Проверяем, что изменения применены
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM users WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals("new_user", resultSet.getString("login"));
        assertEquals("new_password", resultSet.getString("password"));
        assertEquals("admin", resultSet.getString("role"));

        // Удаляем пользователя после теста
        connectionManager.executeSQLQuery("DELETE FROM users WHERE id = 1;");
    }


    @Test
    public void testDeleteUser() throws RepositoryException, SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'test_user', 'password123', 'admin');");

        boolean isDeleted = repository.delUser(1);
        assertTrue(isDeleted);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertFalse(resultSet.next());

        connectionManager.executeSQLQuery("DELETE FROM participant WHERE id = 1;");
    }

    @Test
    public void testGetAllUsersId() throws RepositoryException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'test_user', 'password123', 'admin');");
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'test_user2', 'password1234', 'user');");

        List<Integer> userIds = repository.getAllUsersId();
        assertNotNull(userIds);
        assertEquals(userIds.size(), 2);
        assertEquals(userIds.get(0), 1);
        assertEquals(userIds.get(1), 2);

        connectionManager.executeSQLQuery("DELETE FROM users WHERE id IN (1, 2);");
    }
}
