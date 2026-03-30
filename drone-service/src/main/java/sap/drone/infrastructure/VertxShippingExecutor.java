package sap.drone.infrastructure;

import io.vertx.core.Vertx;
import sap.drone.application.DroneService;
import sap.drone.application.ShippingExecutor;
import sap.drone.domain.ShippingStatus;

public class VertxShippingExecutor implements ShippingExecutor {
    private final Vertx vertx;
    
    public VertxShippingExecutor(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void executeToCompletion(DroneService droneService) {
        vertx.setPeriodic(1000, timerId -> {
            ShippingStatus status = droneService.updateCurrentShipping();
            if (status == ShippingStatus.COMPLETED) {
                vertx.cancelTimer(timerId);
            }
        });
    }
}
