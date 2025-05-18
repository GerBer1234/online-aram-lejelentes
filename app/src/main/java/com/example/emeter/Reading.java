package com.example.emeter;

import java.util.Date;

/**
 * Egy bejelentést modellező osztály.
 * A Firestore számára szükséges getterekkel, setterekkel és üres konstruktorral.
 */
public class Reading {
    private String id;
    private String startDate;
    private String endDate;
    private double consumption;
    private String comment;
    private Date createdAt;

    public Reading() {
        // Firestore-hoz kell az üres konstruktor
    }

    public Reading(String startDate, String endDate, double consumption, String comment) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.consumption = consumption;
        this.comment = comment;
    }

    // getterek és setterek Firestore miatt
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
