package com.example.mobile.entity;

public class Record {

    private String id;

    private String time;

    private Integer amount;

    public Record() {
    }

    public Record(String id, String time, Integer amount) {
        this.id = id;
        this.time = time;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
