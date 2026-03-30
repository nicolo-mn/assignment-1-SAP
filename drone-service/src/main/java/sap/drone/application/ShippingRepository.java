package sap.drone.application;

import sap.common.exagonal.OutBoundPort;
import sap.drone.domain.Shipping;

@OutBoundPort
public interface ShippingRepository {
    Shipping getShipping(String shippingId);
    boolean isPresent(String shippingId);
    void addShipping(Shipping shipping);
}
