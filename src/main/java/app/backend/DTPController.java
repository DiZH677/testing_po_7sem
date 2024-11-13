package app.backend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import entities.DTP;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.DTPParams;
import services.DTPService;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DTPController {
    private final DTPService dtpService;

    private static String readRequestBody(InputStream requestBody) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requestBody))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    public DTPController(DTPService dtpService) {
        this.dtpService = dtpService;
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

    private AuthorizationResult validateAuthorization(HttpExchange t) {
        AuthorizationResult resAuth = new AuthorizationResult();
        // Если нет заголовка авторизации
        Headers headers = t.getRequestHeaders();
        List<String> authHeaders = headers.get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            CustomLogger.logError("Invalid request", DTPController.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        // Если нет токена
        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            CustomLogger.logError("Invalid request", DTPController.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        // Проверка токена
        String token = authHeader.substring(7);
        boolean res = JwtExample.validateToken(token);
        if (!res) {
            CustomLogger.logError("Invalid request", DTPController.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        resAuth.setValid(true);
        resAuth.setUserUI(getUser(token));

        return resAuth;
    }

    public JsonObject viewDTP(UserUI usr, DTPParams params) {
        List<DTP> dtpsBL = null;
        try {
            dtpsBL = dtpService.getDTPsByParams(usr.getId(), params);
        } catch (Exception e) {
            dtpsBL = null;
        }
        List<DTPUI> dtps = dtpsBL != null ? dtpsBL.stream()
                .map(DTPUI::new) // Используем конструктор DTPUI(DTP)
                .collect(Collectors.toList())
                : null;

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        if (dtps != null)
            for (DTPUI dtp : dtps) {
                JsonObject dtpJsonObject = gson.toJsonTree(dtp, DTPUI.class).getAsJsonObject();
                jsonObject.add("dtp" + (dtps.indexOf(dtp) + 1), dtpJsonObject);
            }
        return jsonObject;
    }

    private void handleRequest(HttpExchange t, String response, Integer rCode) throws IOException {
        t.sendResponseHeaders(rCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    public static Map<String, String> parseQueryParameters(String requestUri) {
        Map<String, String> queryParameters = new HashMap<>();
        String query = requestUri.substring(requestUri.indexOf('?') + 1);

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            try {
                queryParameters.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return queryParameters;
    }

    public void handleViewDTPRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            handleRequest(t, "Authorization failed", 400);
            return;
        }

        // Парсинг параметров
        DTPParams params = parseRequestParams(t);

        // Обработка ролей
        if (isGuestWithoutPermission(resAuth, params)) {
            handleRequest(t, "No permission for date range", 400);
            return;
        }

        // Получение списка ДТП
        JsonObject dtps = fetchDTPList(resAuth, params);
        if (dtps == null) {
            handleRequest(t, "Failed to fetch DTPs", 500);
            return;
        }

        // Ответ
        handleRequest(t, new Gson().toJson(dtps), 200);
    }

    private DTPParams parseRequestParams(HttpExchange t) {
        Map<String, String> queryParameters = parseQueryParameters(t.getRequestURI().toString());
        DTPParams params = new DTPParams();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");

        // Parse idBegin and idEnd
        params.dtpIdBegin = Integer.parseInt(queryParameters.get("idBegin"));
        params.dtpIdEnd = Integer.parseInt(queryParameters.get("idEnd"));

        // Parse dateBegin and dateEnd
        try {
            if (queryParameters.get("dateBegin") != null && !queryParameters.get("dateBegin").isEmpty()) {
                params.dtpBegin = sdf.parse(URLDecoder.decode(queryParameters.get("dateBegin"), StandardCharsets.UTF_8));
            }
            if (queryParameters.get("dateEnd") != null && !queryParameters.get("dateEnd").isEmpty()) {
                params.dtpEnd = sdf.parse(URLDecoder.decode(queryParameters.get("dateEnd"), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            params.dtpBegin = null;
            params.dtpEnd = null;
        }

        return params;
    }

    private boolean isGuestWithoutPermission(AuthorizationResult resAuth, DTPParams params) {
        return resAuth.getUserUI().getRole().equals("Guest") && (params.dtpBegin != null || params.dtpEnd != null);
    }

    private JsonObject fetchDTPList(AuthorizationResult resAuth, DTPParams params) {
        DTPController dtpController = new DTPController(dtpService);
        return dtpController.viewDTP(resAuth.getUserUI(), params);
    }

    private void handleRequest(HttpExchange t, String response, int statusCode) throws IOException {
            t.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }

    public boolean addDTP(UserUI usr, DTPUI dtp) {
        String date = dtp.getDatetime();
        String desiredFormat = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
        String formattedDateTime = localDateTime.format(DateTimeFormatter.ofPattern(desiredFormat));
        dtp.setDatetime(formattedDateTime);


        DTP dtpBL = dtp.getDTP();
        boolean res;
        try {
            res = dtpService.addDTP(usr.getId(), dtpBL);
        } catch (AccessDeniedException | RepositoryException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    public void handleAddDTPRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            CustomLogger.logError("resAuth is not valid (errors with token)", DTPController.class.getSimpleName());
            handleRequest(t, "resAuth is not valid (errors with token)", 400);
            return;
        }
        // Обработка ролей
        if (resAuth.getUserUI().getRole().equals("Guest")) {
            String response = "Error: you do not have this permissions";
            handleRequest(t, response, 400);
            return;
        }

        // Парсинг параметров
        String requestBody = readRequestBody(t.getRequestBody()); // Замените Utils.readRequestBody() на свою реализацию

        // Создаем объект DTPUI с полученными данными и списком автомобилей
        DTPUI dtpui = DTPUIConverter.fromJson(requestBody);

        // Добавление ДТП
        DTPController dtpController = new DTPController(dtpService);
        boolean res = dtpController.addDTP(resAuth.getUserUI(), dtpui);
        // Ответ
        if (res) {
            String response = "DTP was added successfully";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 200);
        }
        else {
            String response = "DTP wasn't added";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 500);
        }
    }

    public boolean editDTP(UserUI usr, DTPUI dtp) {
        String date = dtp.getDatetime();
        String desiredFormat = "yyyy-MM-dd HH:mm:ss";
        String formattedDateTime = date;
//        try {
//            OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
//            formattedDateTime = offsetDateTime.atZoneSameInstant(ZoneOffset.UTC)
//                    .format(DateTimeFormatter.ofPattern(desiredFormat));
//        } catch (Exception ignored) {}
        try {
            // Пробуем парсить как OffsetDateTime
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
            formattedDateTime = offsetDateTime.atZoneSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern(desiredFormat));
        } catch (Exception e1) {
            try {
                // Если не получилось, пробуем парсить как LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(date);
                formattedDateTime = localDateTime.atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern(desiredFormat));
            } catch (Exception e2) {
                // Обработка ошибок парсинга
                e2.printStackTrace();
            }
        }

        dtp.setDatetime(formattedDateTime);


        DTP dtpBL = dtp.getDTP();
        boolean res;
        try {
            res = dtpService.editDTP(usr.getId(), dtpBL);
        } catch (AccessDeniedException | RepositoryException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    public void handleGetDTPRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            CustomLogger.logError("resAuth is not valid (errors with token)", DTPController.class.getSimpleName());
            handleRequest(t, "resAuth is not valid (errors with token)", 400);
            return;
        }
        // Парсинг параметров
        Integer dtpId = Integer.valueOf(t.getRequestURI().getPath().replaceAll("^/dtps/", ""));
        if (dtpId == null) {
            String response = "Invalid request: wrong parametrs";
            CustomLogger.logError(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 500);
            return;
        }
        // Обработка ролей
        // ... В данном случае отсутствует
        // Получение списка ДТП
        DTPController dtpController = new DTPController(dtpService);
        DTPUI dtp = dtpController.getDTP(resAuth.getUserUI(), Integer.valueOf(dtpId));
        if (dtp == null) {
            String response = "Invalid request: dtp was null";
            CustomLogger.logError(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 500);
            return;
        }
        String response = new Gson().toJson(dtp);
        handleRequest(t, response, 200);
    }

    public boolean delDTP(UserUI usr, Integer dtp_id) {
        boolean res;
        try {
            res = dtpService.deleteDTP(usr.getId(), dtp_id);
        } catch (AccessDeniedException | RepositoryException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    public void handleEditDTPRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            CustomLogger.logError("resAuth is not valid (errors with token)", DTPController.class.getSimpleName());
            handleRequest(t, "resAuth is not valid (errors with token)", 400);
            return;
        }
        // Обработка ролей
        if (resAuth.getUserUI().getRole().equals("Guest")) {
            String response = "Error: you do not have this permissions";
            handleRequest(t, response, 400);
            return;
        }

        // Парсинг параметров
        String requestBody = readRequestBody(t.getRequestBody()); // Замените Utils.readRequestBody() на свою реализацию
        // Создаем объект DTPUI с полученными данными и списком автомобилей
        DTPUI dtpui = DTPUIConverter.fromJson(requestBody);
        // Редактирование ДТП
        DTPController dtpController = new DTPController(dtpService);
        boolean res = dtpController.editDTP(resAuth.getUserUI(), dtpui);
        // Ответ
        if (res) {
            String response = "DTP was added successfully";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 200);
        }
        else {
            String response = "DTP wasn't added";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 500);
        }
    }

    public DTPUI getDTP(UserUI usr, Integer id) {
        DTP dtp;
        try {
            dtp = dtpService.getDTP(usr.getId(), id);
            if (dtp == null)
                return null;
            return new DTPUI(dtp);
        } catch (AccessDeniedException | RepositoryException ignored) {
            return null;
        }
    }

    public void handleDelDTPRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            CustomLogger.logError("resAuth is not valid (errors with token)", DTPController.class.getSimpleName());
            handleRequest(t, "resAuth is not valid (errors with token)", 400);
            return;
        }
        // Обработка ролей
        if (resAuth.getUserUI().getRole().equals("Guest")) {
            String response = "Error: you do not have this permissions";
            handleRequest(t, response, 400);
            return;
        }

        // Парсинг параметров
        Integer dtpId = Integer.valueOf(t.getRequestURI().getPath().replaceAll("^/dtps/", ""));

        // Удаление ДТП
        DTPController dtpController = new DTPController(dtpService);
        boolean res = dtpController.delDTP(resAuth.getUserUI(), dtpId);
        // Ответ
        if (res) {
            String response = "DTP was deleted successfully";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 200);
        }
        else {
            String response = "DTP wasn't deleted";
            CustomLogger.logInfo(response, DTPController.class.getSimpleName());
            handleRequest(t, response, 500);
        }
    }
}
