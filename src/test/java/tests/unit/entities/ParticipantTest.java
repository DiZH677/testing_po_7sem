package tests.unit.entities;

import entities.Participant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParticipantTest {

    // Позитивные тесты
    @Test
    public void testGetId() {
        Participant participant = new ParticipantTestBuilder().withId(1).build();
        assertEquals(1, participant.getId());
    }

    @Test
    public void testGetCarId() {
        Participant participant = new ParticipantTestBuilder().withCarId(100).build();
        assertEquals(100, participant.getCarId());
    }

    @Test
    public void testGetCategory() {
        Participant participant = new ParticipantTestBuilder().withCategory("Driver").build();
        assertEquals("Driver", participant.getCategory());
    }

    @Test
    public void testGetHealth() {
        Participant participant = new ParticipantTestBuilder().withHealth("Good").build();
        assertEquals("Good", participant.getHealth());
    }

    @Test
    public void testGetPol() {
        Participant participant = new ParticipantTestBuilder().withPol("Male").build();
        assertEquals("Male", participant.getPol());
    }

    @Test
    public void testGetSafetyBelt() {
        Participant participant = new ParticipantTestBuilder().withSafetyBelt(true).build();
        assertTrue(participant.getSafetyBelt());
    }

    @Test
    public void testSetId() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setId(2);
        assertEquals(2, participant.getId());
    }

    @Test
    public void testSetCarId() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setCarId(200);
        assertEquals(200, participant.getCarId());
    }

    @Test
    public void testSetCategory() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setCategory("Passenger");
        assertEquals("Passenger", participant.getCategory());
    }

    @Test
    public void testSetHealth() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setHealth("Fair");
        assertEquals("Fair", participant.getHealth());
    }

    @Test
    public void testSetPol() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setPol("Female");
        assertEquals("Female", participant.getPol());
    }

    @Test
    public void testSetSafetyBelt() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setSafetyBelt(false);
        assertFalse(participant.getSafetyBelt());
    }

    // Негативные тесты
    @Test
    public void testGetId_Negative() {
        Participant participant = new ParticipantTestBuilder().withId(-1).build();
        assertEquals(-1, participant.getId());
    }

    @Test
    public void testGetCarId_Negative() {
        Participant participant = new ParticipantTestBuilder().withCarId(-1).build();
        assertEquals(-1, participant.getCarId());
    }

    @Test
    public void testGetCategory_Negative() {
        Participant participant = new ParticipantTestBuilder().withCategory(null).build();
        assertNull(participant.getCategory());
    }

    @Test
    public void testGetHealth_Negative() {
        Participant participant = new ParticipantTestBuilder().withHealth("").build();
        assertEquals("", participant.getHealth());
    }

    @Test
    public void testGetPol_Negative() {
        Participant participant = new ParticipantTestBuilder().withPol(null).build();
        assertNull(participant.getPol());
    }

    @Test
    public void testGetSafetyBelt_Negative() {
        Participant participant = new ParticipantTestBuilder().withSafetyBelt(false).build();
        assertFalse(participant.getSafetyBelt());
    }

    @Test
    public void testSetId_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setId(-1);
        assertEquals(-1, participant.getId());
    }

    @Test
    public void testSetCarId_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setCarId(-1);
        assertEquals(-1, participant.getCarId());
    }

    @Test
    public void testSetCategory_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setCategory(null);
        assertNull(participant.getCategory());
    }

    @Test
    public void testSetHealth_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setHealth("");
        assertEquals("", participant.getHealth());
    }

    @Test
    public void testSetPol_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setPol(null);
        assertNull(participant.getPol());
    }

    @Test
    public void testSetSafetyBelt_Negative() {
        Participant participant = new ParticipantTestBuilder().build();
        participant.setSafetyBelt(true);
        assertTrue(participant.getSafetyBelt());
    }
}

