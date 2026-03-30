package sap.dispatch.domain;

import sap.common.ddd.Entity;

public class User implements Entity<String> {
    private String userName; /* this is the id */
    private String password;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getId() {
        return userName;
    }
}
