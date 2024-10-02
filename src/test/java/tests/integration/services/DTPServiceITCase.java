package tests.integration.services;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import configurator.Configurator;
import entities.Car;
import entities.DTP;
import entities.Participant;
import org.junit.jupiter.api.*;
import params.CarParams;
import params.DTPParams;
import params.ParticipantParams;
import repositories.postgres.*;
import services.DTPService;
import services.UserService;

import java.nio.file.AccessDeniedException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DTPServiceITCase {
    private DTPService dtpService;
    private IDTPRepository dtpRepository;
    private ICarRepository carRepository;
    private IParticipantRepository participantRepository;
    private UserService userService;
    private static PostgresConnectionManager connectionManager;

    @BeforeAll
    public void setUp() {
        String host = Configurator.getValue("db.url");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        connectionManager = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
        dtpRepository = new PostgresDTPRepository(connectionManager);
        carRepository = new PostgresCarRepository(connectionManager);
        participantRepository = new PostgresParticipantRepository(connectionManager);
        userService = new UserService(new PostgresUserRepository(connectionManager));
        dtpService = new DTPService(dtpRepository, carRepository, participantRepository, userService);
    }

    @AfterEach
    public void tearDown() {
        // Удаление тестовых данных после каждого теста
        connectionManager.executeSQLQuery("DELETE FROM participant;");
        connectionManager.executeSQLQuery("DELETE FROM vehicle;");
        connectionManager.executeSQLQuery("DELETE FROM dtp;");
        connectionManager.executeSQLQuery("DELETE FROM users;");
    }

    @AfterAll
    public static void tearDownAll() {
        // Закрытие соединения после всех тестов
        connectionManager.closeConnection();
    }

    @Test
    public void testGetDTPsByParams_Admin() throws Exception {
        // Добавление тестового пользователя с правами администратора
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (2, 'DTP Two', '2024-09-30 10:00:00', 56.0, 38.0, 'Side road', 'Accident', 1)");

        DTP d1 = new DTP(1, "DTP One", "2024-09-29 10:00:00", 55, 37, "Main road", "Accident", 2, 2);
        DTP d2 = new DTP(2, "DTP Two", "2024-09-30 10:00:00", 56, 38, "Side road", "Accident", 1, 2);


        DTPParams params = new DTPParams(); // Укажите параметры, если необходимо
        List<DTP> actualDTPs = dtpService.getDTPsByParams(1, params); // 1 - администратор

        DTP d3 = actualDTPs.get(0);
        DTP d4 = actualDTPs.get(1);

        assertEquals(d1.getDescription(), d3.getDescription());
        assertEquals(d1.getCoords(), d3.getCoords());
        assertEquals(d1.getDor(), d3.getDor());
        assertEquals(d1.getOsv(), d3.getOsv());
        assertEquals(d1.getDatetime(), d3.getDatetime());

        assertEquals(d2.getDescription(), d4.getDescription());
        assertEquals(d2.getCoords(), d4.getCoords());
        assertEquals(d2.getDor(), d4.getDor());
        assertEquals(d2.getOsv(), d4.getOsv());
        assertEquals(d2.getDatetime(), d4.getDatetime());
    }

    @Test
    public void testGetCarsByParams_Admin() throws Exception {
        // Добавление тестового пользователя с правами администратора
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Blue');");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (2, 1, 'Honda', 'Accord', 'r', 'Sedan', 2020, 'Black');");
        Car d1 = new Car(1, 1, "Toyota", "Camry", 2019, "Blue", "Sedan");
        Car d2 = new Car(2, 1, "Honda", "Accord", 2020, "Black", "Sedan");
        CarParams params = new CarParams(); // Укажите параметры, если необходимо

        List<Car> actualCars = dtpService.getCarsByParams(1, params); // 1 - администратор

        Car d3 = actualCars.get(0);
        Car d4 = actualCars.get(1);
        assertEquals(d1.getColor(), d3.getColor());
        assertEquals(d1.getCarYear(), d3.getCarYear());
        assertEquals(d1.getDtpId(), d3.getDtpId());
        assertEquals(d1.getMarka(), d3.getMarka());
        assertEquals(d1.getModel(), d3.getModel());

        assertEquals(d2.getColor(), d4.getColor());
        assertEquals(d2.getCarYear(), d4.getCarYear());
        assertEquals(d2.getDtpId(), d4.getDtpId());
        assertEquals(d2.getMarka(), d4.getMarka());
        assertEquals(d2.getModel(), d4.getModel());
    }

    @Test
    public void testGetParticipantByParams_Admin() throws Exception {
        // Добавление тестового пользователя с правами администратора
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Blue');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (2, 1, 'Passenger', 'No', true, 'Female', 'Healthy');");
        ParticipantParams params = new ParticipantParams(); // Укажите параметры, если необходимо
        Participant d1 = new Participant(1, 1, "Driver", "Healthy", "Male", true);
        Participant d2 = new Participant(2, 1, "Passenger", "Healthy", "Female", true);

        List<Participant> actualParticipants = dtpService.getParticsByParams(1, params); // 1 - администратор

        Participant d3 = actualParticipants.get(0);
        Participant d4 = actualParticipants.get(1);

        assertEquals(d1.getHealth(), d3.getHealth());
        assertEquals(d1.getId(), d3.getId());
        assertEquals(d1.getCategory(), d3.getCategory());
        assertEquals(d1.getPol(), d3.getPol());
        assertEquals(d1.getSafetyBelt(), d3.getSafetyBelt());

        assertEquals(d2.getHealth(), d4.getHealth());
        assertEquals(d2.getId(), d4.getId());
        assertEquals(d2.getCategory(), d4.getCategory());
        assertEquals(d2.getPol(), d4.getPol());
        assertEquals(d2.getSafetyBelt(), d4.getSafetyBelt());
    }

    @Test
    public void testAddDTP_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");

        DTP newDTP = new DTP(1, "DTP One", "2024-09-29 10:00:00", 55, 37, "Main road", "Accident", 2, 2);
        boolean result = dtpService.addDTP(1, newDTP); // 1 - администратор

        assertTrue(result, "DTP should be added successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(resultSet.getString("description"), "DTP One", "The count should be 1");
    }

    @Test
    public void testAddDTP_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'normal_user', 'user_pass', 'Guest');");
        DTP newDTP = new DTP(1, "DTP One", "2024-09-29 10:00:00", 55, 37, "Main road", "Accident", 2, 2);

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.addDTP(2, newDTP); // 2 - обычный пользователь
        });
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertFalse(resultSet.next(), "Result set should be empty");
    }

    @Test
    public void testAddCar_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        Car newCar = new Car(1, 1, "Toyota", "Camry", 2020, "Blue", "Sedan");

        boolean result = dtpService.addCar(1, newCar); // 1 - администратор

        assertTrue(result, "DTP should be added successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(resultSet.getString("marka_ts"), "Toyota");
    }

    @Test
    public void testAddCar_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        Car newCar = new Car(1, 1, "Toyota", "Camry", 2020, "Blue", "Sedan");

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.addCar(2, newCar); // 2 - обычный пользователь
        });
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertFalse(resultSet.next(), "Result set should be empty");
    }

    @Test
    public void testAddParticipant_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        Participant newPar = new Participant(1, 1, "Category1", "Healthy", "Male", true);

        boolean result = dtpService.addParticipant(1, newPar); // 1 - администратор

        assertTrue(result, "DTP should be added successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(resultSet.getString("health"), "Healthy");
    }

    @Test
    public void testAddParticipant_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        Participant newPar = new Participant(1, 1, "Category1", "Healthy", "Male", true);

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.addParticipant(2, newPar); // 2 - обычный пользователь
        });
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertFalse(resultSet.next(), "Result set should be empty");
    }

    @Test
    public void testGetDTP_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");

        DTP actualDTP = dtpService.getDTP(1, 1); // 1 - администратор

        assertNotNull(actualDTP);
        assertEquals(actualDTP.getDor(), "Main road");
    }

    @Test
    public void testGetDTP_NoAdmin() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.getDTP(1, 1);
        });
    }

    @Test
    public void testGetCar_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");

        Car actualCar = dtpService.getCar(1, 1); // 1 - администратор

        assertNotNull(actualCar);
        assertEquals("Toyota", actualCar.getMarka());
    }

    @Test
    public void testGetCar_NonAdmin() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");


        assertThrows(AccessDeniedException.class, () -> {
            dtpService.getCar(1, 1);
        });
    }

    @Test
    public void testGetParticipant_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");

        Participant actualPar = dtpService.getParticipant(1, 1); // 1 - администратор

        assertNotNull(actualPar);
        assertEquals(actualPar.getHealth(), "Healthy");
    }

    @Test
    public void testGetParticipant_NoAdmin() {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.getParticipant(1, 1);
        });
    }

    @Test
    public void testDeleteDTP_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");

        boolean result = dtpService.deleteDTP(1, 1); // 1 - администратор

        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertFalse(resultSet.next());
    }

    @Test
    public void testDeleteDTP_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");


        assertThrows(AccessDeniedException.class, () -> {
            dtpService.deleteDTP(2, 1); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("description"), "DTP One");
    }

    @Test
    public void testDeleteCar_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");


        boolean result = dtpService.deleteCar(1, 1); // 1 - администратор

        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertFalse(resultSet.next());
    }

    @Test
    public void testDeleteCar_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");


        assertThrows(AccessDeniedException.class, () -> {
            dtpService.deleteCar(2, 1); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("marka_ts"), "Toyota");
    }

    @Test
    public void testDeleteParticipant_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");


        boolean result = dtpService.deleteParticipant(1, 1); // 1 - администратор

        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertFalse(resultSet.next());
    }

    @Test
    public void testDeleteParticipant_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (2, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");


        assertThrows(AccessDeniedException.class, () -> {
            dtpService.deleteParticipant(2, 1); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("category"), "Driver");
    }

    @Test
    public void testEditDTP_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        DTP newDTP = new DTP(1, "DTP Edited", "2024-09-29 10:00:00", 55, 37, "Main road", "Accident", 2, 2);

        boolean result = dtpService.editDTP(1, newDTP); // 1 - администратор

        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("description"), "DTP Edited");
    }

    @Test
    public void testEditDTP_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES(2, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        DTP newDTP = new DTP(1, "DTP Edited", "2024-09-29 10:00:00", 55, 37, "Main road", "Accident", 2, 2);

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.editDTP(2, newDTP); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM dtp WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("description"), "DTP One");
    }

    @Test
    public void testEditCar_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        Car newCar = new Car(1, 1, "Honda", "Camry", 2020, "Blue", "Sedan");

        boolean result = dtpService.editCar(1, newCar); // 1 - администратор

        assertTrue(result, "DTP should be added successfully");
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(resultSet.getString("marka_ts"), "Honda");
    }

    @Test
    public void testEditCar_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        Car newCar = new Car(1, 1, "Honda", "Camry", 2020, "Blue", "Sedan");

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.editCar(1, newCar); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM vehicle WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("marka_ts"), "Toyota");
    }

    @Test
    public void testEditParticipant_Admin() throws Exception {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'admin_user', 'admin_pass', 'Analyst');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");
        Participant newPar = new Participant(1, 1, "Category1", "Healthy", "Male", true);

        boolean result = dtpService.editParticipant(1, newPar); // 1 - администратор

        assertTrue(result);
        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertTrue(resultSet.next(), "Result set should not be empty");
        assertEquals(resultSet.getString("category"), "Category1");
    }

    @Test
    public void testEditParticipant_NonAdmin() throws SQLException {
        connectionManager.executeSQLQuery("INSERT INTO users VALUES (1, 'usr', 'pswrd', 'Guest');");
        connectionManager.executeSQLQuery("INSERT INTO dtp VALUES (1, 'DTP One', '2024-09-29 10:00:00', 55.0, 37.0, 'Main road', 'Accident', 2)");
        connectionManager.executeSQLQuery("INSERT INTO vehicle VALUES (1, 1, 'Toyota', 'Camry', 'r', 'Sedan', 2019, 'Black');");
        connectionManager.executeSQLQuery("INSERT INTO participant VALUES (1, 1, 'Driver', 'No', true, 'Male', 'Healthy');");
        Participant newPar = new Participant(1, 1, "Category1", "Healthy", "Male", true);

        assertThrows(AccessDeniedException.class, () -> {
            dtpService.addParticipant(2, newPar); // 2 - обычный пользователь
        });

        ResultSet resultSet = connectionManager.executeSQLQuery("SELECT * FROM participant WHERE id = 1;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getString("category"), "Driver");
    }

    // TODO доделать методы, добавить ассерты,
}
