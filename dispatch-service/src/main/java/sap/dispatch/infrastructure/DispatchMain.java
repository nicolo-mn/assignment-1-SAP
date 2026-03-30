package sap.dispatch.infrastructure;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Vertx;
import sap.dispatch.application.DispatchServiceImpl;
import sap.dispatch.application.DroneService;

public class DispatchMain {

    static final int DISPATCH_PORT = 8081;
    static final String ACCOUNT_SERVICE_URI = "http://localhost:9000";

    public static void main(String[] args) {
        String portEnv = System.getenv("DISPATCH_PORT");
        final int port = (portEnv != null) ? Integer.parseInt(portEnv) : DISPATCH_PORT;

        String accountURI = System.getenv("ACCOUNT_SERVICE_URI");
        if (accountURI == null)
            accountURI = ACCOUNT_SERVICE_URI;

        String lightDronesEnv = System.getenv("LIGHT_DRONE_URIS");
        if (lightDronesEnv == null)
            lightDronesEnv = "http://localhost:9001,http://localhost:9002";

        String heavyDronesEnv = System.getenv("HEAVY_DRONE_URIS");
        if (heavyDronesEnv == null)
            heavyDronesEnv = "";

        List<DroneService> lightDrones = parseDroneProxies(lightDronesEnv);
        List<DroneService> heavyDrones = parseDroneProxies(heavyDronesEnv);

        Vertx vertx = Vertx.vertx();

        var userSessionRepository = new InMemoryUserSessionRepository();
        var accountService = new AccountServiceProxy(accountURI);
        var scheduler = new VertxDispatchScheduler(vertx);

        var dispatchService = new DispatchServiceImpl(
                userSessionRepository,
                accountService,
                lightDrones,
                heavyDrones,
                scheduler);

        var controller = new DispatchServiceController(dispatchService, port);
        vertx.deployVerticle(controller)
                .onSuccess(id -> System.out.println("Dispatch Service deployed on port " + port))
                .onFailure(Throwable::printStackTrace);
    }

    private static List<DroneService> parseDroneProxies(String urisEnv) {
        List<DroneService> list = new ArrayList<>();
        if (urisEnv == null || urisEnv.isBlank())
            return list;
        for (String uri : urisEnv.split(",")) {
            uri = uri.strip();
            if (!uri.isEmpty()) {
                list.add(new DroneServiceProxy(uri));
            }
        }
        return list;
    }
}
