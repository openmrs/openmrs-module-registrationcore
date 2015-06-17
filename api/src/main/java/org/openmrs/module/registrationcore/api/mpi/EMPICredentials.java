package org.openmrs.module.registrationcore.api.mpi;

public class EMPICredentials {

    private String username;

    private String password;

    private String token;

    public EMPICredentials(String username, String password) {
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
