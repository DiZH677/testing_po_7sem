package entities;

public class Car {
    int id;
    int dtp_id;
    int car_year;
    String model;
    String marka;
    String color;
    String type_ts;

    public Car(int id, int dtp_id, String marka, String model, int car_year, String color, String type_ts) {
        this.id = id;
        this.dtp_id = dtp_id;
        this.marka = marka;
        this.car_year = car_year;
        this.model = model;
        this.color = color;
        this.type_ts = type_ts;
    }

    public Car(int dtp_id, String marka, String model, int car_year, String color, String type_ts) {
        this.id = -1;
        this.dtp_id = dtp_id;
        this.marka = marka;
        this.car_year = car_year;
        this.model = model;
        this.color = color;
        this.type_ts = type_ts;
    }

    public int getId() {
        return id;
    }
    public int getDtpId() {
        return dtp_id;
    }
    public int getCarYear() { return car_year; }
    public String getModel() { return model; }
    public String getMarka() { return marka; }
    public String getColor() { return color; }
    public String getTypeTS() { return type_ts; }

    public void setId(int id) { this.id = id; }
    public void setDtpId(int dtp_id) {
        this.dtp_id = dtp_id;
    }
    public void setCarYear(int car_year) { this.car_year = car_year; }
    public void setModel(String model) { this.model = model; }
    public void setMarka(String marka) { this.marka = marka; }
    public void setColor(String color) { this.color = color; }
    public void setTypeTS(String type_ts) { this.type_ts = type_ts; }
}
