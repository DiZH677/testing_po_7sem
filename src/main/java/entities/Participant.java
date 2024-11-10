package entities;

public class Participant {
    int id;
    int car_id;
    String category;
    String health;
    String pol;
    boolean safety_belt;

    public Participant (int id, int car_id, String category, String health, String pol, boolean safety_belt)
    {
        this.id = id;
        this.car_id = car_id;
        this.category = category;
        this.health = health;
        this.pol = pol;
        this.safety_belt = safety_belt;
    }

    public Participant (int car_id, String category, String health, String pol, boolean safety_belt)
    {
        this.id = -1;
        this.car_id = car_id;
        this.category = category;
        this.health = health;
        this.pol = pol;
        this.safety_belt = safety_belt;
    }

    public int getId() {
        return id;
    }
    public int getCarId() {
        return car_id;
    }
    public String getCategory() {
        return category;
    }
    public String getHealth() {
        return health;
    }
    public String getPol() {
        return pol;
    }
    public boolean getSafetyBelt() {
        return safety_belt;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setCarId(int car_id) {
        this.car_id = car_id;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setHealth(String health) {
        this.health = health;
    }
    public void setPol(String pol) {
        this.pol = pol;
    }
    public void setSafetyBelt(boolean sb) {
        this.safety_belt = sb;
    }
}
