package com.rcg.walmart.seating;

public class Seat {
    private int blockNumber;
    private int seatNumber;
    private double seatValue;

    public Integer getBlockNumber() {
        return this.blockNumber;
    }

    public Integer getSeatNumber() {
        return this.seatNumber;
    }

    public double getSeatValue() {
        return seatValue;
    }

    public Seat(int blockNumber, int seatNumber, double seatValue) {
        this.blockNumber = blockNumber;
        this.seatNumber = seatNumber;
        this.seatValue = seatValue;
    }
}
