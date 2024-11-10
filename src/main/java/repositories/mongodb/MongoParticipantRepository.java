package repositories.mongodb;

import IRepositories.IParticipantRepository;
import com.mongodb.client.MongoCollection;
import entities.Participant;
import exceptions.RepositoryException;
import logger.CustomLogger;
import org.bson.Document;
import params.ParticipantParams;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoParticipantRepository implements IParticipantRepository {
    private final MongoDBConnectionManager connectionManager;
    private final MongoCollection<Document> participantCollection;

    public MongoParticipantRepository(MongoDBConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.participantCollection = connectionManager.getDatabase().getCollection("participant");
    }

    @Override
    public Participant getParticipant(int id) throws RepositoryException {
        try {
            Document doc = participantCollection.find(eq("id", id)).first();
            if (doc == null) {
                return null;
            }
            return documentToParticipant(doc);
        } catch (Exception e) {
            CustomLogger.logError("Error while getting Participant by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Participant by id... " + e.getMessage());
        }
    }

    @Override
    public boolean saveParticipant(Participant prt) throws RepositoryException {
        try {
            if (prt.getId() == -1) {
                prt.setId(getNextId());
            }
            participantCollection.insertOne(participantToDocument(prt));
            CustomLogger.logInfo("Successful saving Participant", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while saving Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving Participant... " + e.getMessage());
        }
    }

    @Override
    public boolean delParticipant(int del_id) throws RepositoryException {
        try {
            participantCollection.deleteOne(eq("id", del_id));
            CustomLogger.logInfo("Successful deleting Participant", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while deleting Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting Participant... " + e.getMessage());
        }
    }

    @Override
    public boolean editParticipant(Participant prt) throws RepositoryException {
        try {
            participantCollection.replaceOne(eq("id", prt.getId()), participantToDocument(prt));
            CustomLogger.logInfo("Successful editing Participant", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while editing Participant", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing Participant... " + e.getMessage());
        }
    }

    @Override
    public List<Participant> getParticByParams(ParticipantParams params) throws RepositoryException {
        List<Participant> result = new ArrayList<>();
        try {
            Document query = getQuery(params);
            for (Document doc : participantCollection.find(query)) {
                result.add(documentToParticipant(doc));
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting Participant by main.java.params", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Participant by main.java.params...\n" + e.getMessage());
        }
        CustomLogger.logInfo("Successful getting Participant by main.java.params", this.getClass().getSimpleName());
        return result;
    }

    private static Participant documentToParticipant(Document doc) {
        return new Participant(
                doc.getInteger("id"),
                doc.getInteger("vehicle_id"),
                doc.getString("category"),
                doc.getString("health"),
                doc.getString("pol"),
                doc.getBoolean("safety_belt")
        );
    }

    private static Document participantToDocument(Participant prt) {
        return new Document("id", prt.getId())
                .append("vehicle_id", prt.getCarId())
                .append("category", prt.getCategory())
                .append("health", prt.getHealth())
                .append("pol", prt.getPol())
                .append("safety_belt", prt.getSafetyBelt());
    }

    private int getNextId() {
        Document doc = participantCollection.find().sort(new Document("id", -1)).first();
        return doc == null ? 1 : doc.getInteger("id") + 1;
    }

    private static Document getQuery(ParticipantParams params) {
        List<Document> filters = new ArrayList<>();
        if (params.prIdBegin != null) {
            filters.add(new Document("id", new Document("$gte", params.prIdBegin)));
        }
        if (params.prIdEnd != null) {
            filters.add(new Document("id", new Document("$lte", params.prIdEnd)));
        }
        if (params.category != null) {
            filters.add(new Document("category", params.category));
        }
        if (params.pol != null) {
            filters.add(new Document("pol", params.pol));
        }
        if (params.health != null) {
            filters.add(new Document("health", params.health));
        }
        if (params.safety_belt != null) {
            filters.add(new Document("safety_belt", params.safety_belt));
        }

        return filters.isEmpty() ? new Document() : new Document("$and", filters);
    }
}

