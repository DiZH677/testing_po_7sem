package IRepositories;

import entities.Participant;
import exceptions.RepositoryException;
import params.ParticipantParams;

import java.util.List;

public interface IParticipantRepository {
    Participant getParticipant(int id) throws RepositoryException;
    boolean saveParticipant(Participant pr) throws RepositoryException;
    boolean delParticipant(int del_id) throws RepositoryException;
    boolean editParticipant(Participant pr) throws RepositoryException;
    List<Participant> getParticByParams(ParticipantParams params) throws RepositoryException;
}
