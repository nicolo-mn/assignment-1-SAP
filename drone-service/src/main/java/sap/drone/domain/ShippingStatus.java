package sap.drone.domain;

import sap.common.ddd.ValueObject;

public enum ShippingStatus implements ValueObject {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
