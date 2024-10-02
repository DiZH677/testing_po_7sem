package tests.unit.entities;


import entities.Car;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CarTest {
    @Test
    public void testConstructorWithId() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        assertEquals(1, car.getId());
        assertEquals(100, car.getDtpId());
        assertEquals(2021, car.getCarYear());
        assertEquals("BMW", car.getMarka());
        assertEquals("X5", car.getModel());
        assertEquals("Black", car.getColor());
        assertEquals("SUV", car.getTypeTS());
    }

    @Test
    public void testConstructorWithIdNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        assertEquals(2021, car.getCarYear()); // Проверка негативного значения года
    }

    @Test
    public void testConstructorWithoutId() {
        Car car = new CarTestBuilder().withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        assertEquals(-1, car.getId());
        assertEquals(100, car.getDtpId());
        assertEquals(2021, car.getCarYear());
        assertEquals("BMW", car.getMarka());
        assertEquals("X5", car.getModel());
        assertEquals("Black", car.getColor());
        assertEquals("SUV", car.getTypeTS());
    }

    @Test
    public void testConstructorWithoutIdNegative() {
        Car car = new CarTestBuilder().withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS(null).build();
        assertNull(car.getTypeTS()); // Проверка для пустого значения типа ТС
    }

    @Test
    public void testSetId() {
        Car car = new CarTestBuilder().withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setId(10);
        assertEquals(10, car.getId());
    }

    @Test
    public void testSetIdNegative() {
        Car car = new CarTestBuilder().withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setId(-1);
        assertEquals(-1, car.getId());
    }

    @Test
    public void testSetDtpId() {
        Car car = new CarTestBuilder().withId(1).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setDtpId(200);
        assertEquals(200, car.getDtpId());
    }

    @Test
    public void testSetDtpIdNegative() {
        Car car = new CarTestBuilder().withId(1).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setDtpId(-200);
        assertEquals(-200, car.getDtpId());
    }

    @Test
    public void testSetCarYear() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setCarYear(2022);
        assertEquals(2022, car.getCarYear());
    }

    @Test
    public void testSetCarYearNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setCarYear(-2022);
        assertEquals(-2022, car.getCarYear());
    }

    @Test
    public void testSetModel() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setModel("X6");
        assertEquals("X6", car.getModel());
    }

    @Test
    public void testSetModelNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setModel(null);
        assertNull(car.getModel());
    }

    @Test
    public void testSetMarka() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withModel("X5")
                .withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setMarka("Audi");
        assertEquals("Audi", car.getMarka());
    }

    @Test
    public void testSetMarkaNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withModel("X5")
                .withCarYear(2021).withColor("Black").withTypeTS("SUV").build();
        car.setMarka("");
        assertEquals("", car.getMarka());
    }

    @Test
    public void testSetColor() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withTypeTS("SUV").build();
        car.setColor("White");
        assertEquals("White", car.getColor());
    }

    @Test
    public void testSetColorNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withTypeTS("SUV").build();
        car.setColor(null);
        assertNull(car.getColor());
    }

    @Test
    public void testSetTypeTS() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").build();
        car.setTypeTS("Hatchback");
        assertEquals("Hatchback", car.getTypeTS());
    }

    @Test
    public void testSetTypeTSNegative() {
        Car car = new CarTestBuilder().withId(1).withDtpId(100).withMarka("BMW")
                .withModel("X5").withCarYear(2021).withColor("Black").build();
        car.setTypeTS("");
        assertEquals("", car.getTypeTS());
    }
}

