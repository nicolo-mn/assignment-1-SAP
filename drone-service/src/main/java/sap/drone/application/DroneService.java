package sap.drone.application;

import sap.drone.domain.Position;
import sap.drone.domain.ShippingStatus;
import sap.common.exagonal.InBoundPort;
import sap.drone.domain.Shipping;

@InBoundPort
public interface DroneService {
    void createNewShipping(String shippingId, Position position, long timeLeft) throws ShippingAlreadyPresentException;

    void startShipping(String shippingId) throws ShippingNotFoundException;

    ShippingStatus updateCurrentShipping();

    Shipping getShipping(String shippingId) throws ShippingNotFoundException;
}
