package sap.dispatch.application;

import sap.common.exagonal.OutBoundPort;
import sap.dispatch.domain.Shipping;

@OutBoundPort
public interface DispatchScheduler {
    void scheduleDispatch(Shipping shipping, long timeBeforeScheduling, DispatchService dispatchService);
}
