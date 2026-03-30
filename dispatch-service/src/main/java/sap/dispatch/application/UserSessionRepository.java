package sap.dispatch.application;

import sap.common.exagonal.OutBoundPort;
import sap.dispatch.domain.UserSession;

@OutBoundPort
public interface UserSessionRepository {
    UserSession getSession(String sessionId);

    boolean isPresent(String sessionId);

    void addSession(UserSession userSession);
}
