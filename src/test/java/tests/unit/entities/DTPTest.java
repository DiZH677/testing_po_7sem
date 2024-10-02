// * Без mock, паттерн Builder
package tests.unit.entities;

import entities.DTP;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DTPTest {

    // Тесты для метода setId и getId
    @Test
    public void testSetIdPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setId(10);
        assertEquals(10, dtp.getId());
    }

    @Test
    public void testSetIdNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setId(-1);
        assertEquals(-1, dtp.getId());
    }

    // Тесты для метода setDescription и getDescription
    @Test
    public void testSetDescriptionPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDescription("Accident");
        assertEquals("Accident", dtp.getDescription());
    }

    @Test
    public void testSetDescriptionNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDescription(null);
        assertNull(dtp.getDescription());
    }

    // Тесты для метода setDatetime и getDatetime
    @Test
    public void testSetDatetimePositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDatetime("2023-09-20 12:00:00");
        assertEquals("2023-09-20 12:00:00", dtp.getDatetime());
    }

    @Test
    public void testSetDatetimeNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDatetime(null);
        assertNull(dtp.getDatetime());
    }

    // Тесты для метода setCoords и getCoords
    @Test
    public void testSetCoordsPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCoords((double) 50.123, 30.456);
        assertEquals(List.of(50.123, 30.456), dtp.getCoords());
    }

    @Test
    public void testSetCoordsNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCoords(0, 0);
        assertEquals(List.of(0.0, 0.0), dtp.getCoords());
    }

    // Тесты для метода setDor и getDor
    @Test
    public void testSetDorPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDor("Highway");
        assertEquals("Highway", dtp.getDor());
    }

    @Test
    public void testSetDorNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setDor(null);
        assertNull(dtp.getDor());
    }

    // Тесты для метода setOsv и getOsv
    @Test
    public void testSetOsvPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setOsv("Rainy");
        assertEquals("Rainy", dtp.getOsv());
    }

    @Test
    public void testSetOsvNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setOsv(null);
        assertNull(dtp.getOsv());
    }

    // Тесты для метода setCountTs и getCountTs
    @Test
    public void testSetCountTsPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCountTs(3);
        assertEquals(3, dtp.getCountTs());
    }

    @Test
    public void testSetCountTsNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCountTs(0);
        assertEquals(0, dtp.getCountTs());
    }

    // Тесты для метода setCountParts и getCountParts
    @Test
    public void testSetCountPartsPositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCountParts(2);
        assertEquals(2, dtp.getCountParts());
    }

    @Test
    public void testSetCountPartsNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.setCountParts(0);
        assertEquals(0, dtp.getCountParts());
    }

    // Тесты для метода update
    @Test
    public void testUpdatePositive() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.update(1, "Accident", "2023-09-20 12:00:00", 50.123, 30.456, "Highway", "Clear", 2, 1);
        assertEquals(1, dtp.getId());
        assertEquals("Accident", dtp.getDescription());
        assertEquals("2023-09-20 12:00:00", dtp.getDatetime());
        assertEquals(List.of(50.123, 30.456), dtp.getCoords());
        assertEquals("Highway", dtp.getDor());
        assertEquals("Clear", dtp.getOsv());
        assertEquals(2, dtp.getCountTs());
        assertEquals(1, dtp.getCountParts());
    }

    @Test
    public void testUpdateNegative() {
        DTP dtp = new DTPTestBuilder().build();
        dtp.update(-1, null, null, 0.0, 0.0, null, null, 0, 0);
        assertEquals(-1, dtp.getId());
        assertNull(dtp.getDescription());
        assertNull(dtp.getDatetime());
        assertEquals(List.of(0.0, 0.0), dtp.getCoords());
        assertNull(dtp.getDor());
        assertNull(dtp.getOsv());
        assertEquals(0, dtp.getCountTs());
        assertEquals(0, dtp.getCountParts());
    }

    // Тесты для метода print (сложно тестировать напрямую)
    @Test
    public void testPrintPositive() {
        DTP dtp = new DTPTestBuilder().withId(1).withDescription("Accident").build();
        dtp.print();  // Прямо протестировать вывод невозможно, нужно использовать System.out capture
    }

    @Test
    public void testPrintNegative() {
        DTP dtp = new DTPTestBuilder().withId(-1).withDescription(null).build();
        dtp.print();  // Прямо протестировать вывод невозможно, нужно использовать System.out capture
    }

    // Тесты для метода printInfo (сложно тестировать напрямую)
    @Test
    public void testPrintInfoPositive() {
        DTP dtp = new DTPTestBuilder().withId(1).withDescription("Accident").build();
        dtp.printInfo();  // Прямо протестировать вывод невозможно, нужно использовать System.out capture
    }

    @Test
    public void testPrintInfoNegative() {
        DTP dtp = new DTPTestBuilder().withId(-1).withDescription(null).build();
        dtp.printInfo();  // Прямо протестировать вывод невозможно, нужно использовать System.out capture
    }
}

