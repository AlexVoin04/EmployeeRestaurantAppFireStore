package com.example.employeerestaurantappfirestore.model;
public class ModelTablesData {
    private int[] langArray;
    private boolean[] selectedLanguage;

    public ModelTablesData(int[] langArray, boolean[] selectedLanguage) {
        this.langArray = langArray;
        this.selectedLanguage = selectedLanguage;
    }

    public int[] getLangArray() {
        return langArray;
    }

    public boolean[] getSelectedLanguage() {
        return selectedLanguage;
    }
}
