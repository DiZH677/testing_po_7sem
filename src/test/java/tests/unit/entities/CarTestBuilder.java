package tests.unit.entities;

import entities.Car;

public class CarTestBuilder {
    private int id = -1;  // Значение по умолчанию для id
    private int dtp_id = 1;  // Пример значения для dtp_id
    private int car_year = 2020;  // Пример значения для года выпуска машины
    private String model = "Model";  // Пример значения для модели
    private String marka = "Marka";  // Пример значения для марки
    private String color = "Black";  // Пример значения для цвета
    private String type_ts = "Sedan";  // Пример значения для типа транспортного средства

    public CarTestBuilder() {
    }

    public CarTestBuilder withId(int id) {
        this.id = id;
        return this;
    }

    public CarTestBuilder withDtpId(int dtp_id) {
        this.dtp_id = dtp_id;
        return this;
    }

    public CarTestBuilder withCarYear(int car_year) {
        this.car_year = car_year;
        return this;
    }

    public CarTestBuilder withModel(String model) {
        this.model = model;
        return this;
    }

    public CarTestBuilder withMarka(String marka) {
        this.marka = marka;
        return this;
    }

    public CarTestBuilder withColor(String color) {
        this.color = color;
        return this;
    }

    public CarTestBuilder withTypeTS(String type_ts) {
        this.type_ts = type_ts;
        return this;
    }

    public Car build() {
        if (id == -1) {
            return new Car(dtp_id, marka, model, car_year, color, type_ts);
        } else {
            return new Car(id, dtp_id, marka, model, car_year, color, type_ts);
        }
    }
}

