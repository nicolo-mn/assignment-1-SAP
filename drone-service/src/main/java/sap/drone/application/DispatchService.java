package sap.drone.application;

import sap.common.exagonal.OutBoundPort;

@OutBoundPort
public interface DispatchService {
    void notifyShippingCompleted(String shippingId);
}
