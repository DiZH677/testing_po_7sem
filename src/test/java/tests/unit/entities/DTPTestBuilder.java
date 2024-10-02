package tests.unit.entities;

import entities.DTP;

public class DTPTestBuilder {
    private int id = -1;
    private String description = "default";
    private String datetime = "2020-01-01 12:00:00";
    private double coordW = 0.0;
    private double coordL = 0.0;
    private String dor = "default";
    private String osv = "default";
    private int countTs = 1;
    private int countParts = 1;

    public DTPTestBuilder withId(int id) {
        this.id = id;
        return this;
    }

    public DTPTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public DTPTestBuilder withDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public DTPTestBuilder withCoords(double coordW, double coordL) {
        this.coordW = coordW;
        this.coordL = coordL;
        return this;
    }

    public DTPTestBuilder withDor(String dor) {
        this.dor = dor;
        return this;
    }

    public DTPTestBuilder withOsv(String osv) {
        this.osv = osv;
        return this;
    }

    public DTPTestBuilder withCountTs(int countTs) {
        this.countTs = countTs;
        return this;
    }

    public DTPTestBuilder withCountParts(int countParts) {
        this.countParts = countParts;
        return this;
    }

    public DTP build() {
        return new DTP(id, description, datetime, coordW, coordL, dor, osv, countTs, countParts);
    }
}
