package params;

import java.util.List;

public class ParticipantParams {
    public Integer prIdBegin;
    public Integer prIdEnd;
    public List<Integer> car_ids;
    public String category;
    public String pol;
    public String health;
    public Boolean safety_belt;

    public ParticipantParams() {
        // Все параметры будут инициализированы значением по умолчанию (null)
    }

    // Дополнительный конструктор, позволяющий инициализировать параметры явно
    public ParticipantParams(Integer prIdBegin, Integer prIdEnd, List<Integer> car_ids, String category, String pol, String health, Boolean safety_belt) {
        this.prIdBegin = prIdBegin;
        this.prIdEnd = prIdEnd;
        this.car_ids = car_ids;
        this.category = category;
        this.pol = pol;
        this.health = health;
        this.safety_belt = safety_belt;
    }
}
