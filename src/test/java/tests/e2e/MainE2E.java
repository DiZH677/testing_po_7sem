package tests.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainE2E {
    @Test
    public void testDTPFlow() {
        // Шаг 1: Аутентификация
        String authPayload = "{ \"login\": \"lgn\", \"password\": \"pswrd\" }";
        Response authResponse = RestAssured
                .given()
                .baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .body(authPayload) // Передаем тело запроса в формате JSON
                .post("/auth"); // Выполняем POST запрос на /auth

        assertEquals(200, authResponse.getStatusCode());
        String token = authResponse.jsonPath().getString("token");

        // удаление ДТП (по /dtps/id DELETE) (если есть)
        Response delResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .delete("/dtps/99999"); // Выполняем GET запрос на /viewDTP

        assertEquals(200, delResponse.getStatusCode());

        // Добавление ДТП
        String addDTPJson = String.format(
                "{\"id\": 99999, \"description\": \"Testing dtp\", \"datetime\": \"2022-01-01T20:00:00.000Z\", \"coordL\": 50, \"coordW\": 51, \"osv\": \"Temnoe vremya\", \"countTs\": 1, \"countParts\": 2}");
        Response addResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .body(addDTPJson) // Передаем тело запроса в формате JSON
                .post("/addDTP"); // Выполняем POST запрос на /auth

        assertEquals(200, addResponse.getStatusCode());

        // Просмотр ДТП
        Response viewResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .get("/dtps/99999"); // Выполняем GET запрос на /viewDTP

        assertEquals(200, viewResponse.getStatusCode());
        assertEquals(viewResponse.jsonPath().getString("description"), "Testing dtp");
        assertEquals(viewResponse.jsonPath().getString("osv"), "Temnoe vremya");
        assertEquals(viewResponse.jsonPath().getString("countTs"), "1");
        assertEquals(viewResponse.jsonPath().getString("countParts"), "2");

        // Изменение ДТП
        String editDTPJson = String.format(
                "{\"id\": 99999, \"description\": \"Testing dtp_2\", \"datetime\": \"2022-01-01T20:00:00.000Z\", \"coordL\": 50, \"coordW\": 51, \"osv\": \"Temnoe vremya\", \"countTs\": 1, \"countParts\": 2}");
        Response editResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .body(editDTPJson) // Передаем тело запроса в формате JSON
                .put("/dtps/99999"); // Выполняем GET запрос на /viewDTP

        assertEquals(200, editResponse.getStatusCode());
        // Проверка ДТП
        viewResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .get("/dtps/99999"); // Выполняем GET запрос на /viewDTP
        assertEquals(200, viewResponse.getStatusCode());
        assertEquals(viewResponse.jsonPath().getString("description"), "Testing dtp_2");
        assertEquals(viewResponse.jsonPath().getString("osv"), "Temnoe vremya");
        assertEquals(viewResponse.jsonPath().getString("countTs"), "1");
        assertEquals(viewResponse.jsonPath().getString("countParts"), "2");


        // удаление ДТП (по /dtps/id DELETE)
        delResponse = RestAssured.given().baseUri("http://localhost") // Указываем базовый URI
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .header("Authorization", "Bearer " + token) // Добавляем заголовок Authorization с Bearer null
                .delete("/dtps/99999"); // Выполняем GET запрос на /viewDTP

        assertEquals(200, delResponse.getStatusCode());
    }
}

