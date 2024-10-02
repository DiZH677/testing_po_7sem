package tests.unit.report;

import com.google.gson.Gson;
import entities.DCP;
import report.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tests.unit.entities.DCPTestFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


class ReportGeneratorTest {
    private Gson gson;

    private ReportGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gson = new Gson();
        generator = new ReportGenerator(gson);
    }

    @Test
    void generateReportFileIsSuccessfullyCreated() {
        DCP reportData = DCPTestFactory.createDefaultDCP();
        String filename = "testReport";
        String format = "json";

        boolean result = generator.generateReport(filename, format, reportData);

        assertTrue(result);
        File file = new File(filename + ".json");
        file.delete();
    }

    @Test
    void generateReportIOExceptionOccurs() {
        DCP reportData = DCPTestFactory.createDefaultDCP();
        String filename = "a".repeat(500);
        String format = "json";

        boolean result = generator.generateReport(filename, format, reportData);

        assertFalse(result);
    }

    @Test
    void getReportWhenFormatIsJson() {
        DCP reportData = DCPTestFactory.createDefaultDCP();
        String format = "json";
        generator = new ReportGenerator(gson);

        byte[] result = generator.getReport(format, reportData);

        assertNotNull(result);
    }

    @Test
    void getReportFormatIsNotJson() {
        DCP reportData = DCPTestFactory.createDefaultDCP();
        String format = "xml";

        byte[] result = generator.getReport(format, reportData);

        assertNull(result);
    }
}
