package com.app.pojo.ExcelRow;

public class ExcelRow {
    private String stage;
    private String function;
    private String client;
    private String otherColumn;
    
    public ExcelRow() {}

    // Constructor
    public ExcelRow(String stage, String function, String client, String otherColumn) {
        this.stage = stage;
        this.function = function;
        this.client = client;
        this.otherColumn = otherColumn;
    }

    // Getters and Setters
    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getOtherColumn() {
        return otherColumn;
    }

    public void setOtherColumn(String otherColumn) {
        this.otherColumn = otherColumn;
    }

    @Override
    public String toString() {
        return "ExcelRow{" +
                "stage='" + stage + '\'' +
                ", function='" + function + '\'' +
                ", client='" + client + '\'' +
                ", otherColumn='" + otherColumn + '\'' +
                '}';
    }
}

