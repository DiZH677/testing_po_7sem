package entities;

import java.util.List;

public class DTP {
    private int id;
    private String description;
    private String datetime;
    private double coordW;
    private double coordL;
    private String dor;
    private String osv;
    private int countTs;
    private int countParts;

    public DTP(String description, String datetime, double coordW, double coordL, String dor, String osv, int countTs, int countPart) {
        this.id = -1;
        this.description = description;
        this.datetime = datetime;
        this.coordW = coordW;
        this.coordL = coordL;
        this.dor = dor;
        this.osv = osv;
        this.countTs = countTs;
        this.countParts = countPart;
    }

    public DTP(int id, String description, String datetime, double coordW, double coordL, String dor, String osv, int countTs, int countPart) {
        this.id = id;
        this.description = description;
        this.datetime = datetime;
        this.coordW = coordW;
        this.coordL = coordL;
        this.dor = dor;
        this.osv = osv;
        this.countTs = countTs;
        this.countParts = countPart;
    }

    public DTP() {
        this.id = -1;
        this.description = null;
        this.datetime = "2000-01-01 00:00:00";
        this.coordW = 0;
        this.coordL = 0;
        this.dor = null;
        this.osv = null;
        this.countTs = 0;
        this.countParts = 0;
    }

    // Геттеры и сеттеры для всех полей (идут после конструктора)

    public int getId() {
        return id;
    }
    public String getDescription() { return description; }
    public String getDatetime() { return datetime; }
    public List<Double> getCoords() { return List.of(coordW, coordL); }
    public String getDor() { return dor; }
    public String getOsv() { return osv; }
    public int getCountTs() { return countTs; }
    public int getCountParts() { return countParts; }

    public void setId(int id) {
        this.id = id;
    }
    public void setDescription(String desc) { this.description = desc; }
    public void setDatetime(String datetime) { this.datetime = datetime; }
    public void setCoords(double coordW, double coordL) { this.coordW = coordW; this.coordL = coordL; }
    public void setDor(String dor) { this.dor = dor; }
    public void setOsv(String osv) { this.osv = osv; }
    public void setCountTs(int countTs) { this.countTs = countTs; }
    public void setCountParts(int countParts) { this.countParts = countParts; }
    public void print() {
        String desc = formatString(description, 20);
        String coordWFormatted = formatString(coordW, 18);
        String coordLFormatted = formatString(coordL, 18);
        String dorTrimmed = formatString(dor, 15);
        String osvTrimmed = formatString(osv, 15);

        System.out.printf("ID: %-5s | Описание: %-20s | Дата и Время: %-20s | Координаты: (%-18s, %-18s) | Дорожные условия: %-15s | Обстоятельства: %-15s | Количество транспортных средств: %-3s | Количество пострадавших: %-3s%n",
                id, desc, datetime, coordWFormatted, coordLFormatted, dorTrimmed, osvTrimmed, countTs, countParts);
    }

    private String formatString(String input, int maxLength) {
        if (input == null) return "";
        if (input.length() > maxLength) {
            return input.substring(0, maxLength - 3) + "...";
        }
        return input;
    }

    private String formatString(double input, int maxLength) {
        String inputStr = String.valueOf(input);
        return formatString(inputStr, maxLength);
    }


    // метод для модификации полей
    public void update(int id, String description, String datetime, double coordW, double coordL, String dor, String osv, int countTs, int countPart) {
        this.id = id;
        this.description = description;
        this.datetime = datetime;
        this.coordW = coordW;
        this.coordL = coordL;
        this.dor = dor;
        this.osv = osv;
        this.countTs = countTs;
        this.countParts = countPart;
    }

    public void printInfo() {
        System.out.printf("id: %5d; desc: %10s; datetime: %10s; cords: %f.%f; dor: %10s, osv: %10s; countTs: %2d; countPart: %3d\n",
                id, description, datetime, coordW, coordL, dor, osv, countTs, countParts);
    }
}
