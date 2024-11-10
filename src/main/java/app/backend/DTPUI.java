package app.backend;

import entities.DTP;

import java.util.List;

public class DTPUI {
    private int id;
    private String description;
    private String datetime;
    private double coordW;
    private double coordL;
    private String dor;
    private String osv;
    private int countTs;
    private int countParts;

    public DTPUI(String description, String datetime, double coordW, double coordL, String dor, String osv, int countTs, int countPart) {
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

    public DTPUI(int id, String description, String datetime, double coordW, double coordL, String dor, String osv, int countTs, int countPart) {
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

    public DTPUI(DTP dtp) {
        this.id = dtp.getId();
        this.description = dtp.getDescription();
        this.datetime = dtp.getDatetime();
        this.coordW = dtp.getCoords().get(0);
        this.coordL = dtp.getCoords().get(1);
        this.dor = dtp.getDor();
        this.osv = dtp.getOsv();
        this.countTs = dtp.getCountTs();
        this.countParts = dtp.getCountParts();
    }

    public DTPUI() {
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
    public void setCoords(int coordW, int coordL) { this.coordW = coordW; this.coordL = coordL; }
    public void setDor(String dor) { this.dor = dor; }
    public void setOsv(String osv) { this.osv = osv; }
    public void setCountTs(int countTs) { this.countTs = countTs; }
    public void setCountParts(int countParts) { this.countParts = countParts; }
    public void print() {
        String desc = (description != null && description.length() > 20) ? description.substring(0, 17) + "..." : (description != null ? description : "");
        String coordWFormatted = ("" + coordW).length() > 18 ? ("" + coordW).substring(0, 15) + "..." : "" + coordW;
        String coordLFormatted = ("" + coordL).length() > 18 ? ("" + coordL).substring(0, 15) + "..." : "" + coordL;
        String dorTrimmed = (dor != null && dor.length() > 15) ? dor.substring(0, 12) + "..." : (dor != null ? dor : "");
        String osvTrimmed = (osv != null && osv.length() > 15) ? osv.substring(0, 12) + "..." : (osv != null ? osv : "");

        System.out.printf("ID: %-5s | Описание: %-20s | Дата и Время: %-20s | Координаты: (%-18s, %-18s) | Дорожные условия: %-15s | Обстоятельства: %-15s | Количество транспортных средств: %-3s | Количество пострадавших: %-3s%n",
                id, desc, datetime, coordWFormatted, coordLFormatted, dorTrimmed, osvTrimmed, countTs, countParts);



    }

    public DTP getDTP() {
        return new DTP(id, description, datetime, coordW, coordL, dor, osv, countTs, countParts);
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
