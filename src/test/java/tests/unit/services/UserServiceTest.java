package tests.unit.services;

import IRepositories.IUserRepository;
import exceptions.RepositoryException;
import permission.Permission;
import services.UserService;
import user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    @InjectMocks
    private UserService usrService;
    @Mock
    private IUserRepository mockUserRepository;

    private User usr;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usr = new User(0, "lgn", "password", "Analyst");
    }

    @Test
    public void addUser_ShouldReturnTrue_WhenPermissionGranted() throws RepositoryException, AccessDeniedException {
        User usr2 = new User(3, "lgn1", "password2", "Analyst");

        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");
        Mockito.when(mockUserRepository.saveUser(usr2)).thenReturn(true);

        boolean result = usrService.addUser(usr.id, usr2);
        assertTrue(result);
    }

    @Test
    public void addUser_ShouldThrowAccessDenied_WhenNoPermission() throws RepositoryException {
        User usr2 = new User(3, "lgn1", "password2", "Analyst");

        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertThrows(AccessDeniedException.class, () -> usrService.addUser(usr.id, usr2));
    }

    @Test
    public void updateUser_ShouldReturnTrue_WhenPermissionGranted() throws RepositoryException, AccessDeniedException {
        User usr2 = new User(3, "lgn1", "password2", "Analyst");

        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");
        Mockito.when(mockUserRepository.editUser(usr2)).thenReturn(true);

        boolean result = usrService.editUser(usr.id, usr2);
        assertTrue(result);
    }

    @Test
    public void updateUser_ShouldThrowAccessDenied_WhenNoPermission() throws RepositoryException {
        User usr2 = new User(3, "lgn1", "password2", "Analyst");

        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertThrows(AccessDeniedException.class, () -> usrService.editUser(usr.id, usr2));
    }

    @Test
    public void delUser_ShouldReturnTrue_WhenPermissionGranted() throws RepositoryException, AccessDeniedException {
        int userId = 1;
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");
        Mockito.when(mockUserRepository.delUser(userId)).thenReturn(true);

        boolean result = usrService.delUser(usr.id, userId);
        assertTrue(result);
    }

    @Test
    public void delUser_ShouldThrowAccessDenied_WhenNoPermission() throws RepositoryException {
        int userId = 1;
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertThrows(AccessDeniedException.class, () -> usrService.delUser(usr.id, userId));
    }

    @Test
    public void delUser_ShouldThrowIllegalArgument_WhenDeletingOwnAccount() throws RepositoryException, AccessDeniedException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");

        assertThrows(IllegalArgumentException.class, () -> usrService.delUser(usr.id, usr.id));
    }

    @Test
    public void getUserById_ShouldReturnUser_WhenPermissionGranted() throws RepositoryException, AccessDeniedException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");
        Mockito.when(mockUserRepository.getUser(usr.id)).thenReturn(usr);

        User result = usrService.getUserById(usr.id, usr.id);
        assertEquals(usr, result);
    }

    @Test
    public void getUserById_ShouldThrowAccessDenied_WhenNoPermission() throws RepositoryException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertThrows(AccessDeniedException.class, () -> usrService.getUserById(usr.id, usr.id));
    }

    @Test
    public void getAllUsersId_ShouldReturnIds_WhenAdmin() throws RepositoryException, AccessDeniedException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Admin");
        Mockito.when(mockUserRepository.getAllUsersId()).thenReturn(List.of(1, 2, 3));

        List<Integer> ids = usrService.getAllUsersId(-5555);
        assertEquals(List.of(1, 2, 3), ids);
    }

    @Test
    public void getAllUsersId_ShouldThrowAccessDenied_WhenNoPermission() throws RepositoryException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertThrows(AccessDeniedException.class, () -> usrService.getAllUsersId(usr.id));
    }

    @Test
    public void getUserLP_ShouldReturnUser_WhenValidCredentials() throws RepositoryException {
        Mockito.when(mockUserRepository.getUser("lgn", "password")).thenReturn(usr);

        User result = usrService.getUserLP("lgn", "password");
        assertEquals(usr, result);
    }

    @Test
    public void getUserLP_ShouldThrowRepositoryException_WhenInvalidCredentials() throws RepositoryException {
        Mockito.when(mockUserRepository.getUser("lgn", "password")).thenThrow(new RepositoryException("Test"));

        assertThrows(RepositoryException.class, () -> usrService.getUserLP("lgn", "password"));
    }

    @Test
    public void hasPermission_ShouldReturnTrue_ForAnalystRole() throws RepositoryException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Analyst");

        assertTrue(usrService.hasPermission(usr.id, Permission.ADD_USER));
        assertTrue(usrService.hasPermission(usr.id, Permission.GET_DTP_BY_PARAMS));
    }

    @Test
    public void hasPermission_ShouldReturnFalse_ForGuestRole() throws RepositoryException {
        Mockito.when(mockUserRepository.getRole(usr.id)).thenReturn("Guest");

        assertFalse(usrService.hasPermission(usr.id, Permission.ADD_USER));
        assertFalse(usrService.hasPermission(usr.id, Permission.EDIT_CAR));
        assertTrue(usrService.hasPermission(usr.id, Permission.GET_DTP_BY_PARAMS));
    }
}
