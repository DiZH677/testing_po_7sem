package app.backend;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import IRepositories.IUserRepository;
import app.console.ConsoleApp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import services.UserService;
import user.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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

            User usrBL = usrService.getUserLP(login, password);
            if (usrBL == null) {
                String response = "User is null";
                CustomLogger.logError(response, ConsoleApp.class.getSimpleName());
                handleRequest(t, response, 400);
                return;
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
                } else {
                    CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
                    handleRequest(t, "Invalid request", 400);
                }
            } else if ("/user".equals(requestPath)) {
                if (requestMethod.equals("OPTIONS")) {
                    handleOptionsRequest(t);
                } else if (requestMethod.equals("GET")) {
                    handleUserRequest(t);
                }
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

}


