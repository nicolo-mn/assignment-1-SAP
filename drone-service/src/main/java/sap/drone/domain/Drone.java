package sap.drone.domain;

import sap.common.ddd.Entity;

public class Drone implements Entity<String> {

    private String id;
    private int speed;
    private double maxWeight;
    private Position currentPosition;
    private Position stationPosition;
    private Position targetPosition;

    public Drone(String id, int speed, double maxWeight, Position stationPosition) {
        this.id = id;
        this.speed = speed;
        this.maxWeight = maxWeight;
        this.currentPosition = stationPosition;
        this.stationPosition = stationPosition;
        this.targetPosition = stationPosition;
    }

    @Override
    public String getId() {
        return id;
    }

    public int getSpeed() {
        return speed;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public Position setTargetPosition(Position targetPosition) {
        this.targetPosition = targetPosition;
        return targetPosition;
    }

    public void moveToTarget() {
        if (targetPosition == null) return;
        double dx = targetPosition.x() - currentPosition.x();
        double dy = targetPosition.y() - currentPosition.y();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            currentPosition = new Position(targetPosition.x(), targetPosition.y());
        } else {
            double angle = Math.atan2(dy, dx);
            double newX = currentPosition.x() + Math.cos(angle) * speed;
            double newY = currentPosition.y() + Math.sin(angle) * speed;
            currentPosition = new Position(newX, newY);
        }
    }

    public long getTimeLeft() {
        if (targetPosition == null) return 0;
        double dx = targetPosition.x() - currentPosition.x();
        double dy = targetPosition.y() - currentPosition.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return (long) (distance / speed);
    }
}
