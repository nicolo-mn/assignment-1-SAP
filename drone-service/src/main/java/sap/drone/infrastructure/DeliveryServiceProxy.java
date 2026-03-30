package sap.drone.infrastructure;

import sap.drone.application.DeliveryService;

public class DeliveryServiceProxy implements DeliveryService {
    @Override
    public void notifyShippingCompleted(String shippingId) {
        System.out.println("DELIVERY SERVICE PROXY: Notified shipping completed for " + shippingId);
    }
}
