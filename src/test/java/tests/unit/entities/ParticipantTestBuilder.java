package tests.unit.entities;

import entities.Participant;

public class ParticipantTestBuilder {
    private int id = -1;
    private int car_id = 0;
    private String category = "";
    private String health = "";
    private String pol = "";
    private boolean safety_belt = false;

    public ParticipantTestBuilder withId(int id) {
        this.id = id;
        return this;
    }

    public ParticipantTestBuilder withCarId(int car_id) {
        this.car_id = car_id;
        return this;
    }

    public ParticipantTestBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public ParticipantTestBuilder withHealth(String health) {
        this.health = health;
        return this;
    }

    public ParticipantTestBuilder withPol(String pol) {
        this.pol = pol;
        return this;
    }

    public ParticipantTestBuilder withSafetyBelt(boolean safety_belt) {
        this.safety_belt = safety_belt;
        return this;
    }

    public Participant build() {
        return new Participant(id, car_id, category, health, pol, safety_belt);
    }
}

