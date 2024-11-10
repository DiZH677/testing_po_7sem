package tests.e2e;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features", // Путь к папке с feature файлами
        glue = "tests.e2e" // Путь к классам с шагами
)
public class CucumberTestE2E {
    // Этот класс пустой, его задача — просто запускать Cucumber
}