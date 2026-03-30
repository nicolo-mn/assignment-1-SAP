package sap.drone.application;

import sap.drone.domain.Drone;
import sap.drone.domain.Position;
import sap.drone.domain.Shipping;
import sap.drone.domain.ShippingStatus;

public class DroneServiceImpl implements DroneService {
    private final ShippingRepository shippingRepository;
    private final Drone drone;
    private final DispatchService dispatchService;
    private Shipping currentShipping;
    private final ShippingExecutor shippingExecutor;

    public DroneServiceImpl(ShippingRepository shippingRepository, Drone drone, ShippingExecutor shippingExecutor,
            DispatchService dispatchService) {
        this.shippingRepository = shippingRepository;
        this.drone = drone;
        this.shippingExecutor = shippingExecutor;
        this.dispatchService = dispatchService;
    }

    @Override
    public void createNewShipping(String shippingId, Position pickupPosition, Position deliveryPosition)
            throws ShippingAlreadyPresentException {
        if (shippingRepository.isPresent(shippingId)) {
            throw new ShippingAlreadyPresentException();
        }
        shippingRepository.addShipping(new Shipping(shippingId, pickupPosition, deliveryPosition));
    }

    @Override
    public void startShipping(String shippingId) throws ShippingNotFoundException {
        if (!shippingRepository.isPresent(shippingId)) {
            throw new ShippingNotFoundException();
        }
        currentShipping = shippingRepository.getShipping(shippingId);
        drone.setTargetPosition(currentShipping.getCurrentPosition());
        shippingExecutor.executeToCompletion(this);
    }

    @Override
    public ShippingStatus updateCurrentShipping() {
        drone.moveToTarget();
        if (currentShipping.getStatus() == ShippingStatus.PENDING) {
            if (drone.getCurrentPosition().equals(currentShipping.getCurrentPosition())) {
                drone.setTargetPosition(currentShipping.getDeliveryPosition());
                return currentShipping.update(drone.getCurrentPosition(), drone.getTimeLeft());
            }
            return ShippingStatus.PENDING;
        }
        ShippingStatus status = currentShipping.update(drone.getCurrentPosition(), drone.getTimeLeft());
        if (status == ShippingStatus.COMPLETED) {
            dispatchService.notifyShippingCompleted(currentShipping.getId());
        }
        return status;
    }

    @Override
    public Shipping getShipping(String shippingId) throws ShippingNotFoundException {
        if (!shippingRepository.isPresent(shippingId)) {
            throw new ShippingNotFoundException();
        }
        return shippingRepository.getShipping(shippingId);
    }
}
