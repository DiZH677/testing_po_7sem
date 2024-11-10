package app.backend;

import app.console.ConsoleApp;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.DTPParams;
import params.Params;
import services.ReportService;
import services.DTPService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportController {
    private ReportService repService;
    private DTPService dtpService;

    public ReportController(ReportService repService, DTPService dtpService) {
        this.repService = repService;
        this.dtpService = dtpService;
    }

    public boolean save(UserUI usr, String fname, String format, Params params) {
        boolean res;
        try {
            res = repService.save(usr.getId(), fname, format, params);
        } catch (AccessDeniedException | RepositoryException e) {
            res = false;
        }

        return res;
    }

    public byte[] getReport(UserUI usr, String format, Params params) {
        byte[] res;
        try {
            res = repService.get(usr.getId(), format, params);
        } catch (AccessDeniedException | RepositoryException e) {
            res = null;
        }

        return res;
    }

    public void handleGetReportRequest(HttpExchange t) throws IOException {
        // Валидация запроса
        AuthorizationResult resAuth = validateAuthorization(t);
        if (!resAuth.isValid()) {
            CustomLogger.logError("resAuth is not valid (errors with token)", ConsoleApp.class.getSimpleName());
            handleRequest(t, "resAuth is not valid (errors with token)", 400);
            return;
        }
        // Обработка ролей
        String role_user = resAuth.getUserUI().getRole();
        if (!role_user.equals("Analyst")) {
            String response = "Error: you do not have this permissions";
            handleRequest(t, response, 400);
            return;
        }

        // Парсинг параметров
        Map<String, String> queryParameters = parseQueryParameters(t.getRequestURI().toString());
        DTPParams paramsDTP = new DTPParams();
        String fname = queryParameters.get("fname");
        String format = queryParameters.get("format");
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        paramsDTP.dtpIdBegin = Integer.parseInt(queryParameters.get("idBegin"));
        paramsDTP.dtpIdEnd = Integer.parseInt(queryParameters.get("idEnd"));
        try {
            if (queryParameters.get("dateBegin") != null)
            {
                paramsDTP.dtpBegin = sdf.parse(URLDecoder.decode(queryParameters.get("dateBegin"), StandardCharsets.UTF_8));
            }
            if (queryParameters.get("dateBegin") != null)
            {
                paramsDTP.dtpEnd = sdf.parse(URLDecoder.decode(queryParameters.get("dateEnd"), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            paramsDTP.dtpBegin = null;
            paramsDTP.dtpEnd = null;
        }
        Params params = new Params();
        params.exportDTP = true;
        params.dtpps = paramsDTP;

        // Отчет по ДТП
        ReportController repController = new ReportController(repService, dtpService);
        byte[] res = repController.getReport(resAuth.getUserUI(), "json", params);
        // Ответ
        if (res == null) {
            String response = "File wasn't generated";
            CustomLogger.logInfo(response, ConsoleApp.class.getSimpleName());
            handleRequest(t, response, 500);
        }
        else {
            String response = "File was downloaded successfully";
            CustomLogger.logInfo(response, ConsoleApp.class.getSimpleName());
            t.getResponseHeaders().set("Content-Type", "application/octet-stream");
            t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fname + "." + format + "\"");
            t.sendResponseHeaders(200, res.length);
            OutputStream os = t.getResponseBody();
            os.write(res);
            os.close();
        }
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

    private AuthorizationResult validateAuthorization(HttpExchange t) {
        AuthorizationResult resAuth = new AuthorizationResult();
        // Если нет заголовка авторизации
        Headers headers = t.getRequestHeaders();
        List<String> authHeaders = headers.get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        // Если нет токена
        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        // Проверка токена
        String token = authHeader.substring(7);
        boolean res = JwtExample.validateToken(token);
        if (!res) {
            CustomLogger.logError("Invalid request", ConsoleApp.class.getSimpleName());
            resAuth.setValid(false);
            return resAuth;
        }
        resAuth.setValid(true);
        resAuth.setUserUI(getUser(token));

        return resAuth;
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
}
