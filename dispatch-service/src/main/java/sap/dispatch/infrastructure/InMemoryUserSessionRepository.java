package sap.dispatch.infrastructure;

import java.util.HashMap;
import java.util.Map;

import sap.common.exagonal.Adapter;
import sap.dispatch.application.UserSessionRepository;
import sap.dispatch.domain.UserSession;

@Adapter
public class InMemoryUserSessionRepository implements UserSessionRepository {

    private final Map<String, UserSession> sessions = new HashMap<>();

    @Override
    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public boolean isPresent(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public void addSession(UserSession userSession) {
        sessions.put(userSession.getSessionId(), userSession);
    }
}
