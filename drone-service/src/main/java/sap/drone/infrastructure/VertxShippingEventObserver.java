package sap.drone.infrastructure;
import java.util.logging.Logger;
import java.util.logging.Level;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import sap.drone.domain.ShippingEvent;
import sap.drone.domain.ShippingObserver;
import sap.drone.domain.ShippingStarted;
import sap.drone.domain.ShippingUpdate;
import sap.drone.domain.ShippingCompleted;

public class VertxShippingEventObserver implements ShippingObserver {
    Logger logger = Logger.getLogger("[VertxShippingEventObserver]");
    private final EventBus eventBus;

    public VertxShippingEventObserver(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void notifyShippingEvent(ShippingEvent event) {
        JsonObject json = new JsonObject();
        String shippingId = null;

        if (event instanceof ShippingStarted) {
            json.put("event", "ShippingStarted");
            shippingId = ((ShippingStarted) event).shippingId();
        } else if (event instanceof ShippingUpdate update) {
            json.put("event", "ShippingUpdate");
            json.put("x", update.position().x());
            json.put("y", update.position().y());
            json.put("timeLeft", update.timeLeft());
            shippingId = update.shippingId();
        } else if (event instanceof ShippingCompleted completed) {
            json.put("event", "ShippingCompleted");
            shippingId = completed.shippingId();
        }

        if (shippingId != null) {
            json.put("shippingId", shippingId);
            logger.log(Level.INFO, "Publishing Event for " + shippingId + ": " + json);
            eventBus.publish(shippingId, json);
        } else {
            logger.log(Level.WARNING, "Received unknown event type: " + event.getClass().getName());
        }
    }
}
