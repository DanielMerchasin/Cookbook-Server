package com.daniel.myrecipes.utils;

import com.daniel.myrecipes.entities.MeasurementUnit;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnitConverterTest {

    @Test
    public void testUnitConverter() {

        System.out.println(UnitConverter.formatQuantity(0.9, MeasurementUnit.TSP));

    }

}