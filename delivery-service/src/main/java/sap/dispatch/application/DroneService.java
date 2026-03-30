package sap.dispatch.application;

import sap.common.exagonal.OutBoundPort;
import sap.dispatch.domain.Position;

@OutBoundPort
public interface DroneService {
    void createNewShipping(String shippingId, Position pickupPosition, Position deliveryPosition)
            throws ShippingAlreadyPresentException;

    void startShipping(String shippingId) throws ShippingNotFoundException;

}
