package entities;

import java.util.List;

public class DCP {
    private List<DTP> dtps;
    private List<Car> cars;
    private List<Participant> participants;

    public DCP() {
        this.dtps = null;
        this.cars = null;
        this.participants = null;
    }

    public DCP(List<DTP> dtps, List<Car> cars, List<Participant> participants) {
        this.dtps = dtps;
        this.cars = cars;
        this.participants = participants;
    }

    public List<DTP> getDTPs() {
        return dtps;
    }
    public List<Car> getCars() {
        return cars;
    }
    public List<Participant> getParticipants() {
        return participants;
    }

    public void setDTPs(List<DTP> dtps) {
        this.dtps = dtps;
    }
    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
