package sap.dispatch.application;

import sap.common.exagonal.InBoundPort;
import sap.dispatch.domain.Position;
import sap.dispatch.domain.Shipping;

@InBoundPort
public interface DispatchService {
    String login(String userName, String password) throws LoginFailedException;

    CreateShippingResult createShipping(String sessionId, Position pickupPosition, Position deliveryPosition, long timeLimit,
            long timeBeforeScheduling, long weight) throws CreateShippingFailedException;

    void scheduleShipping(Shipping shipping);

    void notifyShippingCompleted(String shippingId);
}
