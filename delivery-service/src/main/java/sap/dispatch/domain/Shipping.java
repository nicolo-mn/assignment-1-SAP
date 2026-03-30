package sap.dispatch.domain;

import java.util.ArrayList;
import java.util.List;

import sap.common.ddd.Entity;

public class Shipping implements Entity<String> {
    private final String id;
    private Position currentPosition;
    private Position deliveryPosition;
    private long weight;
    private long timeLimit;

    public Shipping(String id, Position pickupPosition, Position deliveryPosition, long weight, long timeLimit) {
        this.id = id;
        this.currentPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.weight = weight;
        this.timeLimit = timeLimit;
    }

    @Override
    public String getId() {
        return id;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public Position getDeliveryPosition() {
        return deliveryPosition;
    }

    public long getWeight() {
        return weight;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

}
