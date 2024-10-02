package tests.unit.services;

import entities.DCP;
import entities.DTP;
import exceptions.RepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import params.Params;
import report.ReportGenerator;
import services.DTPService;
import services.ReportService;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertNull;
import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @Mock
    private ReportGenerator mockReportGenerator;
    @Mock
    private DTPService mockDTPService;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetReportGenerator_Success() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        ReportGenerator newReportGenerator = mock(ReportGenerator.class);

        // Act
        reportService.setReportGenerator(newReportGenerator);

        // Assert
        assertEquals(newReportGenerator, getPrivateField(reportService, "reportGenerator"));
    }

    @Test
    public void testSetReportGenerator_Null() throws NoSuchFieldException, IllegalAccessException {
        // Act
        reportService.setReportGenerator(null);

        // Assert
        assertNull(getPrivateField(reportService, "reportGenerator"));
    }

    @Test
    public void testSetDTPService_Success() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        DTPService newDTPService = mock(DTPService.class);

        // Act
        reportService.setDTPService(newDTPService);

        // Assert
        assertEquals(newDTPService, getPrivateField(reportService, "dtpService"));
    }

    @Test
    public void testSetDTPService_Null() throws NoSuchFieldException, IllegalAccessException {
        // Act
        reportService.setDTPService(null);

        // Assert
        assertNull(getPrivateField(reportService, "dtpService"));
    }

    @Test
    public void testSave_Success() throws AccessDeniedException, RepositoryException {
        // Arrange
        int userId = 1;
        String fileName = "report";
        String format = "PDF";
        Params params = new Params();
        List<DTP> dtpList = new ArrayList<>();
        DCP data = new DCP(dtpList, new ArrayList<>(), new ArrayList<>());
        when(mockDTPService.getAllByParams(userId, params)).thenReturn(data);
        when(mockReportGenerator.generateReport(fileName, format, data)).thenReturn(true);

        // Act
        boolean result = reportService.save(userId, fileName, format, params);

        // Assert
        assertTrue(result);
        verify(mockReportGenerator).generateReport(fileName, format, data);
    }

    @Test
    public void testSave_Failure() throws AccessDeniedException, RepositoryException {
        // Arrange
        int userId = 1;
        String fileName = "report";
        String format = "PDF";
        Params params = new Params();
        List<DTP> dtpList = new ArrayList<>();
        DCP data = new DCP(dtpList, new ArrayList<>(), new ArrayList<>());
        when(mockDTPService.getAllByParams(userId, params)).thenReturn(data);
        when(mockReportGenerator.generateReport(fileName, format, data)).thenReturn(false);

        // Act
        boolean result = reportService.save(userId, fileName, format, params);

        // Assert
        assertFalse(result);
        verify(mockReportGenerator).generateReport(fileName, format, data);
    }

    @Test
    public void testGet_Success() throws AccessDeniedException, RepositoryException {
        // Arrange
        int userId = 1;
        String format = "PDF";
        Params params = new Params();
        List<DTP> dtpList = new ArrayList<>();
        DCP data = new DCP(dtpList, new ArrayList<>(), new ArrayList<>());
        byte[] reportBytes = new byte[0]; // Пустой массив байтов для успешного случая
        when(mockDTPService.getAllByParams(userId, params)).thenReturn(data);
        when(mockReportGenerator.getReport(format, data)).thenReturn(reportBytes);

        // Act
        byte[] result = reportService.get(userId, format, params);

        // Assert
        assertNotNull(result);
        assertArrayEquals(reportBytes, result);
        verify(mockReportGenerator).getReport(format, data);
    }

    @Test
    public void testGet_Failure() throws AccessDeniedException, RepositoryException {
        // Arrange
        int userId = 1;
        String format = "PDF";
        Params params = new Params();
        List<DTP> dtpList = new ArrayList<>();
        DCP data = new DCP(dtpList, new ArrayList<>(), new ArrayList<>());
        when(mockDTPService.getAllByParams(userId, params)).thenReturn(data);
        when(mockReportGenerator.getReport(format, data)).thenThrow(new RuntimeException("Generation error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> reportService.get(userId, format, params));
        assertEquals("Generation error", thrown.getMessage());
    }



    // Метод для доступа к приватному полю через рефлексию
    private Object getPrivateField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
