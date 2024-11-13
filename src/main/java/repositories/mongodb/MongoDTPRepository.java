package repositories.mongodb;

import IRepositories.IDTPRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import entities.DTP;
import exceptions.RepositoryException;
import logger.CustomLogger;
import org.bson.Document;
import params.DTPParams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDTPRepository implements IDTPRepository {
    private final MongoDBConnectionManager connectionManager;
    private final MongoCollection<Document> collection;

    public MongoDTPRepository(MongoDBConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.collection = connectionManager.getDatabase().getCollection("dtp");
    }

    @Override
    public DTP getDTP(int id) throws RepositoryException {
        try {
            Document doc = collection.find(Filters.eq("ID", id)).first();
            // Предположим, что doc.getDate("datetime") возвращает объект Date
            Date date = doc.getDate("datetime"); // Замените на doc.getDate("datetime")
            // Форматирование Date в нужный формат
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = formatter.format(date);
            if (doc != null) {
                return new DTP(
                        doc.getInteger("ID"),
                        doc.getString("description"),
                        formattedDate,
                        doc.getDouble("coord_w"),
                        doc.getDouble("coord_l"),
                        doc.getString("dor"),
                        doc.getString("osv"),
                        doc.getInteger("count_ts"),
                        doc.getInteger("count_parts")
                );
            } else {
                return null;
            }
        } catch (Exception e) {
            CustomLogger.logError("Error while getting DTP by id", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting DTP by id..." + e.getMessage());
        }
    }

    @Override
    public boolean saveDTP(DTP dtp) throws RepositoryException {
        try {
            int maxId = 0;
            MongoCursor<Document> cursor = collection.find().projection(Projections.fields(Projections.include("ID"), Projections.excludeId())).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Integer id = doc.getInteger("ID");
                if (id != null && id > maxId) {
                    maxId = id;
                }
            }
            cursor.close();

            dtp.setId(maxId + 1);
            // Создание и форматирование даты
            Date datetime = new Date(); // Пример: текущая дата и время


            Document doc = new Document("ID", dtp.getId())
                    .append("description", dtp.getDescription())
                    .append("datetime", datetime)
                    .append("coord_w", dtp.getCoords().get(0))
                    .append("coord_l", dtp.getCoords().get(1))
                    .append("dor", dtp.getDor())
                    .append("osv", dtp.getOsv())
                    .append("count_ts", dtp.getCountTs())
                    .append("count_parts", dtp.getCountParts());

            collection.insertOne(doc);
            Document insertedDoc = collection.find(Filters.eq("ID", dtp.getId())).first();
            if (insertedDoc != null) {
                System.out.println("Document inserted successfully: " + insertedDoc.toJson());
            } else {
                System.out.println("Document insertion failed.");
            }
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while saving DTP", this.getClass().getSimpleName());
            throw new RepositoryException("Error while saving DTP..." + e.getMessage());
        }
    }

    @Override
    public boolean delDTP(int del_id) throws RepositoryException {
        try {
            collection.deleteOne(Filters.eq("ID", del_id));
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while deleting DTP", this.getClass().getSimpleName());
            throw new RepositoryException("Error while deleting DTP..." + e.getMessage());
        }
    }

    @Override
    public boolean editDTP(DTP dtp) throws RepositoryException {
        try {
            // Пример строки даты
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date datetime = null;
            try {
                datetime = formatter.parse(dtp.getDatetime());
            } catch (ParseException e) {
                System.out.println("Неверный формат даты. Пожалуйста, используйте формат: гггг-мм-дд чч:мм:сс");
            }
            Document doc = new Document("ID", dtp.getId())
                    .append("description", dtp.getDescription())
                    .append("datetime", datetime)
                    .append("coord_w", dtp.getCoords().get(0))
                    .append("coord_l", dtp.getCoords().get(1))
                    .append("dor", dtp.getDor())
                    .append("osv", dtp.getOsv())
                    .append("count_ts", dtp.getCountTs())
                    .append("count_parts", dtp.getCountParts());

            collection.replaceOne(Filters.eq("ID", dtp.getId()), doc);
            return true;
        } catch (Exception e) {
            CustomLogger.logError("Error while editing DTP", this.getClass().getSimpleName());
            throw new RepositoryException("Error while editing DTP..." + e.getMessage());
        }
    }

    @Override
    public List<DTP> getDTPByParams(DTPParams params) throws RepositoryException {
        try {
            List<DTP> result = new ArrayList<>();
            Document query = new Document();

            buildDatetimeQuery(params, query);
            buildIdQuery(params, query);
            buildCountTsQuery(params, query);

            MongoCursor<Document> cursor = collection.find(query).iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                result.add(mapToDTP(doc));
            }
            cursor.close();

            return result;
        } catch (Exception e) {
            CustomLogger.logError("Error while getting DTP by params", this.getClass().getSimpleName());
            throw new RepositoryException("Error while getting DTP by params..." + e.getMessage());
        }
    }

    private void buildDatetimeQuery(DTPParams params, Document query) {
        if (params.dtpBegin != null && params.dtpEnd != null) {
            query.append("datetime", new Document("$gte", params.dtpBegin).append("$lte", params.dtpEnd));
        } else if (params.dtpBegin != null) {
            query.append("datetime", new Document("$gte", params.dtpBegin));
        } else if (params.dtpEnd != null) {
            query.append("datetime", new Document("$lte", params.dtpEnd));
        }
    }

    private void buildIdQuery(DTPParams params, Document query) {
        if (params.dtpIdBegin != null && params.dtpIdEnd != null) {
            query.append("ID", new Document("$gte", params.dtpIdBegin).append("$lte", params.dtpIdEnd));
        } else if (params.dtpIdBegin != null) {
            query.append("ID", new Document("$gte", params.dtpIdBegin));
        } else if (params.dtpIdEnd != null) {
            query.append("ID", new Document("$lte", params.dtpIdEnd));
        }
    }

    private void buildCountTsQuery(DTPParams params, Document query) {
        if (params.countTs != null) {
            query.append("count_ts", params.countTs);
        }
    }

    private DTP mapToDTP(Document doc) {
        return new DTP(
                doc.getInteger("ID"),
                doc.getString("description"),
                doc.getDate("datetime").toString(),
                doc.getDouble("coord_w"),
                doc.getDouble("coord_l"),
                doc.getString("dor"),
                doc.getString("osv"),
                doc.getInteger("count_ts"),
                doc.getInteger("count_parts")
        );
    }
}


