package tests.integration.services;

import IRepositories.ICarRepository;
import IRepositories.IParticipantRepository;
import IRepositories.IUserRepository;
import configurator.Configurator;
import exceptions.RepositoryException;
import org.junit.jupiter.api.*;
import params.Params;
import report.ReportGenerator;
import repositories.postgres.*;
import services.DTPService;
import services.ReportService;
import services.UserService;

import java.io.File;
import java.nio.file.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportServiceITCase {
    private DTPService dtpService; // Реальный DTPService
    private ReportGenerator reportGenerator; // Реальный ReportGenerator
    private ReportService reportService;
    private static PostgresConnectionManager connectionManager;
    String filename = "integration_report_test_file";
    String format = "json";

    @BeforeAll
    public void setUp() {
        // Используем реальные объекты зависимостей
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        IRepositories.IDTPRepository dtpRep = new PostgresDTPRepository(connectionManager);
        ICarRepository carRep = new PostgresCarRepository(connectionManager);
        IParticipantRepository prtRep = new PostgresParticipantRepository(connectionManager);
        IUserRepository usrRep = new PostgresUserRepository(connectionManager);
        UserService usrService = new UserService(usrRep);
        dtpService = new DTPService(dtpRep, carRep, prtRep, usrService);
        ReportGenerator repGen = new ReportGenerator();
        reportService = new ReportService(dtpService, repGen);
        reportGenerator = new ReportGenerator(); // Настрой ReportGenerator для генерации отчетов

        reportService = new ReportService(dtpService, reportGenerator);
    }

    @AfterEach
    public void tearDown() {
        // Удаление тестовых данных после каждого теста
        connectionManager.executeSQLQuery("DELETE FROM participant;");
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.executeSQLQuery("DELETE FROM users;");
        File reportFile = new File(filename + "." + format);
        if (reportFile.exists()) {
            reportFile.delete();
        }
    }

    @AfterAll
    public static void tearDownAll() {
        // Закрытие соединения после всех тестов
        connectionManager.closeConnection();
    }

    @Test
    public void testSave_GeneratesReport_Successfully() throws AccessDeniedException, RepositoryException {
        // Создаем данные для теста
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (2, 'DTP Two', '2024-09-30 10:00:00', 56.0, 38.0, 'Side road', 'Accident', 1)");
        int userId = 1; // ID пользователя с правами
        Params params = new Params();

        // Выполняем тестируемый метод
        boolean result = reportService.save(userId, filename, format, params);

        // Проверяем результат
        assertTrue(result);

        // Можно также добавить проверку того, что отчет действительно был сохранен
        // Например, проверка, что файл был создан:
        File reportFile = new File(filename + "." + format);
        assertTrue(reportFile.exists());
    }

    @Test
    public void testSave_ThrowsException_WhenAccessDenied() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (2, 'DTP Two', '2024-09-30 10:00:00', 56.0, 38.0, 'Side road', 'Accident', 1)");
        int userId = 1; // Обычный пользователь без прав
        Params params = new Params();

        // Выполняем тест и проверяем выбрасывание исключения
        assertThrows(AccessDeniedException.class, () -> reportService.save(userId, filename, format, params));

        // Проверяем, что файл не был создан
        File reportFile = new File(filename);
        assertFalse(reportFile.exists());
    }

    @Test
    public void testGet_GeneratesReport_Successfully() throws AccessDeniedException, RepositoryException {
        // Создаем данные для теста
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (2, 'DTP Two', '2024-09-30 10:00:00', 56.0, 38.0, 'Side road', 'Accident', 1)");
        int userId = 1; // ID пользователя с правами
        Params params = new Params();

        // Выполняем тестируемый метод
        byte[] result = reportService.get(userId, format, params);

        // Проверяем результат
        assertNotNull(result);
        assertTrue(result.length > 0, "Отчет не должен быть пустым");
    }
}
