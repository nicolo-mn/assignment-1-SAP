package sap.drone.infrastructure;

import sap.drone.application.ShippingRepository;
import sap.drone.domain.Shipping;

import java.util.HashMap;
import java.util.Map;

public class ShippingRepositoryImpl implements ShippingRepository {
    private final Map<String, Shipping> shippings = new HashMap<>();

    @Override
    public Shipping getShipping(String shippingId) {
        return shippings.get(shippingId);
    }

    @Override
    public boolean isPresent(String shippingId) {
        return shippings.containsKey(shippingId);
    }

    @Override
    public void addShipping(Shipping shipping) {
        shippings.put(shipping.getId(), shipping);
    }
}
