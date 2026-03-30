package sap.dispatch.domain;

import sap.common.ddd.Entity;

public class UserSession implements Entity<String> {
    private String sessionId;
    private UserId userId;

    public UserSession(String sessionId, UserId userId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public String getId() {
        return sessionId;
    }
}
