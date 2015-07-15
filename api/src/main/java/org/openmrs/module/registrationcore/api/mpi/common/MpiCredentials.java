package org.openmrs.module.registrationcore.api.mpi.common;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "authenticationRequest")
public class MpiCredentials {

    private String username;

    private String password;

    private String token;

    public MpiCredentials() {
        //Is used for XML marshalling.
    }

    public MpiCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
