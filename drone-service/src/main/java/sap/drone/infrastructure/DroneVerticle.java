package sap.drone.infrastructure;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import sap.drone.application.DroneService;
import sap.drone.domain.Position;
import sap.drone.application.ShippingAlreadyPresentException;
import sap.drone.application.ShippingNotFoundException;

import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: cambio nome in controller? aggiungi versioni API?
public class DroneVerticle extends VerticleBase {

    static Logger logger = Logger.getLogger("[Drone Verticle]");

    private final int port;
    private final DroneService droneService;

    public DroneVerticle(DroneService droneService, int port) {
        this.port = port;
        this.droneService = droneService;
    }

    @Override
    public Future<?> start() {
        logger.log(Level.INFO, "Drone Service initializing...");
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.POST, "/api/shippings").handler(this::createShipping);
        router.route(HttpMethod.POST, "/api/shippings/:shippingId/start").handler(this::startShipping);

        this.handleEventSubscription(server, "/api/events");

        return server
                .requestHandler(router)
                .listen(port)
                .onSuccess(res -> {
                    logger.log(Level.INFO, "Drone Service ready on port: " + port);
                });
    }

    private void createShipping(RoutingContext context) {
        JsonObject body = context.body().asJsonObject();
        String shippingId = body.getString("shippingId");
        double pickupX = body.getDouble("pickupX");
        double pickupY = body.getDouble("pickupY");
        double deliveryX = body.getDouble("deliveryX");
        double deliveryY = body.getDouble("deliveryY");

        try {
            droneService.createNewShipping(shippingId, new Position(pickupX, pickupY),
                    new Position(deliveryX, deliveryY));
            JsonObject reply = new JsonObject().put("result", "ok");
            sendReply(context.response(), reply);
        } catch (ShippingAlreadyPresentException ex) {
            sendError(context.response(), 400, "shipping-already-present");
        } catch (Exception ex) {
            ex.printStackTrace();
            sendError(context.response(), 500, "internal-error");
        }
    }

    private void startShipping(RoutingContext context) {
        String shippingId = context.pathParam("shippingId");
        try {
            droneService.startShipping(shippingId);
            JsonObject reply = new JsonObject().put("result", "ok");
            sendReply(context.response(), reply);
        } catch (ShippingNotFoundException ex) {
            sendError(context.response(), 404, "shipping-not-found");
        } catch (Exception ex) {
            ex.printStackTrace();
            sendError(context.response(), 500, "internal-error");
        }
    }

    protected void handleEventSubscription(HttpServer server, String path) {
        server.webSocketHandler(webSocket -> {
            if (webSocket.path().equals(path)) {
                logger.log(Level.INFO, "New WS subscription accepted.");
                webSocket.textMessageHandler(msg -> {
                    JsonObject obj = new JsonObject(msg);
                    String shippingId = obj.getString("shippingId");

                    EventBus eb = vertx.eventBus();
                    eb.consumer(shippingId, evtMsg -> {
                        JsonObject ev = (JsonObject) evtMsg.body();
                        webSocket.writeTextMessage(ev.encodePrettily());
                    });

                    try {
                        var shipping = droneService.getShipping(shippingId);
                        shipping.addObserver(new VertxShippingEventObserver(eb));
                        logger.log(Level.INFO, "Observer added for shipping: " + shippingId);
                    } catch (ShippingNotFoundException e) {
                        logger.log(Level.WARNING, "Shipping not found for WS subscription: " + shippingId);
                        webSocket.close();
                    }
                });
            } else {
                webSocket.close();
            }
        });
    }

    private void sendReply(HttpServerResponse response, JsonObject reply) {
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    private void sendError(HttpServerResponse response, int statusCode, String message) {
        response.setStatusCode(statusCode);
        response.putHeader("content-type", "application/json");
        response.end(new JsonObject().put("error", message).toString());
    }
}
