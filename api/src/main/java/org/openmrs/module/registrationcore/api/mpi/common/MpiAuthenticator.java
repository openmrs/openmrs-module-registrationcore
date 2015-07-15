package org.openmrs.module.registrationcore.api.mpi.common;

public interface MpiAuthenticator {

    boolean isAuthenticated();

    void performAuthentication();
}
