package sap.drone.application;

import sap.common.exagonal.OutBoundPort;

@OutBoundPort
public interface ShippingExecutor {
    void executeToCompletion(DroneService droneService);
}
