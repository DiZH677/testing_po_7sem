package repositories.mongodb;

import IRepositories.ICarRepository;
import entities.Car;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.CarParams;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class MongoCarRepository implements ICarRepository {
    private final MongoDBConnectionManager connectionManager;

    public MongoCarRepository(MongoDBConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Car getCar(int id) throws RepositoryException {
        try {
            Document doc = connectionManager.getCollection("vehicle").find(new Document("id", id)).first();
            if (doc == null) {
                return null;
            }
            return documentToCar(doc);
        } catch (Exception e) {
            CustomLogger.logError("Error while getting Car by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Car by id... " + e.getMessage());
        }
    }

    @Override
    public boolean saveCar(Car car) throws RepositoryException {
        try {
            if (car.getId() == -1) {
                car.setId(getNextId());
            }
            connectionManager.getCollection("vehicle").insertOne(carToDocument(car));
            CustomLogger.logInfo("Successful saving Car", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while saving Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving Car... " + e.getMessage());
        }
    }

    @Override
    public boolean delCar(int id) throws RepositoryException {
        try {
            connectionManager.getCollection("vehicle").deleteOne(new Document("id", id));
            CustomLogger.logInfo("Successful deleting Car", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while deleting Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting Car... " + e.getMessage());
        }
    }

    @Override
    public boolean editCar(Car car) throws RepositoryException {
        try {
            connectionManager.getCollection("vehicle").replaceOne(new Document("id", car.getId()), carToDocument(car));
            CustomLogger.logInfo("Successful editing Car", this.getClass().getSimpleName());
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while editing Car", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing Car... " + e.getMessage());
        }
    }

    @Override
    public List<Car> getCarsByParams(CarParams params) throws RepositoryException {
        List<Car> result = new ArrayList<>();
        try {
            Document query = getQuery(params);
            for (Document doc : connectionManager.getCollection("vehicle").find(query)) {
                result.add(documentToCar(doc));
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting Car by main.java.params", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting Car by main.java.params...\n" + e.getMessage());
        }
        CustomLogger.logInfo("Successful getting Car by main.java.params", this.getClass().getSimpleName());
        return result;
    }

    private static Car documentToCar(Document doc) {
        return new Car(
                doc.getInteger("id"),
                doc.getInteger("dtp_id"),
                doc.getString("marka_ts"),
                doc.getString("m_ts"),
                doc.getInteger("car_year"),
                doc.getString("color"),
                doc.getString("type_ts")
        );
    }

    private static Document carToDocument(Car car) {
        return new Document("id", car.getId())
                .append("dtp_id", car.getDtpId())
                .append("marka_ts", car.getMarka())
                .append("m_ts", car.getModel())
                .append("car_year", car.getCarYear())
                .append("color", car.getColor())
                .append("type_ts", car.getTypeTS());
    }

    private int getNextId() {
        Document doc = connectionManager.getCollection("vehicle").find().sort(new Document("id", -1)).first();
        return doc == null ? 1 : doc.getInteger("id") + 1;
    }

    private static Document getQuery(CarParams params) {
        List<Document> filters = new ArrayList<>();
        if (params.carIdBegin != null) {
            filters.add(new Document("id", new Document("$gte", params.carIdBegin)));
        }
        if (params.carIdEnd != null) {
            filters.add(new Document("id", new Document("$lte", params.carIdEnd)));
        }
        if (params.color != null) {
            filters.add(new Document("color", params.color));
        }
        if (params.marka != null) {
            filters.add(new Document("marka_ts", params.marka));
        }
        if (params.model != null) {
            filters.add(new Document("m_ts", params.model));
        }

        // Добавьте аналогичные фильтры для остальных параметров

        return filters.isEmpty() ? new Document() : new Document("$and", filters);
    }
}


