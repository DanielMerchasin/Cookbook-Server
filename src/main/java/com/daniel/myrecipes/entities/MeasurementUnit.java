package com.daniel.myrecipes.entities;

public enum MeasurementUnit {

    CUP("Cup"),
    TSP("Teaspoon"),
    TBSP("Tablespoon"),
    GRAM("Gram"),
    LITER("Liter"),
    OZ("Ounce"),
    UNIT("Unit");

    final private String desc;

    MeasurementUnit(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

}
