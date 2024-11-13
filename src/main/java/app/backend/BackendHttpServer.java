package app.backend;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import IRepositories.IUserRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import configurator.Configurator;
import exceptions.RepositoryException;
import logger.CustomLogger;
import report.ReportGenerator;
import repositories.mongodb.*;
import repositories.postgres.*;
import services.DTPService;
import services.ReportService;
import services.TwoFactorService;
import services.UserService;
import user.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class BackendHttpServer {
    private static final Scanner scanner = new Scanner(System.in);

    private static MongoDBConnectionManager mngrMongo;
    private static PostgresConnectionManager mngr;
    private static DTPService dtpService;
    private static UserService usrService;
    private static ReportService reportService;
    private static User usr;

//    private void abc() {return; }

    public static void main(String[] args) throws Exception {
        configureAndConnectToDatabase();

        usr = usrService.getUserLP("", "");

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8000");
    }

    // засунуть в jwt токен айди пользователя (возможно и его роль)
    // обрабатывать роли в хендлерах
    // Сделать нормальную обработку ошибок
    // Можно удалить кэш (токен ключ, логин пароль значение)
    // Allow-Headers в одном методе
    // UserController && DTPController вместо обработчиков
    // Сделать модельки (дтп, пользователь, мб отчет. Внутри метод преобразования от бл, метод конвертации в джсон)
    static class MyHandler implements HttpHandler {
        private void handleAuthRequest(HttpExchange t) throws IOException, RepositoryException {
            if (!t.getRequestMethod().equals("POST")) {
                String response = "Invalid type request... Must be POST, has – " + t.getRequestMethod();
                CustomLogger.logError(response, BackendHttpServer.class.getSimpleName());
                handleRequest(t, response, 400);
            }

            CustomLogger.logInfo("Parsing json...", BackendHttpServer.class.getSimpleName());
            JsonObject jsonObject = parseJsonRequest(t);
            CustomLogger.logInfo("Json parsed", BackendHttpServer.class.getSimpleName());

            if (jsonObject == null || !jsonObject.has("login") || !jsonObject.has("password")) {
                if (jsonObject == null)
                    CustomLogger.logError("Invalid body request... json object is null", BackendHttpServer.class.getSimpleName());
                else
                    CustomLogger.logError("Invalid body request... " + jsonObject.getAsString(), BackendHttpServer.class.getSimpleName());
                String response = "Invalid request (no json or login/password in it)";
                CustomLogger.logError(response, BackendHttpServer.class.getSimpleName());
                handleRequest(t, response, 400);
                return;
            }

            String login = jsonObject.get("login").getAsString();
            String password = jsonObject.get("password").getAsString();
            String twoFactorCode;
            User usrBL = usrService.getUserLP(login, password);
            if (usrBL == null) {
                String response = "User is null";
                CustomLogger.logError(response, BackendHttpServer.class.getSimpleName());
                handleRequest(t, response, 400);
                return;
            }
            else {
                // Генерация и отправка 2FA-кода
                twoFactorCode = TwoFactorService.generateCode();
                TwoFactorService.saveCodeForUser(login, twoFactorCode, password);
                TwoFactorService.sendTwoFactorCode(login, twoFactorCode, "Your Two-Factor Authentication Code"); // Отправляем код на email пользователя

                // Отправляем ответ клиенту, что нужно ввести 2FA-код
                JsonObject answer = new JsonObject();
                answer.addProperty("answer", "Two-factor authentication required. Code sent to your email.");
                if (System.getenv("DTP_TESTING_TRUE") != null) {
                    answer.addProperty("code", twoFactorCode);
                }
                handleRequest(t, answer.toString(), 200);
            }
            UserUI usr = new UserUI(usrBL);
            JsonObject user = new JsonObject();
            JsonObject userDetails = usr.toJson();
            user.add("user", userDetails);

            String token = JwtExample.createToken(usr.getLogin(), usr.getRole(), usr.getId());

            user.addProperty("token", token);
            String response = user.toString();
            handleRequest(t, response, 200);
        }

        private void handleVerify2FARequest(HttpExchange t) throws IOException, RepositoryException {
            JsonObject jsonObject = parseJsonRequest(t);
            assert jsonObject != null;
            String login = jsonObject.get("login").getAsString();
            String code = jsonObject.get("code").getAsString();

            String savedCode = TwoFactorService.getCodeForUser(login);
            String savedPassword = TwoFactorService.getPasswordForUser(login);

            if (savedCode != null && savedCode.equals(code)) {
                // Успешная аутентификация, удаляем код из хранилища
                TwoFactorService.removeCodeForUser(login);
                User usr = usrService.getUserLP(login, savedPassword);

                // Создаем JWT-токен
                String token = JwtExample.createToken(login, usr.getRole(), usr.getId());

                // Возвращаем токен пользователю
                JsonObject response = new JsonObject();
                response.addProperty("token", token);
                handleRequest(t, response.toString(), 200);
            } else {
                handleRequest(t, "Invalid two-factor code", 401);
            }
        }

        @FunctionalInterface
        public interface CheckedConsumer<T> {
            void accept(T t) throws IOException, RepositoryException;
        }

        public <T> Consumer<T> wrapChecked(CheckedConsumer<T> checkedConsumer) {
            return t -> {
                try {
                    checkedConsumer.accept(t);
                } catch (IOException e) {
                    CustomLogger.logError("IO Exception in handler", BackendHttpServer.class.getSimpleName());
                } catch (RepositoryException e) {
                    CustomLogger.logError("RepositoryException in handler", BackendHttpServer.class.getSimpleName());
                }
            };
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestPath = t.getRequestURI().getPath();
            String requestMethod = t.getRequestMethod();

            // Настройка заголовков
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD, PUT, DELETE");
            t.getResponseHeaders().add("Content-Type", "application/json");

            // Инициализация карты маршрутов
            Map<String, Consumer<HttpExchange>> routes = new HashMap<>();
            routes.put("OPTIONS", wrapChecked(this::handleOptionsRequest));
            routes.put("/auth|POST", wrapChecked(this::handleAuthRequest));
            routes.put("/auth/verify-2fa|POST", wrapChecked(this::handleVerify2FARequest));
            routes.put("/user|GET", wrapChecked(this::handleUserRequest));
            routes.put("/user|PUT", wrapChecked(this::handleUpdateUserRequest));
            routes.put("/user|DELETE", wrapChecked(this::handleDelUserRequest));
            routes.put("/user/confirm-password|PUT", wrapChecked(this::handleConfirmPasswordChange));
            routes.put("/viewDTP|GET", wrapChecked(this::handleViewDTPRequest));
            routes.put("/addDTP|POST", wrapChecked(this::handleAddDTPRequest));
            routes.put("/dtps/\\d+|GET", wrapChecked(this::handleGetDTPRequest));
            routes.put("/dtps/\\d+|PUT", wrapChecked(this::handleEditDTPRequest));
            routes.put("/dtps/\\d+|DELETE", wrapChecked(this::handleDelDTPRequest));
            routes.put("/report|GET", wrapChecked(this::handleGetReportRequest));
            routes.put("/register|POST", wrapChecked(this::handleRegisterRequest));

            // Выбор и выполнение обработчика
            Consumer<HttpExchange> handler = routes.getOrDefault(requestPath + "|" + requestMethod, exchange -> {
                CustomLogger.logInfo("Default request", DTPController.class.getSimpleName());
                try {
                    handleRequest(exchange, "Undefined request", 404);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            handler.accept(t);
        }

        private void handleDelUserRequest(HttpExchange t) {
            String response = "Invalid request";

            // Чтение данных из тела запроса (в частности, ID пользователя для удаления)
            try (InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(isr).getAsJsonObject();
                int usrid = json.get("usrid").getAsInt();  // ID пользователя, который отправил запрос
                int id = json.get("id").getAsInt();        // ID пользователя, которого нужно удалить

                // Проверка, не пытается ли пользователь удалить свою учетную запись
                if (usrid == id) {
                    response = "You cannot delete your own account.";
                    t.sendResponseHeaders(400, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    return;
                }

                // Попытка удалить пользователя
                boolean result = usrService.delUser(usrid, id);
                if (result) {
                    response = "User successfully deleted.";
                    t.sendResponseHeaders(200, response.length());
                } else {
                    response = "Failed to delete user.";
                    t.sendResponseHeaders(500, response.length());
                }

                OutputStream os = t.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
            } catch (IOException | RepositoryException e) {
                response = "Invalid request data.";
                try {
                    t.sendResponseHeaders(400, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void handleUpdateUserRequest(HttpExchange t) throws IOException {
            String response = "Invalid request";
            Headers headers = t.getRequestHeaders();
            List<String> authHeaders = headers.get("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                t.sendResponseHeaders(400, response.length());
                return;
            }

            String authHeader = authHeaders.get(0);
            if (!authHeader.toLowerCase().startsWith("bearer ")) {
                t.sendResponseHeaders(400, response.length());
                return;
            }

            String token = authHeader.substring(7);
            if (!JwtExample.validateToken(token)) {
                response = "Unauthorized";
                t.sendResponseHeaders(401, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            // Извлекаем пользователя из тела запроса
            UserUI usrUI = parseUserFromRequestBody(t);
            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseReader(isr).getAsJsonObject();
            String newPassword = json.get("newPassword").getAsString();
            String oldPassword = json.get("oldPassword").getAsString();
            User usr = new User();
            try {
                usr = usrService.getUserById(-5555, usrUI.getId());
            } catch (RepositoryException e) {
                response = "Error updating user";
                t.sendResponseHeaders(500, response.length());
            }

            // Проверка старого пароля
            boolean passwordVerified = usr.getPassword().equals(oldPassword);
            if (!passwordVerified) {
                response = "Old password is incorrect";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            // Отправка двухфакторного кода на почту
            String twoFactorCode = TwoFactorService.generateCode();
            TwoFactorService.saveCodeForUser(usr.getLogin(), twoFactorCode, usr.getPassword());
            TwoFactorService.sendTwoFactorCode(usr.getLogin(), twoFactorCode, "You are going to change password. This is code for changing it."); // Отправляем код на email пользователя
            TwoFactorService.saveNewPasswordForUser(usr.getLogin(), newPassword);

            // Ожидание подтверждения 2FA
            JsonObject answer = new JsonObject();
            answer.addProperty("answer", "Password change requested. Please confirm the change via the email.");
            if (System.getenv("DTP_TESTING_TRUE") != null) {
                answer.addProperty("code", twoFactorCode);
            }
            handleRequest(t, answer.toString(), 200);
        }


        public void handleConfirmPasswordChange(HttpExchange t) throws IOException {
            String response = "Invalid request";

            // Проверяем авторизацию
            if (!isAuthorized(t)) {
                response = "Unauthorized";
                sendResponse(t, 401, response);
                return;
            }

            // Извлекаем данные из запроса
            JsonObject json = getRequestBody(t);
            String twoFactorCode = json.get("twoFactorCode").getAsString();
            UserUI usrUI = parseUserFromRequestBody(t);

            // Получаем пользователя
            User usr = getUser(usrUI.getId());
            if (usr == null) {
                response = "Error updating user";
                sendResponse(t, 500, response);
                return;
            }

            // Проверяем 2FA код
            if (!validateTwoFactorCode(usr, twoFactorCode)) {
                response = "Invalid two-factor code";
                sendResponse(t, 400, response);
                return;
            }

            // Обновляем пароль
            response = updatePassword(usr);
            sendResponse(t, response.equals("Password successfully updated") ? 200 : 500, response);
        }

        private boolean isAuthorized(HttpExchange t) {
            Headers headers = t.getRequestHeaders();
            List<String> authHeaders = headers.get("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                return false;
            }

            String authHeader = authHeaders.get(0);
            return authHeader.toLowerCase().startsWith("bearer ") && JwtExample.validateToken(authHeader.substring(7));
        }

        private JsonObject getRequestBody(HttpExchange t) throws IOException {
            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
            return JsonParser.parseReader(isr).getAsJsonObject();
        }

        private User getUser(int userId) {
            try {
                return usrService.getUserById(-5555, userId);
            } catch (RepositoryException e) {
                return null;
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean validateTwoFactorCode(User usr, String twoFactorCode) {
            String savedCode = TwoFactorService.getCodeForUser(usr.getLogin());
            return savedCode != null && savedCode.equals(twoFactorCode);
        }

        private String updatePassword(User usr) throws IOException {
            String newPassword = TwoFactorService.getNewPasswordForUser(usr.getLogin());
            usr.setPassword(newPassword);

            try {
                if (usrService.editUser(-5555, usr)) {
                    return "Password successfully updated";
                } else {
                    return "Failed to update password";
                }
            } catch (AccessDeniedException e) {
                return "Access denied";
            } catch (RepositoryException e) {
                return "Error updating user";
            }
        }

        private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
            t.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private void handleOptionsRequest(HttpExchange t) throws IOException {
            String response = "HTTP method allowed: GET, POST, DELETE, OPTIONS, HEAD, PUT,";
            t.sendResponseHeaders(200, response.length());

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

        // Вызов при создании веб-страницы (инициализации юзера)
        private void handleUserRequest(HttpExchange t) throws IOException {
            UserController usrController = new UserController(usrService);
            usrController.handleUserRequest(t);
        }

        private void handleRegisterRequest(HttpExchange t) throws IOException {
            UserController usrController = new UserController(usrService);
            usrController.handleRegisterRequest(t);
        }

        private void handleViewDTPRequest(HttpExchange t) throws IOException {
            DTPController dtpController = new DTPController(dtpService);
            dtpController.handleViewDTPRequest(t);
        }

        private void handleAddDTPRequest(HttpExchange t) throws IOException {
            DTPController dtpController = new DTPController(dtpService);
            dtpController.handleAddDTPRequest(t);
        }

        private void handleGetDTPRequest(HttpExchange t) throws IOException {
            DTPController dtpController = new DTPController(dtpService);
            dtpController.handleGetDTPRequest(t);
        }

        private void handleEditDTPRequest(HttpExchange t) throws IOException {
            DTPController dtpController = new DTPController(dtpService);
            dtpController.handleEditDTPRequest(t);
        }

        private void handleDelDTPRequest(HttpExchange t) throws IOException {
            DTPController dtpController = new DTPController(dtpService);
            dtpController.handleDelDTPRequest(t);
        }

        private void handleGetReportRequest(HttpExchange t) throws IOException {
            ReportController repController = new ReportController(reportService, dtpService);
            repController.handleGetReportRequest(t);
        }

        private void handleRequest(HttpExchange t, String response, Integer rCode) throws IOException {
            t.sendResponseHeaders(rCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    public static void configureAndConnectToDatabase() {
        String host = Configurator.getValue("db.url");
        String database = Configurator.getValue("db.dbname");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");
        String typeDB = Configurator.getValue("db.type");

        String dbName = System.getenv("DTP_TESTING_TRUE") != null ? "itcase_test" : null;
        System.out.println("DTP_TESTING_TRUE: " + System.getenv("DTP_TESTING_TRUE"));
        if (dbName != null) {
            System.out.println("SERVER RELATED TO TEST DATABASE");
            database = dbName;
        }


        // Используем полученные значения для установки соединения с базой данных
        IDTPRepository dtpRep = null;
        ICarRepository carRep = null;
        IParticipantRepository prtRep = null;
        IUserRepository usrRep = null;
        if (typeDB.equals("postgres"))
        {
            mngr = PostgresConnectionManager.getInstance(host, database, username, password);
//            String schema = System.getProperty("testSchema");
//            if (schema != null) {
//                mngr.setSearchPath(schema);
//            }
//            mngr = PostgresConnectionManager.getInstance(host, database, username, password);
            dtpRep = new PostgresDTPRepository(mngr);
            carRep = new PostgresCarRepository(mngr);
            prtRep = new PostgresParticipantRepository(mngr);
            usrRep = new PostgresUserRepository(mngr);
        }
        else if (typeDB.equals("mongo"))
        {
            mngrMongo = MongoDBConnectionManager.getInstance("localhost:27017", database, username, "123");
            dtpRep = new MongoDTPRepository(mngrMongo);
            carRep = new MongoCarRepository(mngrMongo);
            prtRep = new MongoParticipantRepository(mngrMongo);
            usrRep = new MongoUserRepository(mngrMongo);
        }

        usrService = new UserService(usrRep);
        dtpService = new DTPService(dtpRep, carRep, prtRep, usrService);
        ReportGenerator repGen = new ReportGenerator();
        reportService = new ReportService(dtpService, repGen);
    }

    public static UserUI getUser(String token) {
        UserUI usrUI = new UserUI();
        if (token != null && !token.equals("null")) {
            Integer a = JwtExample.getUserID(token);
            usrUI.setId(JwtExample.getUserID(token));
            usrUI.setRole(JwtExample.getUserRole(token));
        }
        else {
            usrUI.setId(0);
            usrUI.setRole("Guest");
        }

        return usrUI;
    }

    public static JsonObject parseJsonRequest(HttpExchange t) throws IOException {
        JsonParser parser = new JsonParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(t.getRequestBody()));
        try {
            return parser.parse(reader).getAsJsonObject();
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            return null;
        } finally {
            reader.close();
        }
    }

    private static UserUI parseUserFromRequestBody(HttpExchange t) throws IOException {
        Headers headers = t.getRequestHeaders();
        List<String> authHeaders = headers.get("Authorization");
        String token = "null";
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                // Убираем "Bearer " и пробел
                token = authHeader.substring(7).trim(); // Срезаем первые 7 символов ("Bearer ") и удаляем пробелы
            }
        }

        return getUser(token);
    }

}


