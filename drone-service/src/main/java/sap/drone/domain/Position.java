package sap.drone.domain;

import sap.common.ddd.ValueObject;

public record Position(double x, double y) implements ValueObject {
}
