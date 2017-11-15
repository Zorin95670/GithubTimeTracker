package com.gtt.core;

public enum GTTTable {

    ACTIVITIES("activities"), TRASH("trash");

    private String name;

    private GTTTable(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
}
