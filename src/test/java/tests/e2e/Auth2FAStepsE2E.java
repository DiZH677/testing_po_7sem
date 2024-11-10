package tests.e2e;

import io.cucumber.java.After;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.restassured.RestAssured;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

public class Auth2FAStepsE2E {

    private String email;
    private String password;
    private String newPassword;
    private String twoFactorCode;
    private String jwtToken;
    private Integer id;

    @Given("a user with email {string} and password {string} exists")
    public void aUserWithEmailAndPasswordExists(String email, String password) {
        this.email = email;
        this.password = password;
        this.id = 5555;
        Response answer = regUser(email, password);
        System.out.println(answer.statusCode());
        System.out.println(answer.jsonPath());
        // Создаём тестового пользователя или проверяем его существование
    }

    @When("the user logs in with email {string} and password {string}")
    public void theUserLogsInWithEmailAndPassword(String email, String password) {
        // Логинимся через UserService или через API
        // Отправляем код 2FA на email
        Response response = simulateLogin(email, password);
        twoFactorCode = response.jsonPath().getString("code");
        assertEquals(200, response.getStatusCode());
    }

    @Then("a two-factor authentication code is sent to {string}")
    public void aTwoFactorAuthenticationCodeIsSent(String email) {
        assertNotNull(twoFactorCode);
    }

    @When("the user enters the two-factor code received on email")
    public void theUserEntersTheTwoFactorCodeReceivedOnEmail() {
        // Проверяем код на сервере или через API
        Response response = verifyTwoFactorCode(email, twoFactorCode);
        jwtToken = response.jsonPath().getString("token");
        assertEquals(200, response.getStatusCode());
    }

    @Then("the user is successfully authenticated")
    public void theUserIsSuccessfullyAuthenticated() {
        // Проверка успешной аутентификации, например, получаем токен
        assertNotNull(jwtToken);
    }


    @And("the user is authenticated with {string} and password {string} with two-factor authentication")
    public void theUserIsAuthenticatedWithAndPasswordWithTwoFactorAuthentication(String email, String password) {
        theUserLogsInWithEmailAndPassword(email, password);
        aTwoFactorAuthenticationCodeIsSent(email);
        theUserEntersTheTwoFactorCodeReceivedOnEmail();
        theUserIsSuccessfullyAuthenticated();
    }

    @When("the user {string} requests to change the password from {string} to {string}")
    public void theUserRequestsToChangeThePasswordTo(String email, String oldPassword, String newPassword) {
        // Запрос на изменение пароля
        Response response = requestToChangePassword(email, oldPassword, newPassword);
        twoFactorCode = response.jsonPath().getString("code");
        assertEquals(200, response.getStatusCode());
    }

    @When("the user confirms the password change with email {string} and two-factor code")
    public void theUserConfirmsThePasswordChangeWithEmailAndTwoFactorCode(String email) {
        // Отправка запроса на подтверждение смены пароля
        Response response = requestToConfirmPasswordChange(email, twoFactorCode);
        assertEquals(200, response.getStatusCode());
    }

    @Then("the password for the user is successfully updated to {string}")
    public void thePasswordForTheUserIsSuccessfullyUpdatedTo(String newPassword) {
        // Проверка обновления пароля в базе данных
    }

    @Then("the user can log in with email {string} and the new password {string}")
    public void theUserCanLogInWithEmailAndTheNewPassword(String email, String newPassword) {
        // Авторизация с новым паролем
        theUserLogsInWithEmailAndPassword(email, newPassword);
        aTwoFactorAuthenticationCodeIsSent(email);
        theUserEntersTheTwoFactorCodeReceivedOnEmail();
        theUserIsSuccessfullyAuthenticated();
    }

    @And("the user cannot log in with email {string} and the old password {string}")
    public void theUserCannotLogInWithEmailAndTheOldPassword(String email, String password) {
        Response response = simulateLogin(email, password);
        assertNotEquals(200, response.getStatusCode());
    }

    @After
    public void deleteTestUser() {
        // Выполнить удаление пользователя после каждого теста
        deleteUserById(this.id);
    }

    private void deleteUserById(Integer id) {
        // Реальный запрос на удаление пользователя через API
        String deletePayload = "{ \"usrid\": -5555, \"id\": \"" + id + "\" }";
        RestAssured.given()
                .baseUri("http://localhost")
                .port(8000)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken) // При необходимости передать токен
                .body(deletePayload)
                .delete("/user"); // Выполняем DELETE запрос на /user
    }

    // Регистрируем пользователя
    private Response regUser(String email, String password) {
        // Тут должен быть реальный запрос на авторизацию через API
        String authPayload = "{ \"login\": \"" + email + "\", \"password\": \"" + password + "\", \"id\": " + this.id + " }";
        return RestAssured.given().baseUri("http://localhost").port(8000)
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .body(authPayload) // Передаем тело запроса в формате JSON
                .post("/register"); // Выполняем POST запрос на /auth
    }

    // Моделируем логин (зависит от вашего API)
    private Response simulateLogin(String email, String password) {
        // Тут должен быть реальный запрос на авторизацию через API
        String authPayload = "{ \"login\": \"" + email + "\", \"password\": \"" + password + "\" }";
        return RestAssured.given().baseUri("http://localhost").port(8000)
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .body(authPayload) // Передаем тело запроса в формате JSON
                .post("/auth"); // Выполняем POST запрос на /auth
    }

    // Моделируем верификацию двухфакторного кода
    private Response verifyTwoFactorCode(String email, String code) {
        // Тут должен быть реальный запрос на верификацию двухфакторного кода
        String authPayload = "{ \"login\": \"" + email + "\", \"code\": \"" + code + "\" }";
        return RestAssured.given().baseUri("http://localhost").port(8000)
                .port(8000) // Указываем порт, на котором запущен ваш сервер
                .header("Content-Type", "application/json") // Указываем тип содержимого как JSON
                .body(authPayload) // Передаем тело запроса в формате JSON
                .post("/auth/verify-2fa"); // Выполняем POST запрос на /auth
    }

    private Response requestToChangePassword(String email, String oldPassword, String newPassword) {
        String changePasswordPayload = "{ \"email\": \"" + email + "\", \"oldPassword\": \"" + oldPassword + "\", \"newPassword\": \"" + newPassword + "\" }";
        return RestAssured.given()
                .baseUri("http://localhost") // Базовый URL сервера
                .port(8000) // Порт сервера
                .header("Content-Type", "application/json") // Заголовок с типом содержимого
                .header("Authorization", "Bearer " + jwtToken) // Добавляем JWT в заголовок Authorization
                .body(changePasswordPayload) // Тело запроса в формате JSON
                .put("/user"); // Выполняем PUT запрос на /user/password
    }

    private Response requestToConfirmPasswordChange(String email, String twoFactorCode) {
        String confirmPayload = "{ \"email\": \"" + email + "\", \"twoFactorCode\": \"" + twoFactorCode + "\" }";
        return RestAssured.given()
                .baseUri("http://localhost") // Базовый URL сервера
                .port(8000) // Порт сервера
                .header("Content-Type", "application/json") // Заголовок с типом содержимого
                .header("Authorization", "Bearer " + jwtToken) // Добавляем JWT в заголовок Authorization
                .body(confirmPayload) // Тело запроса в формате JSON
                .put("/user/confirm-password"); // Выполняем PUT запрос на /user/confirm-password
    }
}