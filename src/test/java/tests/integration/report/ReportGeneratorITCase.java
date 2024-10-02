package tests.integration.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.DCP;
import entities.DTP;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportGeneratorITCase {

    private ReportGenerator reportGenerator;
    private DCP testData;
    private String testFilename;

    @BeforeEach
    public void setUp() {
        // Создаем экземпляр ReportGenerator
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        reportGenerator = new ReportGenerator(gson);

        // Подготавливаем тестовые данные
        testData = new DCP();
        testData.setDTPs(List.of(new DTP(1, "Test DTP 1", "2024-09-30 10:00:00", 55.0, 37.0, "Main road", "Accident", 2, 1)));

        // Имя файла для тестов
        testFilename = "test_report";
    }

    @AfterEach
    public void tearDown() {
        // Удаление тестового файла после выполнения теста
        File file = new File(testFilename + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testGenerateReport_Success() {
        // Проверяем успешное создание отчета в формате JSON
        boolean result = reportGenerator.generateReport(testFilename, "json", testData);

        assertTrue(result, "Отчет должен быть успешно создан");
        File reportFile = new File(testFilename + ".json");
        assertTrue(reportFile.exists(), "Файл отчета должен существовать");
        String expectedJson = new GsonBuilder().setPrettyPrinting().create().toJson(testData);
        String actualContent = readFileContent(reportFile);
        assertEquals(expectedJson, actualContent, "Содержимое файла должно соответствовать ожидаемому JSON");
    }

    @Test
    public void testGenerateReport_InvalidFormat() {
        boolean result = reportGenerator.generateReport(testFilename, "xml", testData);

        assertFalse(result, "Отчет не должен быть создан для неподдерживаемого формата");
        File reportFile = new File(testFilename + ".xml");
        assertFalse(reportFile.exists(), "Файл отчета не должен существовать для неподдерживаемого формата");
    }

    @Test
    public void testGetReport_Success() {
        byte[] reportBytes = reportGenerator.getReport("json", testData);

        assertNotNull(reportBytes, "Отчет должен быть успешно создан");
        String expectedJson = new GsonBuilder().setPrettyPrinting().create().toJson(testData);
        assertEquals(expectedJson, new String(reportBytes), "Содержимое отчета должно соответствовать ожидаемому JSON");
        File reportFile = new File("json.json");
        assertTrue(reportFile.exists(), "Файл отчета должен существовать");
        reportFile.delete();
    }

    @Test
    public void testGetReport_InvalidFormat() {
        byte[] reportBytes = reportGenerator.getReport("xml", testData);

        assertNull(reportBytes, "Отчет не должен быть создан для неподдерживаемого формата");
        File reportFile = new File("xml.json");
        assertFalse(reportFile.exists(), "Файл отчета не должен существовать для неподдерживаемого формата");
    }

    // Вспомогательный метод для чтения содержимого файла
    private String readFileContent(File file) {
        try {
            return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            fail("Не удалось прочитать файл: " + file.getName());
            return null;
        }
    }
}

