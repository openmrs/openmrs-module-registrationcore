package org.openmrs.module.registrationcore.api.mpi;

public class MpiCredentials {

    private String username;

    private String password;

    private String token;

    public MpiCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }
}
