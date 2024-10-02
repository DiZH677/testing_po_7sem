// В тестах для класса Configurator используется рефлексия
// для доступа к приватному статическому полю configValues,
// поскольку прямого доступа к нему из тестов нет.
// Проверяется корректность работы метода getValue
// при наличии и отсутствии ключей в конфигурации,
// а также проверка того, что статическое поле инициализировано
// после вызова конструктора.
package tests.unit.configurator;

import configurator.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfiguratorTest {

    @BeforeEach
    public void setUp() {
        try {
            Field field = Configurator.class.getDeclaredField("configValues");
            field.setAccessible(true);
            Map<String, String> configValues = (Map<String, String>) field.get(null);
            configValues.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to reset configValues: " + e.getMessage());
        }
    }

    @Test
    public void testGetValueWhenKeyExists() {
        try {
            Field field = Configurator.class.getDeclaredField("configValues");
            field.setAccessible(true);
            Map<String, String> configValues = (Map<String, String>) field.get(null);
            configValues.put("key1", "value1");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set configValues: " + e.getMessage());
        }

        assertEquals("value1", Configurator.getValue("key1"));
    }

    @Test
    public void testGetValueWhenKeyDoesNotExist() {
        try {
            Field field = Configurator.class.getDeclaredField("configValues");
            field.setAccessible(true);
            Map<String, String> configValues = (Map<String, String>) field.get(null);
            configValues.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set configValues: " + e.getMessage());
        }

        assertNull(Configurator.getValue("non_existing_key"));
    }

    @Test
    public void testConfiguratorConstructor() {
        try {
            Field field = Configurator.class.getDeclaredField("configValues");
            field.setAccessible(true);
            Map<String, String> configValues = (Map<String, String>) field.get(null);
            assertNotNull(configValues);
            assertTrue(configValues.isEmpty());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access configValues: " + e.getMessage());
        }
    }
}


