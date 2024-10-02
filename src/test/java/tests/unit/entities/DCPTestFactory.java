package tests.unit.entities;

import entities.*;
import java.util.ArrayList;
import java.util.List;

public class DCPTestFactory {
    public static DCP createDefaultDCP() {
        List<DTP> dtps = new ArrayList<>();
        dtps.add(new DTP(1, "Description 1", "2024-01-01 10:00:00", 10.0, 20.0, "Road 1", "Condition 1", 3, 1));
        dtps.add(new DTP(2, "Description 2", "2024-01-02 11:00:00", 15.0, 25.0, "Road 2", "Condition 2", 2, 2));

        List<Car> cars = new ArrayList<>();
        cars.add(new Car(1, 1, "Toyota", "Corolla", 2020, "Red", "Sedan"));
        cars.add(new Car(2, 2, "Honda", "Civic", 2019, "Blue", "Hatchback"));

        List<Participant> participants = new ArrayList<>();
        participants.add(new Participant(1, 1, "Driver", "Healthy", "Male", true));
        participants.add(new Participant(2, 2, "Passenger", "Injured", "Female", false));

        return new DCP(dtps, cars, participants);
    }

    public static DCP createEmptyDCP() {
        return new DCP(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public static DCP createDCPWithNullLists() {
        return new DCP(null, null, null);
    }
}

