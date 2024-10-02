package configurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configurator {
    private static final Map<String, String> configValues = new HashMap<>();

    static {
        readConfigFile("app.properties");
    }

    private static void readConfigFile(String configFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    configValues.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getValue(String key) {
        return configValues.get(key);
    }
}



