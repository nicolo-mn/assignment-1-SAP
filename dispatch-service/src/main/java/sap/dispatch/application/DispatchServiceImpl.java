package sap.dispatch.application;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import sap.dispatch.domain.Position;
import sap.dispatch.domain.Shipping;
import sap.dispatch.domain.UserId;
import sap.dispatch.domain.UserSession;
import sap.common.config.Config;

public class DispatchServiceImpl implements DispatchService {
    static Logger logger = Logger.getLogger("[Dispatch Service]");

    private UserSessionRepository userSessionRepository;
    private AccountService accountService;
    private int sessionCount;
    private int shippingCount;
    private DispatchScheduler dispatchScheduler;
    private Map<DroneService, Deque<Shipping>> lightFleetQueues;
    private Map<DroneService, Deque<Shipping>> heavyFleetQueues;
    private Map<String, DroneService> shippingToDroneMap;

    public DispatchServiceImpl(UserSessionRepository userSessionRepository, AccountService accountService,
            List<DroneService> lightDroneServices, List<DroneService> heavyDroneServices,
            DispatchScheduler dispatchScheduler) {
        this.userSessionRepository = userSessionRepository;
        this.accountService = accountService;
        this.shippingToDroneMap = new HashMap<>();
        this.dispatchScheduler = dispatchScheduler;
        this.sessionCount = 0;
        this.shippingCount = 0;
        this.lightFleetQueues = new HashMap<>();
        this.heavyFleetQueues = new HashMap<>();
        for (DroneService droneService : lightDroneServices) {
            lightFleetQueues.put(droneService, new LinkedList<>());
        }
        for (DroneService droneService : heavyDroneServices) {
            heavyFleetQueues.put(droneService, new LinkedList<>());
        }
    }

    @Override
    public String login(String userName, String password) throws LoginFailedException {
        logger.log(Level.INFO, "Login: " + userName + " " + password);
        try {
            accountService.isValidPassword(userName, password);
            var id = new UserId(userName);
            sessionCount++;
            var sessionId = "user-session-" + sessionCount;
            var us = new UserSession(sessionId, id);
            userSessionRepository.addSession(us);
            return us.getSessionId();
        } catch (Exception ex) {
            throw new LoginFailedException();
        }
    }

    @Override
    public CreateShippingResult createShipping(String sessionId, Position pickupPosition, Position deliveryPosition, long timeLimit,
            long timeBeforeScheduling, long weight) throws CreateShippingFailedException {
        var targetFleetOpt = getTargetFleet(pickupPosition, deliveryPosition, timeLimit, weight);
        if (targetFleetOpt.isPresent()) {
            var shippingId = generateShippingId();
            var shipping = new Shipping(shippingId, pickupPosition, deliveryPosition, weight, timeLimit);
            // assign casually to the first available drone in the target fleet
            var targetFleet = targetFleetOpt.get();
            DroneService assigned = null;
            for (DroneService d : targetFleet.keySet()) {
                assigned = d;
                break;
            }
            if (assigned != null) {
                shippingToDroneMap.put(shippingId, assigned);
                try {
                    assigned.createNewShipping(shippingId, pickupPosition, deliveryPosition);
                } catch (ShippingAlreadyPresentException e) {
                    logger.log(Level.WARNING, "Shipping already present on drone: " + shippingId);
                    throw new CreateShippingFailedException();
                }

                // schedule or start immediately depending on delay
                if (timeBeforeScheduling > 0) {
                    dispatchScheduler.scheduleDispatch(shipping, timeBeforeScheduling, this);
                } else {
                    scheduleShipping(shipping);
                }
                return new CreateShippingResult(shippingId, assigned.getUri());
            } else {
                throw new CreateShippingFailedException();
            }
        } else {
            throw new CreateShippingFailedException();
        }
    }

    @Override
    public void scheduleShipping(Shipping shipping) {
        // when the scheduler fires, start the shipping on the drone that was previously assigned
        DroneService assigned = shippingToDroneMap.get(shipping.getId());
        if (assigned == null) {
            logger.log(Level.WARNING, "No assigned drone found for shipping: " + shipping.getId());
            return;
        }
        var targetQueue = lightFleetQueues.containsKey(assigned) ? lightFleetQueues.get(assigned) : heavyFleetQueues.get(assigned);
        try {
            targetQueue.add(shipping);
            if (targetQueue.size() == 1) {
                assigned.startShipping(shipping.getId());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to start scheduled shipping: " + shipping.getId());
        }
    }

    @Override
    public void notifyShippingCompleted(String shippingId) {
        var drone = shippingToDroneMap.remove(shippingId);
        if (drone == null) {
            logger.log(Level.WARNING, "Received completion for unknown shipping: " + shippingId);
        }
        var targetQueue = lightFleetQueues.containsKey(drone) ? lightFleetQueues.get(drone) : heavyFleetQueues.get(drone);
        targetQueue.poll();
        if (!targetQueue.isEmpty()) {
            var nextShipping = targetQueue.peek();
            try {                
                drone.startShipping(nextShipping.getId());
            } catch (ShippingNotFoundException e) {
                logger.log(Level.WARNING, "Failed to start next shipping: " + nextShipping.getId());
            }
        }
    }

    private long computeDeliveryTime(Position pickupPosition, Position deliveryPosition, long speed) {
        double distance = Math.sqrt(Math.pow(deliveryPosition.x() - pickupPosition.x(), 2)
                + Math.pow(deliveryPosition.y() - pickupPosition.y(), 2));
        return (long) (distance / speed);
    }

    private String generateShippingId() {
        shippingCount++;
        return "shipping-" + shippingCount;
    }

    private Optional<Map<DroneService, Deque<Shipping>>> getTargetFleet(Position pickupPosition,
            Position deliveryPosition, long timeLimit, long weight) {
        if ((weight <= Config.LIGHT_DRONE_MAX_WEIGHT && lightFleetQueues.size() > 0
                && computeDeliveryTime(pickupPosition, deliveryPosition, Config.LIGHT_DRONE_MAX_SPEED) <= timeLimit)) {
            return Optional.of(lightFleetQueues);
        } else if (weight <= Config.HEAVY_DRONE_MAX_WEIGHT && heavyFleetQueues.size() > 0
                && computeDeliveryTime(pickupPosition, deliveryPosition, Config.HEAVY_DRONE_MAX_SPEED) <= timeLimit) {
            return Optional.of(heavyFleetQueues);
        } else {
            return Optional.empty();
        }
    }
}
