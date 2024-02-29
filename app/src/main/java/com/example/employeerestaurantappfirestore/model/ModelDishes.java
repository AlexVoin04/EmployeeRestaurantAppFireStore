package com.example.employeerestaurantappfirestore.model;

import com.google.firebase.firestore.DocumentReference;

import org.apache.commons.math3.util.Precision;

import java.util.List;

public class ModelDishes {
    private String Id;
    private String Name;
    private String AbbreviatedName;
    private double Cost;
    private String Image;
    private DocumentReference IdTypeOfTheDishes;
    private DocumentReference IdUnits;
    private double WeightOfTheDish;
    private Integer Discount;
    private List<Composition> Compositions;

    public ModelDishes() {
    }

    public ModelDishes(String name, String abbreviatedName,
                       double cost, String image,
                       DocumentReference idTypeOfTheDishes,
                       DocumentReference idUnits, double weightOfTheDish,
                       Integer discount, List<Composition> compositions) {
        Name = name;
        AbbreviatedName = abbreviatedName;
        Cost = cost;
        Image = image;
        IdTypeOfTheDishes = idTypeOfTheDishes;
        IdUnits = idUnits;
        WeightOfTheDish = weightOfTheDish;
        Discount = discount;
        Compositions = compositions;
    }

    public static class Composition {
        private DocumentReference IdProduct;
        private DocumentReference IdUnits;
        private double Quantity;
        public Composition() {
        }

        public Composition(DocumentReference idProduct, DocumentReference idUnits, double quantity) {
            IdProduct = idProduct;
            IdUnits = idUnits;
            Quantity = quantity;
        }

        public DocumentReference getIdProduct() {
            return IdProduct;
        }

        public void setIdProduct(DocumentReference idProduct) {
            IdProduct = idProduct;
        }

        public DocumentReference getIdUnits() {
            return IdUnits;
        }

        public void setIdUnits(DocumentReference idUnits) {
            IdUnits = idUnits;
        }

        public double getQuantity() {
            return Quantity;
        }

        public void setQuantity(int quantity) {
            Quantity = quantity;
        }
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public List<Composition> getCompositions() {
        return Compositions;
    }

    public void setCompositions(List<Composition> compositions) {
        Compositions = compositions;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAbbreviatedName() {
        return AbbreviatedName;
    }

    public void setAbbreviatedName(String abbreviatedName) {
        AbbreviatedName = abbreviatedName;
    }

    public double getCost() {
        return Cost;
    }

    public void setCost(double cost) {
        Cost = cost;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public DocumentReference getIdTypeOfTheDishes() {
        return IdTypeOfTheDishes;
    }

    public void setIdTypeOfTheDishes(DocumentReference idTypeOfTheDishes) {
        IdTypeOfTheDishes = idTypeOfTheDishes;
    }

    public DocumentReference getIdUnits() {
        return IdUnits;
    }

    public void setIdUnits(DocumentReference idUnits) {
        IdUnits = idUnits;
    }

    public double getWeightOfTheDish() {
        return WeightOfTheDish;
    }

    public void setWeightOfTheDish(double weightOfTheDish) {
        WeightOfTheDish = weightOfTheDish;
    }

    public Integer getDiscount() {
        return Discount;
    }

    public void setDiscount(Integer discount) {
        Discount = discount;
    }

    public double getFinalPrice(){
        if (Discount>0){
            return Precision.round(Cost - (Cost/100*Discount), 2);
        }else return Cost;
    }
}
