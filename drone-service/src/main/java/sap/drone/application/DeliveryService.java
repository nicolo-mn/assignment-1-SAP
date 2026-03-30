package sap.drone.application;

import sap.common.exagonal.OutBoundPort;

@OutBoundPort
public interface DeliveryService {
    void notifyShippingCompleted(String shippingId);
}
