package repositories.mongodb;

import IRepositories.IUserRepository;
import com.mongodb.client.MongoCollection;
import exceptions.RepositoryException;
import logger.CustomLogger;
import org.bson.Document;
import user.User;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoUserRepository implements IUserRepository {
    private final MongoDBConnectionManager connectionManager;
    private final MongoCollection<Document> userCollection;

    public MongoUserRepository(MongoDBConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.userCollection = connectionManager.getDatabase().getCollection("users");
    }

    @Override
    public List<Integer> getAllUsersId() throws RepositoryException {
        List<Integer> userIds = new ArrayList<>();
        try {
            for (Document doc : userCollection.find()) {
                userIds.add(doc.getInteger("_id"));
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting all User ids", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting all User ids... " + e.getMessage());
        }
        return userIds;
    }

    @Override
    public User getUser(int id) throws RepositoryException {
        try {
            Document doc = userCollection.find(eq("_id", id)).first();
            if (doc == null) {
                return null;
            }
            return documentToUser(doc);
        } catch (Exception e) {
            CustomLogger.logError("Error while getting User by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting User by id... " + e.getMessage());
        }
    }

    @Override
    public User getUser(String login, String password) throws RepositoryException {
        try {
            Document doc = userCollection.find(eq("login", login)).first();
            if (doc != null && doc.getString("password").equals(password)) {
                return documentToUser(doc);
            } else {
                return null;
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting User by login and password", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting User by login and password... " + e.getMessage());
        }
    }

    @Override
    public String getRole(int id) throws RepositoryException {
        try {
            Document doc = userCollection.find(eq("_id", id)).first();
            if (doc != null) {
                return doc.getString("role");
            } else {
                return null;
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting UserRole by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting UserRole by id... " + e.getMessage());
        }
    }

    @Override
    public boolean saveUser(User usr) throws RepositoryException {
        try {
            if (usr.getId() == -1) {
                usr.setId(getNextId());
            }
            userCollection.insertOne(userToDocument(usr));
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while saving User", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving User... " + e.getMessage());
        }
    }

    @Override
    public boolean delUser(int del_id) throws RepositoryException {
        try {
            userCollection.deleteOne(eq("_id", del_id));
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while deleting User", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting User... " + e.getMessage());
        }
    }

    @Override
    public boolean editUser(User usr) throws RepositoryException {
        try {
            userCollection.replaceOne(eq("_id", usr.getId()), userToDocument(usr));
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while editing User", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing User... " + e.getMessage());
        }
    }

    private static User documentToUser(Document doc) {
        return new User(
                doc.getInteger("_id"),
                doc.getString("login"),
                doc.getString("password"),
                doc.getString("role")
        );
    }

    private static Document userToDocument(User usr) {
        return new Document("_id", usr.getId())
                .append("login", usr.getLogin())
                .append("password", usr.getPassword())
                .append("role", usr.getRole());
    }

    private int getNextId() {
        Document doc = userCollection.find().sort(new Document("id", -1)).first();
        return doc == null ? 1 : doc.getInteger("id") + 1;
    }
}

