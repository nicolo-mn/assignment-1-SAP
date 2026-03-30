package sap.drone.infrastructure;

import sap.drone.application.DispatchService;

public class DispatchServiceProxy implements DispatchService {
    @Override
    public void notifyShippingCompleted(String shippingId) {
        // TODO: actually notify dispatch service
    }
}
