package app.console;

import IRepositories.*;
import configurator.Configurator;
import entities.Car;
import entities.*;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.DTPParams;
import params.Params;
import report.*;
import repositories.postgres.*;
import repositories.postgres.PostgresConnectionManager;
import services.*;
import user.*;

import java.nio.file.AccessDeniedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static boolean isAnalyst = false;

    private static PostgresConnectionManager mngr;
    private static DTPService dtpService;
    private static UserService usrService;
    private static ReportService reportService;
    private static User usr;


    public static void main(String[] args) throws RepositoryException {
        configureAndConnectToDatabase();

        IRepositories.IDTPRepository dtpRep = new PostgresDTPRepository(mngr);
        ICarRepository carRep = new PostgresCarRepository(mngr);
        IParticipantRepository prtRep = new PostgresParticipantRepository(mngr);
        IUserRepository usrRep = new PostgresUserRepository(mngr);
        usrService = new UserService(usrRep);
        dtpService = new DTPService(dtpRep, carRep, prtRep, usrService);
        ReportGenerator repGen = new ReportGenerator();
        reportService = new ReportService(dtpService, repGen);

        usr = usrService.getUserLP("", "");
        showMenu();
    }

    private static void showMenu() {
        for (;;) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();
            handleChoice(choice);
            if (choice == 0)
                break;
        }
    }
    private static void handleChoice(int choice) {
        CustomLogger.logInfo("User input was " + choice, ConsoleApp.class.getSimpleName());
        if (!isLoggedIn && (choice > 1 && choice != 10) || !isAnalyst && choice > 10)  {
            System.out.println("Неверный выбор");
            CustomLogger.logWarning("User bad input", ConsoleApp.class.getSimpleName());
            return;
        }
        switch (choice) {
            // Просмотр ДТП
            case 1:
                try { findDTP(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for findDTP()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in findDTP()", ConsoleApp.class.getSimpleName());
                }

                break;
            // Добавить ДТП
            case 2:
                try { addDTP(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for addDTP()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in addDTP()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Изменить ДТП
            case 3:
                try { editDTP(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for editDTP()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in editDTP()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Удалить ДТП
            case 4:
                try { delDTP(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for delDTP()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in delDTP()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Отчет
            case 5:
                try { generateReport(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for generateReport()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in generateReport()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Просмотреть пользователей
            case 6:
                try { infoUsers(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for infoUsers()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in infoUsers()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Добавить пользователя
            case 7:
                try { addUser(); }
                catch (AccessDeniedException e) {
                    System.out.println("Недостаточно прав доступа");
                    CustomLogger.logWarning("Access denied for addUser()", ConsoleApp.class.getSimpleName());
                }
                catch (RepositoryException e) {
                    System.out.println("Ошибка на стороне БД");
                    CustomLogger.logError("RepositoryException in addUser()", ConsoleApp.class.getSimpleName());
                }
                break;
            // Удалить пользователя
            case 8:
                try { delUser(); }
                catch (AccessDeniedException e) { System.out.println("Недостаточно прав доступа"); CustomLogger.logWarning("Access denied for delUser()", ConsoleApp.class.getSimpleName()); }
                catch (IllegalArgumentException e) { System.out.println("Вы не можете удалить себя"); CustomLogger.logWarning("Error index for delUser()", ConsoleApp.class.getSimpleName());}
                catch (RepositoryException e) { System.out.println("Ошибка на стороне БД"); CustomLogger.logWarning("RepositoryException in delUser()", ConsoleApp.class.getSimpleName());}
                break;
            // Выход/Авторизация
            case 10:
                if (isLoggedIn)
                {
                    logout();
                    CustomLogger.logInfo("User logout", ConsoleApp.class.getSimpleName());
                }
                else {
                    try { login(); }
                    catch (RepositoryException e) { System.out.println("Ошибка на стороне БД"); CustomLogger.logError("RepositoryException in login()", ConsoleApp.class.getSimpleName());}
                    CustomLogger.logInfo("User has been logged", ConsoleApp.class.getSimpleName());
                }
                break;
            case 0:
                System.out.println("Завершение");
                mngr.closeConnection();
                CustomLogger.logInfo("Exit", ConsoleApp.class.getSimpleName());
                System.exit(0);
            default:
                System.out.println("Неверный выбор");
                CustomLogger.logWarning("User bad input", ConsoleApp.class.getSimpleName());
        }
    }
    private static void printMenu() {
        System.out.println();
        if (!isLoggedIn)
        {
            System.out.println("-----Меню   (Гость)-----");
            System.out.println("1. Просмотр информации о ДТП");
            System.out.println("10. Авторизация");
            System.out.println("0. Завершить");
        }
        else if (!isAnalyst)
        {
            System.out.println("-----Меню   (Пользователь)-----");
            System.out.println("1. Просмотр информации о ДТП");
            System.out.println("2. Добавить ДТП");
            System.out.println("3. Изменить ДТП");
            System.out.println("4. Удалить ДТП");
            System.out.println("10. Выйти");
            System.out.println("0. Завершить");
        }
        else {
            System.out.println("-----Меню   (Аналитик)-----");
            System.out.println("1. Просмотр информации о ДТП");
            System.out.println("2. Добавить ДТП");
            System.out.println("3. Изменить ДТП");
            System.out.println("4. Удалить ДТП");
            System.out.println("5. Выгрузить отчет");
            System.out.println("6. Просмотр информации о пользователях");
            System.out.println("7. Добавить пользователя");
            System.out.println("8. Удалить пользователя");
            System.out.println("10. Выйти");
            System.out.println("0. Завершить");
        }
        System.out.print("Введите номер пункта: ");
    }

    private static void logout() {
        System.out.println("Выход");
        isLoggedIn = false;
        isAnalyst = false;

    }
    private static void login() throws RepositoryException {
        // Ввод логина и пароля
        System.out.print("Логин: ");
        String lgn = scanner.nextLine();
        System.out.print("Пароль: ");
        String pswrd = scanner.nextLine();
        // Получение пользователя по логину и паролю
        usr = null;
        usr = usrService.getUserLP(lgn, pswrd);
        // Проверка пользователя
        if (usr != null) {
            System.out.println("Авторизация успешна");
            isLoggedIn = true;
            if (usr.getRole().equals("Analyst")) {
                isAnalyst = true;
            }
        } else {
            System.out.println("Ошибка авторизации. Неверный логин или пароль.");
        }
    }

    private static void generateReport() throws AccessDeniedException, RepositoryException {
        System.out.print("Введите имя файла для сохранения: ");
        String fname = scanner.nextLine();
        Params all_params = new Params();
        all_params.exportDTP = true;
        all_params.dtpps = menuDTPParams();;
        boolean res = reportService.save(usr.getId(), fname, "json", all_params);
        if (res)
            System.out.println("Файл успешно сохранен");
        else
            System.out.println("Файл сохранить не удалось");
    }

    private static void findDTP() throws AccessDeniedException, RepositoryException {
        DTPParams params = menuDTPParams();

        // Получение ДТП по параметрам и вывод их
        List<DTP> dtps = dtpService.getDTPsByParams(usr.getId(), params);
        if (dtps.isEmpty()) {
            System.out.println("По вашему запросу ДТП не найдены.");
        } else {
            System.out.println("Найденные ДТП:");
            int limit = Math.min(dtps.size(), 10);  // определение количества ДТП для вывода (минимум из 10 и реального количества ДТП)
            for (int i = 0; i < limit; i++) { dtps.get(i).print(); }
            if (dtps.size() > 10) { System.out.println("..."); }
        }
    }
    private static void addDTP() throws AccessDeniedException, RepositoryException {
        DTP dtp = menuDTP(new DTP(), "Добавление");
        boolean result = dtpService.addDTP(usr.getId(), dtp);
        if (result)
            System.out.println("ДТП было успешно добавлено!");
        else
            System.out.println("ДТП не было добавлено!");
    }
    private static void editDTP() throws AccessDeniedException, RepositoryException {
        System.out.print("Введите идентификатор ДТП: ");
        int dtp_id = scanner.nextInt();
        DTP dtp = dtpService.getDTP(usr.getId(), dtp_id);
        if (dtp == null) {
            System.out.println("ДТП не было найдено!");
            return;
        }
        DTP edittedDTP = menuDTP(dtp, "Изменение");
        boolean result = dtpService.editDTP(usr.getId(), edittedDTP);
        if (result)
            System.out.println("ДТП было успешно изменено!");
        else
            System.out.println("ДТП не было изменено!");
    }
    private static void delDTP() throws AccessDeniedException, RepositoryException {
        System.out.print("Введите идентификатор ДТП: ");
        int dtp_id = scanner.nextInt();
        DTP dtp = dtpService.getDTP(usr.getId(), dtp_id);
        if (dtp == null) {
            System.out.println("ДТП не было найдено");
            return;
        }
        dtp.print();
        boolean res = dtpService.deleteDTP(usr.getId(), dtp_id);
        if (res)
            System.out.println("ДТП было успешно удалено!");
        else
            System.out.println("ДТП не было удалено!");
    }
    private static void infoUsers() throws AccessDeniedException, RepositoryException {
        List<Integer> usrs = usrService.getAllUsersId(usr.getId());
        for (Integer integer : usrs) usrService.getUserById(usr.getId(), integer).print();
    }
    private static void addUser() throws AccessDeniedException, RepositoryException {
        User user = menuUser();
        boolean res = usrService.addUser(usr.getId(), user);
        if (res)
            System.out.println("Пользователь был успешно добавлен!");
        else
            System.out.println("Пользователь не был добавлен!");
    }
    private static void delUser() throws AccessDeniedException, RepositoryException {
        System.out.print("Введите идентификатор User: ");
        int usr_id = scanner.nextInt();
        User user = usrService.getUserById(usr.getId(), usr_id);
        if (user == null) {
            System.out.println("Пользователь не был найден");
            return;
        }
        user.print();
        boolean res = usrService.delUser(usr.getId(), usr_id);
        if (res)
            System.out.println("Пользователь был успешно удален!");
        else
            System.out.println("Пользователь не был удален!");
    }

    private static DTP menuDTP(DTP dtp, String work) {
        int choice;
        for (;;) {
            System.out.println("1. Идентификатор (" + dtp.getId() + ") -");
            System.out.println("2. Описание (" + dtp.getDescription() + ") -");
            System.out.println("3. Время (" + dtp.getDatetime() + ") -");
            System.out.println("4. Координаты (" + dtp.getCoords() + ") -");
            System.out.println("5. Дорога (" + dtp.getDor() + ") -");
            System.out.println("6. Освещение (" + dtp.getOsv() + ") -");
            System.out.println("7. Количество ТС (" + dtp.getCountTs() + ") -");
            System.out.println("8. Количество пострадавших (" + dtp.getCountParts() + ") -");
            System.out.println("0. Продолжить");
            System.out.print("Введите номер пункта (" + work + " ДТП): ");

            choice = scanner.nextInt();
            scanner.nextLine();
            if (choice > 8 || choice < 0)
            {
                System.out.println("Неверный выбор");
                continue;
            }
            if (choice == 0)
                break;
            else if (choice == 1) {
                System.out.print("Введите новый идентификатор: ");
                int newVehicleCount = scanner.nextInt();
                scanner.nextLine();  // Очистка буфера сканнера
                dtp.setId(newVehicleCount);
            }
            else if (choice == 2) {
                System.out.print("Введите описание: ");
                String dateStr = scanner.nextLine();
                dtp.setDescription(dateStr);
            }
            else if (choice == 3) {
                System.out.print("Введите время (в формате гггг-мм-дд чч:мм:сс): ");
                String dateStr = scanner.nextLine();
                dtp.setDatetime(dateStr);
            }
            else if (choice == 4) {
                System.out.print("Введите координаты: ");
                int coord1 = scanner.nextInt();
                int coord2 = scanner.nextInt();
                dtp.setCoords(coord1, coord2);
            }
            else if (choice == 5) {
                System.out.print("Введите наименование дороги: ");
                String str = scanner.nextLine();
                dtp.setDor(str);
            }
            else if (choice == 6) {
                System.out.print("Введите освещение дороги: ");
                String str = scanner.nextLine();
                dtp.setOsv(str);
            }
            else if (choice == 7) {
                System.out.print("Введите количество ТС: ");
                int cts = scanner.nextInt();
                dtp.setCountTs(cts);
            }
            else if (choice == 8) {
                System.out.print("Введите количество пострадавших: ");
                int cts = scanner.nextInt();
                dtp.setCountParts(cts);
            }

        }

        return dtp;
    }
    private static DTPParams menuDTPParams() {
        int choice;
        DTPParams prms = new DTPParams();
        for (;;) {
            System.out.println("1. Количество участников (" + prms.countTs + ") -");
            if (isLoggedIn)
            {
                System.out.println("2. Дата начала (" + prms.dtpBegin + ") -");
                System.out.println("3. Дата конца (" + prms.dtpEnd + ") -");
                System.out.println("4. Айди начала (" + prms.dtpIdBegin + ") -");
                System.out.println("5. Айди конца (" + prms.dtpIdEnd + ") -");
            }
            System.out.println("0. Продолжить");
            System.out.print("Введите номер пункта (параметры ДТП): ");

            choice = scanner.nextInt();
            scanner.nextLine();
            if (!isLoggedIn && choice > 1)
            {
                System.out.println("Неверный выбор");
                continue;
            }
            if (choice == 0)
                break;
            else if (choice == 1) {
                System.out.print("Введите новое количество участников: ");
                int newVehicleCount = scanner.nextInt();
                scanner.nextLine();  // Очистка буфера сканнера
                prms.countTs = newVehicleCount;
            }
            else if (choice == 2) {
                System.out.print("Введите новую дату начала (в формате гггг-мм-дд чч:мм:сс): ");
                String dateStr = scanner.nextLine();
                prms.dtpBegin = parseDate(dateStr);
            }
            else if (choice == 3) {
                System.out.print("Введите новую дату начала (в формате гггг-мм-дд чч:мм:сс): ");
                String dateStr = scanner.nextLine();
                prms.dtpEnd = parseDate(dateStr);
            }
            else if (choice == 4) {
                System.out.print("Введите начальный идентификатор ДТП: ");
                int id_dtp = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера сканнера
                prms.dtpIdBegin = id_dtp;
            }
            else if (choice == 5) {
                System.out.print("Введите конечный идентификатор ДТП: ");
                int id_dtp = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера сканнера
                prms.dtpIdEnd = id_dtp;
            }

            }

        return prms;
    }
    private static User menuUser() {
        int choice;
        User usr = new User();

        for (;;) {
            System.out.println("1. Идентификатор (" + usr.getId() + ") -");
            System.out.println("2. Логин (" + usr.getLogin() + ") -");
            System.out.println("3. Пароль (" + usr.getPassword() + ") -");
            System.out.println("4. Роль (" + usr.getRole() + ") -");
            System.out.println("0. Продолжить");
            System.out.print("Введите номер пункта (пользователь): ");

            choice = scanner.nextInt();
            scanner.nextLine();
            if (choice > 4 || choice < 0) {
                System.out.println("Неверный выбор");
                continue;
            }
            if (choice == 0)
                break;
            else if (choice == 1) {
                System.out.print("Введите новый идентификатор: ");
                int newVehicleCount = scanner.nextInt();
                scanner.nextLine();  // Очистка буфера сканнера
                usr.setId(newVehicleCount);
            } else if (choice == 2) {
                System.out.print("Введите логин: ");
                String lgn = scanner.nextLine();
                usr.setLogin(lgn);
            } else if (choice == 3) {
                System.out.print("Введите пароль: ");
                String pswrd = scanner.nextLine();
                usr.setPassword(pswrd);
            } else if (choice == 4) {
                System.out.print("Введите роль: ");
                String rl = scanner.nextLine();
                usr.setRole(rl);
            }
        }

        return usr;
    }

    public static void configureAndConnectToDatabase() {
        String host = Configurator.getValue("db.url");
        String database = Configurator.getValue("db.dbname");
        String username = Configurator.getValue("db.username");
        String password = Configurator.getValue("db.password");

        // Используем полученные значения для установки соединения с базой данных
        mngr = PostgresConnectionManager.getInstance(host, database, username, password);
    }

    public static Date parseDate(String dateStr) {
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Неверный формат даты. Пожалуйста, используйте формат: гггг-мм-дд чч:мм:сс");
        }
        return date;
    }
}


