package sap.dispatch.application;

import sap.common.exagonal.OutBoundPort;

@OutBoundPort
public interface AccountService {
    boolean isValidPassword(String userName, String password) throws ServiceNotAvailableException;

}
