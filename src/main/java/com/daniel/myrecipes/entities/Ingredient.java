package com.daniel.myrecipes.entities;

import com.daniel.myrecipes.utils.UnitConverter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Ingredient implements Serializable {

    private static final long serialVersionUID = 2161866001700023305L;

    private int id;
    private int recipeId;
    private String name;
    private double quantity;
    private MeasurementUnit unit;
    private boolean mandatory;

    public Ingredient() {}

    public Ingredient(String name, double quantity, MeasurementUnit unit) {
        this(name, quantity, unit, true);
    }

    public Ingredient(String name, double quantity, MeasurementUnit unit, boolean mandatory) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.mandatory = mandatory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public MeasurementUnit getUnit() {
        return unit;
    }

    public void setUnit(MeasurementUnit unit) {
        this.unit = unit;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public String toString() {
        return UnitConverter.formatQuantity(quantity, unit)
                + " of " + name + (mandatory ? "" : " (Optional)");
    }

    public static Ingredient fromJSON(JSONObject data) throws JSONException {
        Ingredient ingredient = new Ingredient();

        ingredient.setName(data.getString("name"));
        ingredient.setQuantity(data.getDouble("quantity"));
        ingredient.setUnit(MeasurementUnit.valueOf(data.getString("unit").toUpperCase()));

        if (data.has("mandatory")) {
            ingredient.setMandatory(data.getBoolean("mandatory"));
        } else {
            ingredient.setMandatory(true);
        }

        return ingredient;
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("name", name)
                .put("quantity", quantity)
                .put("unit", unit)
                .put("mandatory", mandatory);
    }

}
