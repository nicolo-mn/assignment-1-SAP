package sap.drone.domain;

public record ShippingUpdate(String shippingId, Position position, long timeLeft) implements ShippingEvent {
    
}
