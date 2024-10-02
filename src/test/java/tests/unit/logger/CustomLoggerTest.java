// ArgumentCaptor нужен для захвата сообщений, переданных логгером, и проверки их содержания
// verify(mockHandler).publish(captor.capture()) проверяет, что метод publish был вызван у мока
// captor.getValue() захватывает переданную в publish запись (объект LogRecord)
package tests.unit.logger;

import logger.CustomLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomLoggerTest {

    private Handler mockHandler;
    private ArgumentCaptor<LogRecord> captor;
    private java.util.logging.Logger logger;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockHandler = mock(Handler.class);
        captor = ArgumentCaptor.forClass(LogRecord.class);

        // рефлексия
        Field loggerField = CustomLogger.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        logger = (java.util.logging.Logger) loggerField.get(null); // получение статического поля
        logger.addHandler(mockHandler);
    }

    @Test
    void logInfo_positive() {
        CustomLogger.logInfo("Info message", "TestClass");

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.INFO, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("TestClass"));
        assertTrue(logRecord.getMessage().contains("Info message"));
    }

    @Test
    void logWarning_positive() {
        CustomLogger.logWarning("Warning message", "TestClass");

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.WARNING, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("TestClass"));
        assertTrue(logRecord.getMessage().contains("Warning message"));
    }

    @Test
    void logError_positive() {
        CustomLogger.logError("Error message", "TestClass");

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.SEVERE, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("TestClass"));
        assertTrue(logRecord.getMessage().contains("Error message"));
    }

    @Test
    void logInfo_negative() {
        CustomLogger.logInfo(null, "TestClass");

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.INFO, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("TestClass"));
        assertTrue(logRecord.getMessage().contains("null"));
    }

    @Test
    void logWarning_negative() {
        CustomLogger.logWarning("", "TestClass");

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.WARNING, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("TestClass"));
        assertEquals("(TestClass) ", logRecord.getMessage());
    }

    @Test
    void logError_negative() {
        CustomLogger.logError("Error message", null);

        verify(mockHandler).publish(captor.capture());
        LogRecord logRecord = captor.getValue();

        assertEquals(Level.SEVERE, logRecord.getLevel());
        assertTrue(logRecord.getMessage().contains("Error message"));
        assertTrue(logRecord.getMessage().contains("null"));
    }
}


