package app.backend;

import app.console.ConsoleApp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import exceptions.RepositoryException;
import logger.CustomLogger;
import services.UserService;
import user.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;

public class UserController {
    private UserService usrService;

    public UserController(UserService usrService) {
        this.usrService = usrService;
    }

    public boolean addUser(UserUI usr) {
        boolean res;
        User usrBl = usr.getUser();
        try {
            res = usrService.addUser(-5555, usrBl);
        } catch (AccessDeniedException | RepositoryException e) {
            res = false;
        }

        return res;
    }

    public void handleUserRequest(HttpExchange t) throws IOException {
        String response = "response";

        Headers headers = t.getRequestHeaders();
        List<String> authHeaders = headers.get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            t.sendResponseHeaders(400, response.length());
        }

        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            t.sendResponseHeaders(400, response.length());
        }

        String token = authHeader.substring(7);
        boolean res = JwtExample.validateToken(token);
        if (!res && !token.equals("null")) {
            UserUI usr = getUser(null);
            response = usr.toJson().toString();
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.close();
        }

        UserUI usr = getUser(token);
        response = usr.toJson().toString();
        t.sendResponseHeaders(200, response.length());

        OutputStream os = t.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
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

    private void handleRequest(HttpExchange t, String response, Integer rCode) throws IOException {
        t.sendResponseHeaders(rCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public void handleRegisterRequest(HttpExchange t) throws IOException {
        // Парсинг параметров
        JsonObject jsonObject = parseJsonRequest(t);
        String login = jsonObject.get("login").getAsString();
        String password = jsonObject.get("password").getAsString();
        String role = "User";
        // Регистрация пользователя
        UserController usrController = new UserController(usrService);
        UserUI usr = new UserUI(login, password, role);
        boolean res = usrController.addUser(usr);
        // Ответ
        if (!res) {
            String response = "User wasn't added";
            CustomLogger.logInfo(response, ConsoleApp.class.getSimpleName());
            handleRequest(t, response, 500);
        }
        else {
            String response = "User was added successfully";
            CustomLogger.logInfo(response, ConsoleApp.class.getSimpleName());
            handleRequest(t, response, 200);
        }
    }
}
