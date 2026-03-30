package sap.drone.domain;

public interface ShippingObserver {
    void notifyShippingEvent(ShippingEvent event);
}
