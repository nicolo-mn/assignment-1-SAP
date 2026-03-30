package sap.dispatch.infrastructure;

import io.vertx.core.Vertx;
import sap.common.exagonal.Adapter;
import sap.dispatch.application.DispatchScheduler;
import sap.dispatch.application.DispatchService;
import sap.dispatch.domain.Shipping;

import java.util.logging.Level;
import java.util.logging.Logger;

@Adapter
public class VertxDispatchScheduler implements DispatchScheduler {

    static Logger logger = Logger.getLogger("[VertxDispatchScheduler]");

    private final Vertx vertx;

    public VertxDispatchScheduler(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void scheduleDispatch(Shipping shipping, long timeBeforeScheduling, DispatchService dispatchService) {
        logger.log(Level.INFO, "Scheduling shipping " + shipping.getId() + " in " + timeBeforeScheduling + " ms");
        vertx.setTimer(timeBeforeScheduling, id -> {
            logger.log(Level.INFO, "Timer fired – scheduling shipping " + shipping.getId());
            dispatchService.scheduleShipping(shipping);
        });
    }
}
