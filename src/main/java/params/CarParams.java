package params;

import java.util.List;

public class CarParams {
    public Integer carIdBegin;
    public Integer carIdEnd;
    public List<Integer> dtp_ids;
    public String color;
    public String marka;
    public String model;

    public CarParams() {
        // Все параметры будут инициализированы значением по умолчанию (null)
    }

    // Дополнительный конструктор, позволяющий инициализировать параметры явно
    public CarParams(Integer carIdBegin, Integer carIdEnd, List<Integer> dtp_ids, String color, String marka, String model) {
        this.carIdBegin = carIdBegin;
        this.carIdEnd = carIdEnd;
        this.dtp_ids = dtp_ids;
        this.color = color;
        this.marka = marka;
        this.model = model;
    }
}
