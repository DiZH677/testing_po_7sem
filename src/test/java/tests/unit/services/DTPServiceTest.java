package tests.unit.services;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import entities.Car;
import entities.DCP;
import entities.DTP;
import entities.Participant;
import exceptions.RepositoryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import params.CarParams;
import params.DTPParams;
import params.Params;
import params.ParticipantParams;
import permission.Permission;
import services.DTPService;
import services.UserService;
import user.User;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DTPServiceTest {

    @InjectMocks
    private DTPService dtpService;

    @Mock
    private IDTPRepository mockDTPRepository;

    @Mock
    private ICarRepository mockCarRepository;

    @Mock
    private IParticipantRepository mockParticipantRepository;

    @Mock
    private UserService mockUsrServiceDTP;

    DTP dtp = new DTP(1, "Description1", "2022-01-01", 55.7558, 37.6176, "Dor1", "Osv1", 2, 3);
    Car cr = new Car(1, 1, "BMW", "BMW M5", 2008, "Black", "Прочие легковые");
    Participant pr = new Participant(0, 1, "Водитель", "Здоров", "Мужской", false);
    User usr = new User(0, "lgn", "password", "Analyst");


    @Test
    public void testGetDTPsByParams_Success() throws Exception {
        // Arrange
        int userId = 1;
        DTPParams params = new DTPParams();
        List<DTP> expectedDTPs = new ArrayList<>();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_DTP_BY_PARAMS)).thenReturn(true);
        when(mockDTPRepository.getDTPByParams(params)).thenReturn(expectedDTPs);

        // Act
        List<DTP> actualDTPs = dtpService.getDTPsByParams(userId, params);

        // Assert
        assertNotNull(actualDTPs);
        assertEquals(expectedDTPs, actualDTPs);
    }

    @Test
    public void testGetDTPsByParams_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        DTPParams params = new DTPParams();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_DTP_BY_PARAMS)).thenReturn(false);


        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getDTPsByParams(userId, params);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetCarsByParams_Success() throws Exception {
        // Arrange
        int userId = 1;
        CarParams params = new CarParams();
        List<Car> expectedCars = new ArrayList<>();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_CAR_BY_PARAMS)).thenReturn(true);
        when(mockCarRepository.getCarsByParams(params)).thenReturn(expectedCars);

        // Act
        List<Car> actualCars = dtpService.getCarsByParams(userId, params);

        // Assert
        assertNotNull(actualCars);
        assertEquals(expectedCars, actualCars);
    }

    @Test
    public void testGetCarsByParams_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        CarParams params = new CarParams();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_CAR_BY_PARAMS)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getCarsByParams(userId, params);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetParticsByParams_Success() throws Exception {
        // Arrange
        int userId = 1;
        ParticipantParams params = new ParticipantParams();
        List<Participant> expectedPartics = new ArrayList<>();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_PARTICIPANT_BY_PARAMS)).thenReturn(true);
        when(mockParticipantRepository.getParticByParams(params)).thenReturn(expectedPartics);

        // Act
        List<Participant> actualPartics = dtpService.getParticsByParams(userId, params);

        // Assert
        assertNotNull(actualPartics);
        assertEquals(expectedPartics, actualPartics);
    }

    @Test
    public void testGetParticsByParams_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        ParticipantParams params = new ParticipantParams();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_PARTICIPANT_BY_PARAMS)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getParticsByParams(userId, params);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetDTP_Success() throws Exception {
        // Arrange
        int userId = 1;
        int dtpId = 2;
        DTP expectedDTP = new DTP();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_DTP)).thenReturn(true);
        when(mockDTPRepository.getDTP(dtpId)).thenReturn(expectedDTP);

        // Act
        DTP actualDTP = dtpService.getDTP(userId, dtpId);

        // Assert
        assertNotNull(actualDTP);
        assertEquals(expectedDTP, actualDTP);
    }

    @Test
    public void testGetDTP_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        int dtpId = 2;
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_DTP)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getDTP(userId, dtpId);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetCar_Success() throws Exception {
        // Arrange
        int userId = 1;
        int carId = 2;
        Car expectedCar = new Car(1, 1, "BMW", "BMW M5", 2008, "Black", "Прочие легковые");
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_CAR)).thenReturn(true);
        when(mockCarRepository.getCar(carId)).thenReturn(expectedCar);

        // Act
        Car actualCar = dtpService.getCar(userId, carId);

        // Assert
        assertNotNull(actualCar);
        assertEquals(expectedCar, actualCar);
    }

    @Test
    public void testGetCar_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        int carId = 2;
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_CAR)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getCar(userId, carId);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetParticipant_Success() throws Exception {
        // Arrange
        int userId = 1;
        int participantId = 2;
        Participant expectedParticipant = new Participant(0, 1, "Водитель", "Здоров", "Мужской", false);
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_PARTICIPANT)).thenReturn(true);
        when(mockParticipantRepository.getParticipant(participantId)).thenReturn(expectedParticipant);

        // Act
        Participant actualParticipant = dtpService.getParticipant(userId, participantId);

        // Assert
        assertNotNull(actualParticipant);
        assertEquals(expectedParticipant, actualParticipant);
    }

    @Test
    public void testGetParticipant_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        int participantId = 2;
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_PARTICIPANT)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getParticipant(userId, participantId);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testGetAllByParams_Success() throws Exception {
        // Arrange
        int userId = 1;
        Params params = new Params();
        List<DTP> expectedDTPs = new ArrayList<>();
        List<Car> expectedCars = new ArrayList<>();
        List<Participant> expectedParticipants = new ArrayList<>();
        DCP expectedDCP = new DCP(expectedDTPs, expectedCars, expectedParticipants);
        lenient().when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_ALL_BY_PARAMS)).thenReturn(true);
        lenient().when(mockDTPRepository.getDTPByParams(params.dtpps)).thenReturn(expectedDTPs);
        lenient().when(mockCarRepository.getCarsByParams(params.carps)).thenReturn(expectedCars);
        lenient().when(mockParticipantRepository.getParticByParams(params.pcps)).thenReturn(expectedParticipants);

        // Act
        DCP actualDCP = dtpService.getAllByParams(userId, params);

        // Assert
        assertNotNull(actualDCP);
        assertEquals(expectedDCP.getDTPs(), actualDCP.getDTPs());
        assertEquals(expectedDCP.getCars(), actualDCP.getCars());
        assertEquals(expectedDCP.getParticipants(), actualDCP.getParticipants());
    }

    @Test
    public void testGetAllByParams_NoPermission() throws Exception {
        // Arrange
        int userId = 1;
        Params params = new Params();
        when(mockUsrServiceDTP.hasPermission(userId, Permission.GET_ALL_BY_PARAMS)).thenReturn(false);

        // Act & Assert
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> {
            dtpService.getAllByParams(userId, params);
        });
        assertEquals("You do not have main.java.permission to perform this operation.", thrown.getMessage());
    }

    @Test
    public void testAddDTP() throws RepositoryException {
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.ADD_DTP)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockDTPRepository.saveDTP(dtp)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.addDTP(usr.id, dtp);
        } catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testAddDTP_Failure() {
        DTP newDTP = dtp;

        assertThrows(AccessDeniedException.class, () -> dtpService.addDTP(-1, newDTP));
    }

    @Test
    public void testDelDTP() throws RepositoryException {
        int del_id = 0;

        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.DELETE_DTP)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockDTPRepository.delDTP(del_id)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.deleteDTP(usr.id, del_id);
        } catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testDelDTP_Failure() {
        assertThrows(AccessDeniedException.class, () -> dtpService.deleteDTP(-1, 1));
    }

    @Test
    public void testEditDTP() throws RepositoryException {
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.EDIT_DTP)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockDTPRepository.editDTP(dtp)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.editDTP(usr.id, dtp);
        } catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testEditDTP_Failure() {
        DTP newDTP = dtp;
        assertThrows(AccessDeniedException.class, () -> dtpService.editDTP(-1, newDTP));
    }

    @Test
    public void testAddCar() throws RepositoryException {
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.ADD_CAR)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockCarRepository.saveCar(cr)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.addCar(usr.id, cr);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testAddCar_Failure() {
        Car newCar = cr;

        assertThrows(AccessDeniedException.class, () -> dtpService.addCar(-1, newCar));
    }

    @Test
    public void testDelCar() throws RepositoryException {
        int del_id = 0;

        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.DELETE_CAR)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockCarRepository.delCar(del_id)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.deleteCar(usr.id, del_id);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testDelCar_Failure() {
        assertThrows(AccessDeniedException.class, () -> dtpService.deleteCar(-1, 1));
    }

    @Test
    public void testEditCar() throws RepositoryException {
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.EDIT_CAR)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockCarRepository.editCar(cr)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.editCar(usr.id, cr);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testEditCar_Failure() {
        Car newCar = cr;

        assertThrows(AccessDeniedException.class, () -> dtpService.editCar(-1, newCar));
    }

    @Test
    public void testAddParticipant() throws RepositoryException {
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.ADD_PARTICIPANT)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockParticipantRepository.saveParticipant(pr)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.addParticipant(usr.id, pr);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testAddParticipant_Failure() {
        Participant newParticipant = pr;

        assertThrows(AccessDeniedException.class, () -> dtpService.addParticipant(-1, newParticipant));
    }

    @Test
    public void testDelParticipant() throws RepositoryException {
        int dtp_id = 1;

        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.DELETE_PARTICIPANT)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockParticipantRepository.delParticipant(dtp_id)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.deleteParticipant(usr.id, dtp_id);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testDelParticipant_Failure() {
        assertThrows(AccessDeniedException.class, () -> dtpService.deleteParticipant(-1, 1));
    }

    @Test
    public void testEditParticipant() throws RepositoryException {;
        // Формирование источника запроса
        Mockito.when(mockUsrServiceDTP.hasPermission(usr.id, Permission.EDIT_PARTICIPANT)).thenReturn(true);
        // Формирование запроса
        Mockito.when(mockParticipantRepository.editParticipant(pr)).thenReturn(true);

        boolean answer = false;
        try {
            answer = dtpService.editParticipant(usr.id, pr);
        }
        catch (AccessDeniedException ignored) {};
        assertTrue(answer);
    }

    @Test
    public void testEditParticipant_Failure() {
        Participant newParticipant = pr;

        assertThrows(AccessDeniedException.class, () -> dtpService.editParticipant(-1, newParticipant));
    }
}

