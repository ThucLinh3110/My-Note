package com.example.mynote.models;

public class ColorOption {
    private final String name;
    private final String hex;

    public ColorOption(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }

    public String getName() {
        return name;
    }

    public String getHex() {
        return hex;
    }
}