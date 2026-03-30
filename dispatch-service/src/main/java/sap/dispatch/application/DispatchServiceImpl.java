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
    public String createShipping(String sessionId, Position pickupPosition, Position deliveryPosition, long timeLimit,
            long timeBeforeScheduling, long weight) throws CreateShippingFailedException {
        if (getTargetFleet(pickupPosition, deliveryPosition, timeLimit, weight).isPresent()) {
            var shippingId = generateShippingId();
            var shipping = new Shipping(shippingId, pickupPosition, deliveryPosition, weight, timeLimit);
            if (timeBeforeScheduling > 0) {
                dispatchScheduler.scheduleDispatch(shipping, timeBeforeScheduling, this);
            } else {
                scheduleShipping(shipping);
            }
            return shippingId;
        } else {
            throw new CreateShippingFailedException();
        }
    }

    @Override
    public void scheduleShipping(Shipping shipping) {
        var targetFleet = getTargetFleet(shipping.getCurrentPosition(), shipping.getDeliveryPosition(),
                shipping.getTimeLimit(), shipping.getWeight()).get();
        var selectedDrone = getDroneWithLeastShippings(targetFleet);
        targetFleet.get(selectedDrone).add(shipping);
        shippingToDroneMap.put(shipping.getId(), selectedDrone);
        if (targetFleet.get(selectedDrone).size() == 1) {
            try {
                selectedDrone.startShipping(shipping.getId());
            } catch (ShippingNotFoundException e) {
                logger.log(Level.WARNING, "Shipping not found: " + shipping.getId());
            }
        }
    }

    @Override
    public void notifyShippingCompleted(String shippingId) {
        var drone = shippingToDroneMap.remove(shippingId);
        if (drone == null) {
            logger.log(Level.WARNING, "Received completion for unknown shipping: " + shippingId);
            return;
        }
        var queue = lightFleetQueues.containsKey(drone)
                ? lightFleetQueues.get(drone)
                : heavyFleetQueues.get(drone);
        if (queue != null) {
            queue.poll();
            var next = queue.peek();
            if (next != null) {
                try {
                    drone.startShipping(next.getId());
                } catch (ShippingNotFoundException e) {
                    logger.log(Level.WARNING, "Next shipping not found: " + next.getId());
                }
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

    private DroneService getDroneWithLeastShippings(Map<DroneService, Deque<Shipping>> fleetQueues) {
        DroneService selectedDrone = null;
        int minSize = Integer.MAX_VALUE;
        for (Map.Entry<DroneService, Deque<Shipping>> entry : fleetQueues.entrySet()) {
            if (entry.getValue().size() < minSize) {
                minSize = entry.getValue().size();
                selectedDrone = entry.getKey();
            }
        }
        return selectedDrone;
    }

}
