package sap.drone.domain;

import java.util.ArrayList;
import java.util.List;

import sap.common.ddd.Entity;

public class Shipping implements Entity<String> {
    private final String id;
    private Position currentPosition;
    private Position deliveryPosition;
    private ShippingStatus status = ShippingStatus.PENDING;
    private List<ShippingObserver> observers = new ArrayList<>();

    public Shipping(String id, Position pickupPosition, Position deliveryPosition) {
        this.id = id;
        this.currentPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
    }

    @Override
    public String getId() {
        return id;
    }

    public ShippingStatus update(Position position, long timeLeft) {
        if (status == ShippingStatus.PENDING) {
            notifyObservers(new ShippingStarted(id));
            status = ShippingStatus.IN_PROGRESS;
        }
        this.currentPosition = position;
        notifyObservers(new ShippingUpdate(id, position, timeLeft));
        if (status == ShippingStatus.IN_PROGRESS && position.equals(deliveryPosition)) {
            status = ShippingStatus.COMPLETED;
            notifyObservers(new ShippingCompleted(id));
        }
        return status;
    }

    public void addObserver(ShippingObserver observer) {
        observers.add(observer);
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public Position getDeliveryPosition() {
        return deliveryPosition;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    private void notifyObservers(ShippingEvent event) {
        for (ShippingObserver observer : observers) {
            observer.notifyShippingEvent(event);
        }
    }

}
