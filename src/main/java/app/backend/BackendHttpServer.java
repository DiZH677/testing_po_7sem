package app.backend;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import IRepositories.IUserRepository;
import app.console.ConsoleApp;
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
import java.util.List;
import java.util.Scanner;

public class BackendHttpServer {
    private static final Scanner scanner = new Scanner(System.in);

    private static MongoDBConnectionManager mngrMongo;
    private static PostgresConnectionManager mngr;
    private static DTPService dtpService;
    private static UserService usrService;
    private static ReportService reportService;
    private static User usr;

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
                CustomLogger.logError(response, ConsoleApp.class.getSimpleName());
                handleRequest(t, response, 400);
            }

            CustomLogger.logInfo("Parsing json...", ConsoleApp.class.getSimpleName());
            JsonObject jsonObject = parseJsonRequest(t);
            CustomLogger.logInfo("Json parsed", ConsoleApp.class.getSimpleName());

            if (jsonObject == null || !jsonObject.has("login") || !jsonObject.has("password")) {
                if (jsonObject == null)
                    CustomLogger.logError("Invalid body request... json object is null", ConsoleApp.class.getSimpleName());
                else
                    CustomLogger.logError("Invalid body request... " + jsonObject.getAsString(), ConsoleApp.class.getSimpleName());
                String response = "Invalid request (no json or login/password in it)";
                CustomLogger.logError(response, ConsoleApp.class.getSimpleName());
                handleRequest(t, response, 400);
                return;
            }

            String login = jsonObject.get("login").getAsString();
            String password = jsonObject.get("password").getAsString();
            String twoFactorCode;
            User usrBL = usrService.getUserLP(login, password);
            if (usrBL == null) {
                String response = "User is null";
                CustomLogger.logError(response, ConsoleApp.class.getSimpleName());
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

        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestPath = t.getRequestURI().getPath();
            CustomLogger.logInfo("User input path: " + requestPath, ConsoleApp.class.getSimpleName());

            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Добавление "Authorization"
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD, PUT, DELETE");
            t.getResponseHeaders().add("Content-Type", "application/json");

            String requestMethod = t.getRequestMethod();

            if ("/auth".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("POST")) {
                    try {
                        handleAuthRequest(t);
                    } catch (RepositoryException e) {
                        CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
                        handleRequest(t, "Invalid request", 400);
                    }
                }
            } else if ("/auth/verify-2fa".equals(requestPath) && "POST".equals(requestMethod)) {
                try {
                    handleVerify2FARequest(t);
                } catch (RepositoryException e) {
                    CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
                    handleRequest(t, "Invalid request", 400);
                }
            } else if ("/user".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("GET")) {
                    handleUserRequest(t);
                } else if (requestMethod.equals("PUT")) {
                    handleUpdateUserRequest(t);
                } else if (requestMethod.equals("DELETE")) {
                    handleDelUserRequest(t);
                }
            } else if ("/user/confirm-password".equals(requestPath) && "PUT".equals(requestMethod)) {
                handleConfirmPasswordChange(t);
            } else if ("/viewDTP".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("GET")) {
                    handleViewDTPRequest(t);
                }
            } else if ("/addDTP".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("POST")) {
                    handleAddDTPRequest(t);
                }
            } else if (requestPath.matches("^/dtps/\\d+$")) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("GET")) {
                    handleGetDTPRequest(t);
                } else if (requestMethod.equals("PUT")) {
                    handleEditDTPRequest(t);
                } else if (requestMethod.equals("DELETE")) {
                    handleDelDTPRequest(t);
                }
            } else if ("/report".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("GET")) {
                    handleGetReportRequest(t);
                }
            } else if ("/register".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("POST")) {
                    handleRegisterRequest(t);
                }
            } else {
                CustomLogger.logInfo("Default request", ConsoleApp.class.getSimpleName());
                handleRequest(t, "Undefined request", 404);
            }
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

            // Извлекаем данные из запроса
            InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseReader(isr).getAsJsonObject();
            String twoFactorCode = json.get("twoFactorCode").getAsString();
            UserUI usrUI = parseUserFromRequestBody(t);
            User usr = new User();
            try {
                usr = usrService.getUserById(-5555, usrUI.getId());
            } catch (RepositoryException e) {
                response = "Error updating user";
                t.sendResponseHeaders(500, response.length());
            }
            String newPassword = TwoFactorService.getNewPasswordForUser(usr.getLogin());
            usr.setPassword(newPassword);

            // Проверяем 2FA код
            String savedCode = TwoFactorService.getCodeForUser(usr.getLogin());
            if (!(savedCode != null && savedCode.equals(twoFactorCode))) {
                response = "Invalid two-factor code";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            // Обновляем пароль
            try {
                if (usrService.editUser(-5555, usr)) {
                    response = "Password successfully updated";
                    t.sendResponseHeaders(200, response.length());
                } else {
                    response = "Failed to update password";
                    t.sendResponseHeaders(500, response.length());
                }
            } catch (AccessDeniedException e) {
                response = "Access denied";
                t.sendResponseHeaders(403, response.length());
            } catch (RepositoryException e) {
                response = "Error updating user";
                t.sendResponseHeaders(500, response.length());
            }

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
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
            mngr = PostgresConnectionManager.getInstance(host, "itcase_test", username, password);
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


