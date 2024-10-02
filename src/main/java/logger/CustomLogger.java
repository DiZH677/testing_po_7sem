package logger;

import java.io.IOException;
import java.util.logging.*;

public class CustomLogger {
    private static final Logger logger = Logger.getLogger(CustomLogger.class.getName());

    static {
        logger.setUseParentHandlers(false);

        // Удаляем обработчик для вывода на консоль
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.OFF);
            }
        }

        try {
            // Указываем путь и имя файла, куда будут записываться логи
            Handler fh = new FileHandler("mylog.log");
            logger.addHandler(fh); // Добавляем обработчик для записи в файл
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("Logger initialized");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in configuring main.java.logger", e);
        }
    }

    public static void logInfo(String message, String className) {
        logger.info("(" + className + ") " + message);
    }

    public static void logWarning(String message, String className) {
        logger.warning("(" + className + ") " + message);
    }

    public static void logError(String message, String className) {
        logger.severe("(" + className + ") " + message);
    }
}
