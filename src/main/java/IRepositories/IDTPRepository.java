package IRepositories;

import entities.DTP;
import exceptions.RepositoryException;
import params.DTPParams;

import java.util.List;

public interface IDTPRepository {
    DTP getDTP(int id) throws RepositoryException;
    boolean saveDTP(DTP dtp) throws RepositoryException;
    boolean delDTP(int del_id) throws RepositoryException;
    boolean editDTP(DTP dtp) throws RepositoryException;
    List<DTP> getDTPByParams(DTPParams params) throws RepositoryException;
}
