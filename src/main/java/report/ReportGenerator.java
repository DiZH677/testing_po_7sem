package report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.DCP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportGenerator {
    private final Gson gson;

    public ReportGenerator() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // Конструктор для мокирования Gson в тестах
    public ReportGenerator(Gson gson) {
        this.gson = gson;
    }

    public boolean generateReport(String fnm, String frmt, DCP repData)
    {
        if (frmt.equalsIgnoreCase("json")) {
            String jsonContent = generateJsonReport(repData);
            // Создание файла и сохранение в него JSON-отчета
            try {
                File file = new File(fnm + ".json");
                FileWriter writer = new FileWriter(file);
                writer.write(jsonContent);
                writer.close();
                return true; // Возвращаем true, если сохранение прошло успешно
            } catch (IOException e) {
                return false; // Возвращаем false, если произошла ошибка при сохранении
            }
        }

        return false; // Возвращаем false в случае, если формат не поддерживается
    }

    public byte[] getReport(String format, DCP repData) {
        if (format.equalsIgnoreCase("json")) {
            String jsonContent = generateJsonReport(repData);
            // Создание файла и сохранение в него JSON-отчета
            try {
                File file = new File(format + ".json");
                FileWriter writer = new FileWriter(file);
                writer.write(jsonContent);
                writer.close();
                return jsonContent.getBytes(); // Возвращаем true, если сохранение прошло успешно
            } catch (IOException e) {
                return null; // Возвращаем false, если произошла ошибка при сохранении
            }
        }

        return null;
    }

    private String generateJsonReport(DCP reportData) {
        return gson.toJson(reportData);
    }
}

