package sap.drone.infrastructure;

import io.vertx.core.Vertx;
import sap.drone.application.DroneServiceImpl;
import sap.drone.domain.Drone;
import sap.drone.domain.Position;

public class DroneMain {

    public static void main(String[] args) {
        System.out.println("Starting Drone Service...");

        int port = 9001;
        String droneId = "drone-1";
        if (args.length >= 2) {
            droneId = args[0];
            port = Integer.parseInt(args[1]);
        } else {
            String portEnv = System.getenv("PORT");
            if (portEnv != null) port = Integer.parseInt(portEnv);
            String idEnv = System.getenv("DRONE_ID");
            if (idEnv != null) droneId = idEnv;
        }

        Vertx vertx = Vertx.vertx();
        var repository = new ShippingRepositoryImpl();
        var executor = new VertxShippingExecutor(vertx);
        var deliveryProxy = new DeliveryServiceProxy();
        
        Drone drone = new Drone(droneId, 5, 10.0, new Position(0, 0));
        
        DroneServiceImpl droneService = new DroneServiceImpl(
            repository,
            drone,
            executor,
            deliveryProxy
        );

        final int finalPort = port;
        final String finalDroneId = droneId;
        DroneVerticle verticle = new DroneVerticle(droneService, port);
        vertx.deployVerticle(verticle).onSuccess(id -> {
            System.out.println("Drone Service " + finalDroneId + " deployed on port " + finalPort);
        }).onFailure(err -> {
            err.printStackTrace();
        });
    }
}
