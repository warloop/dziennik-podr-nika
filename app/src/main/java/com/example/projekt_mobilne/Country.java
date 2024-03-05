package com.example.projekt_mobilne;

public class Country {
    private String name;
    private String capital;
    private String region;
    public Country(String name){
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
    public String toString() {
        return name; // Zwraca nazwę kraju jako reprezentację tekstową
    }
}
