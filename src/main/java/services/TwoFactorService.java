package services;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class TwoFactorService {

    // Хранилище для временного хранения кодов (2FA) и паролей (можно заменить на базу данных или кэш)
    private static final Map<String, AbstractMap.SimpleEntry<String, String>> codes = new HashMap<>();

    // Хранилище для временного хранения нового пароля
    private static final Map<String, String> newPasswords = new HashMap<>();

    // Генерация случайного кода
    public static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Генерация кода от 100000 до 999999
        return String.valueOf(code);
    }

    // Сохранение кода для конкретного пользователя
    public static void saveCodeForUser(String email, String code, String password) {
        codes.put(email, new AbstractMap.SimpleEntry<>(code, password));
    }

    // Получение кода для конкретного пользователя
    // Метод для получения кода 2FA по email
    public static String getCodeForUser(String email) {
        AbstractMap.SimpleEntry<String, String> entry = codes.get(email);
        return entry != null ? entry.getKey() : null;
    }

    // Метод для получения пароля по email
    public static String getPasswordForUser(String email) {
        AbstractMap.SimpleEntry<String, String> entry = codes.get(email);
        return entry != null ? entry.getValue() : null;
    }

    // Удаление кода после использования
    public static void removeCodeForUser(String email) {
        codes.remove(email);
    }

    // Сохранение нового пароля, ожидающего подтверждения
    public static void saveNewPasswordForUser(String email, String newPassword) {
        newPasswords.put(email, newPassword);
    }

    // Получение нового пароля для пользователя
    public static String getNewPasswordForUser(String email) {
        return newPasswords.get(email);
    }

    // Отправка 2FA-кода на email
    public static void sendTwoFactorCode(String email, String code, String msg) {
        // Настройки почтового сервера
        final String username = "island03@mail.ru";
        final String password = System.getenv("SECRET_PASSWORD_MAIL");

        // Настройка свойств для подключения
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.mail.ru");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", "465");
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });


        try {
            // Формируем email-сообщение
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username)); // Адрес отправителя
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email)); // Адрес получателя
            message.setSubject(msg); // Тема сообщения
            message.setText("Your authentication code is: " + code); // Текст сообщения

            // Отправляем email
            Transport.send(message);
            System.out.println("Two-factor code sent to " + email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return;
//        System.out.println("To check");
    }

//    private int abc() { return 0; }
}
