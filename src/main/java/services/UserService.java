package services;

import IRepositories.IUserRepository;
import exceptions.RepositoryException;
import permission.Permission;
import user.User;

import java.nio.file.AccessDeniedException;
import java.util.List;

public class UserService {
    private IUserRepository userRepository;

    public UserService(IUserRepository rep) {
        this.userRepository = rep;
    }

    public List<Integer> getAllUsersId(int usrid) throws AccessDeniedException, RepositoryException {
        if (hasPermission(usrid, Permission.ADMIN))
            return userRepository.getAllUsersId();
        else throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
    }

    private String getRole(int id) throws RepositoryException {
        return userRepository.getRole(id);
    }

    public User getUserLP(String login, String password) throws RepositoryException {
        return userRepository.getUser(login, password);
    }

    public User getUserById(int usrid, int id) throws AccessDeniedException, RepositoryException {
        if (hasPermission(usrid, Permission.ADMIN))
            return userRepository.getUser(id);
        else throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
    }

    public boolean addUser(int usrid, User usr) throws AccessDeniedException, RepositoryException {
        if (hasPermission(usrid, Permission.ADD_USER))
                return userRepository.saveUser(usr);
        else throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
    }

    public boolean delUser(int usrid, int id) throws AccessDeniedException, IllegalArgumentException, RepositoryException {
        if (hasPermission(usrid, Permission.DELETE_USER))
            if (usrid == id)
                throw new IllegalArgumentException("You cannot delete your own main.java.user account.");
            else
                return userRepository.delUser(id);
        else throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
    }

    public boolean editUser(int usrid, User usr) throws AccessDeniedException, RepositoryException {
        if (hasPermission(usrid, Permission.EDIT_USER))
            return userRepository.editUser(usr);
        else throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
    }

    public boolean hasPermission(int usrid, Permission permission) throws RepositoryException {
        String role = getRole(usrid);

        if (role == null && usrid != -5555)
            return false;

        if (usrid == -5555 || role.equals("Analyst"))
            return true;

        else if (role.equals("Guest")) {
            return permission == Permission.GET_DTP_BY_PARAMS;
        }
        else if (role.equals("User")) {
            return permission == Permission.ADD_DTP ||
                    permission == Permission.ADD_CAR ||
                    permission == Permission.ADD_PARTICIPANT ||
                    permission == Permission.GET_DTP ||
                    permission == Permission.EDIT_DTP ||
                    permission == Permission.EDIT_CAR ||
                    permission == Permission.EDIT_PARTICIPANT ||
                    permission == Permission.GET_DTP_BY_PARAMS ||
                    permission == Permission.GET_CAR_BY_PARAMS ||
                    permission == Permission.GET_PARTICIPANT_BY_PARAMS;
        }

        return false;
    }
}

