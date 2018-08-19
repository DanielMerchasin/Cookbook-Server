package com.daniel.myrecipes.utils;

import com.daniel.myrecipes.entities.Ingredient;
import com.daniel.myrecipes.entities.MeasurementUnit;

public class UnitConverter {

    public static String formatQuantity(Ingredient ingredient) {
        return formatQuantity(ingredient.getQuantity(), ingredient.getUnit());
    }

    public static String formatQuantity(double quantity, MeasurementUnit measurementUnit) {

        String modifiedUnit = measurementUnit.toString();
        switch (measurementUnit) {
            case CUP:
                break;
            case TSP:
                break;
            case TBSP:
                break;
            case LITER:
                if (quantity < 1) {
                    quantity *= 1000;
                    modifiedUnit = "Milliliter";
                }
                break;
            case GRAM:
                if (quantity >= 1000) {
                    quantity /= 1000;
                    modifiedUnit = "Kilogram";
                } else if (quantity < 1) {
                    quantity *= 1000;
                    modifiedUnit = "Milligram";
                }
                break;
            case OZ:
                break;
        }

        if (quantity != 1)
            modifiedUnit += "s";

        if (quantity == (long) quantity) {
            return String.format("%d %s", (long) quantity, modifiedUnit);
        } else {
            return String.format("%s %s", quantity, modifiedUnit);
        }

    }

}
