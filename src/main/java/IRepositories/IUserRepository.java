package IRepositories;

import exceptions.RepositoryException;
import user.User;

import java.util.List;

public interface IUserRepository {
    List<Integer> getAllUsersId() throws RepositoryException;
    User getUser(int id) throws RepositoryException;
    User getUser(String login, String password) throws RepositoryException;
    String getRole(int id) throws RepositoryException;
    boolean saveUser(User usr) throws RepositoryException;
    boolean delUser(int del_id) throws RepositoryException;
    boolean editUser(User usr) throws RepositoryException;
}
