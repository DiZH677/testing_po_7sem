package tests.unit.entities;

import entities.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;



public class DCPTest {

    @Test
    public void testGetDTPs() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        assertEquals(2, dcp.getDTPs().size());
    }

    @Test
    public void testGetDTPsEmpty() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        assertTrue(dcp.getDTPs().isEmpty());
    }

    @Test
    public void testGetCars() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        assertEquals(2, dcp.getCars().size());
    }

    @Test
    public void testGetCarsEmpty() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        assertTrue(dcp.getCars().isEmpty());
    }

    @Test
    public void testGetParticipants() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        assertEquals(2, dcp.getParticipants().size());
    }

    @Test
    public void testGetParticipantsEmpty() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        assertTrue(dcp.getParticipants().isEmpty());
    }

    @Test
    public void testSetDTPs() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        List<DTP> dtps = Arrays.asList(
                new DTP(1, "Desc 1", "2024-01-01 10:00:00", 1.0, 1.0, "Road 1", "Condition 1", 1, 1)
        );
        dcp.setDTPs(dtps);
        assertEquals(1, dcp.getDTPs().size());
    }

    @Test
    public void testSetCars() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        List<Car> cars = Arrays.asList(
                new Car(1, 1, "Brand", "Model", 2022, "Black", "SUV")
        );
        dcp.setCars(cars);
        assertEquals(1, dcp.getCars().size());
    }

    @Test
    public void testSetParticipants() {
        DCP dcp = DCPTestFactory.createEmptyDCP();
        List<Participant> participants = Arrays.asList(
                new Participant(1, 1, "Category", "Health", "Pol", true)
        );
        dcp.setParticipants(participants);
        assertEquals(1, dcp.getParticipants().size());
    }

    @Test
    public void testSetDTPsNull() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        dcp.setDTPs(null);
        assertNull(dcp.getDTPs());
    }

    @Test
    public void testSetCarsNull() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        dcp.setCars(null);
        assertNull(dcp.getCars());
    }

    @Test
    public void testSetParticipantsNull() {
        DCP dcp = DCPTestFactory.createDefaultDCP();
        dcp.setParticipants(null);
        assertNull(dcp.getParticipants());
    }
}
