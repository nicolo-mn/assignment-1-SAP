package sap.dispatch.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import sap.dispatch.application.CreateShippingFailedException;
import sap.dispatch.application.DispatchService;
import sap.dispatch.application.LoginFailedException;
import sap.dispatch.domain.Position;

/**
 * Inbound HTTP adapter for the Dispatch Service.
 *
 * Routes:
 * POST /api/v1/dispatch/login
 * POST /api/v1/dispatch/user-sessions/:sessionId/shippings
 * POST /api/v1/dispatch/shippings/:shippingId/completed (called by drones)
 */
public class DispatchServiceController extends VerticleBase {

    static Logger logger = Logger.getLogger("[DispatchController]");

    static final String API_VERSION = "v1";
    static final String LOGIN_PATH = "/api/" + API_VERSION + "/dispatch/login";
    static final String CREATE_SHIPPING_PATH = "/api/" + API_VERSION + "/dispatch/user-sessions/:sessionId/shippings";
    static final String SHIPPING_COMPLETED_PATH = "/api/" + API_VERSION + "/dispatch/shippings/:shippingId/completed";

    private final DispatchService dispatchService;
    private final int port;

    public DispatchServiceController(DispatchService dispatchService, int port) {
        this.dispatchService = dispatchService;
        this.port = port;
    }

    @Override
    public Future<?> start() {
        logger.log(Level.INFO, "Dispatch Service initializing...");
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        // enable CORS for browser requests coming from the UI (different origin/port)
        router.route().handler(io.vertx.ext.web.handler.CorsHandler.create()
            .addOrigin("*")
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.OPTIONS)
            .allowedHeader("Access-Control-Allow-Method")
            .allowedHeader("Access-Control-Allow-Origin")
            .allowedHeader("Access-Control-Allow-Credentials")
            .allowedHeader("Content-Type")
            .allowedHeader("Authorization"));

        router.route().handler(BodyHandler.create());

        router.route(HttpMethod.POST, LOGIN_PATH).handler(this::login);
        router.route(HttpMethod.POST, CREATE_SHIPPING_PATH).handler(this::createShipping);
        router.route(HttpMethod.POST, SHIPPING_COMPLETED_PATH).handler(this::notifyShippingCompleted);

        return server
                .requestHandler(router)
                .listen(port)
                .onSuccess(res -> logger.log(Level.INFO, "Dispatch Service ready on port: " + port));
    }

    // handlers

    private void login(RoutingContext context) {
        logger.log(Level.INFO, "login request");
        JsonObject body = context.body().asJsonObject();
        String userName = body.getString("userName");
        String password = body.getString("password");
        var reply = new JsonObject();
        try {
            String sessionId = dispatchService.login(userName, password);
            reply.put("result", "ok");
            reply.put("sessionId", sessionId);
            reply.put("createShippingLink",
                    CREATE_SHIPPING_PATH.replace(":sessionId", sessionId));
            sendReply(context.response(), reply);
        } catch (LoginFailedException ex) {
            reply.put("result", "error");
            reply.put("error", "login-failed");
            sendError(context.response(), 401, reply);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendInternalError(context.response());
        }
    }

    private void createShipping(RoutingContext context) {
        logger.log(Level.INFO, "createShipping request");
        String sessionId = context.pathParam("sessionId");
        JsonObject body = context.body().asJsonObject();
        double pickupX = body.getDouble("pickupX");
        double pickupY = body.getDouble("pickupY");
        double deliveryX = body.getDouble("deliveryX");
        double deliveryY = body.getDouble("deliveryY");
        long timeLimit = body.getLong("timeLimit");
        long timeBeforeScheduling = body.getLong("timeBeforeScheduling", 0L);
        long weight = body.getLong("weight");

        var reply = new JsonObject();
        try {
            var result = dispatchService.createShipping(
                    sessionId,
                    new Position(pickupX, pickupY),
                    new Position(deliveryX, deliveryY),
                    timeLimit, timeBeforeScheduling, weight);
            reply.put("result", "ok");
            reply.put("shippingId", result.shippingId());
            reply.put("assignedDroneUri", result.droneUri());
            sendReply(context.response(), reply);
        } catch (CreateShippingFailedException ex) {
            reply.put("result", "error");
            reply.put("error", "create-shipping-failed");
            sendError(context.response(), 400, reply);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendInternalError(context.response());
        }
    }

    private void notifyShippingCompleted(RoutingContext context) {
        String shippingId = context.pathParam("shippingId");
        logger.log(Level.INFO, "notifyShippingCompleted: " + shippingId);
        try {
            dispatchService.notifyShippingCompleted(shippingId);
            sendReply(context.response(), new JsonObject().put("result", "ok"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendInternalError(context.response());
        }
    }

    // helpers

    private void sendReply(HttpServerResponse response, JsonObject reply) {
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    private void sendError(HttpServerResponse response, int statusCode, JsonObject reply) {
        response.setStatusCode(statusCode);
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }

    private void sendInternalError(HttpServerResponse response) {
        response.setStatusCode(500);
        response.putHeader("content-type", "application/json");
        response.end(new JsonObject().put("error", "internal-error").toString());
    }
}
