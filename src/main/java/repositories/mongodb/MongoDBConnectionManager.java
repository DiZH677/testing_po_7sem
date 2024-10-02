package repositories.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBConnectionManager {
    private static volatile MongoDBConnectionManager instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoDBConnectionManager(String serverAddress, String dbName, String username, String password) {
        this.mongoClient = connectToMongoDB(serverAddress, dbName, username, password);
        this.database = mongoClient.getDatabase(dbName);
    }

    public static MongoDBConnectionManager getInstance(String serverAddress, String dbName, String username, String password) {
        if (instance == null) {
            synchronized (MongoDBConnectionManager.class) {
                if (instance == null) {
                    instance = new MongoDBConnectionManager(serverAddress, dbName, username, password);
                }
            }
        }
        return instance;
    }

    private MongoClient connectToMongoDB(String serverAddress, String dbName, String username, String password) {
        MongoClient mongoClient = null;
        String uri = "mongodb://" + username + ":" + password + "@" + serverAddress + "/" + dbName;
        try {
            mongoClient = MongoClients.create(uri);
            System.out.println("Connected to the MongoDB server successfully.");
        } catch (Exception e) {
            System.out.println("Error connecting to MongoDB server: " + e.getMessage());
        }
        return mongoClient;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connection to the MongoDB server closed successfully.");
        }
    }
}
