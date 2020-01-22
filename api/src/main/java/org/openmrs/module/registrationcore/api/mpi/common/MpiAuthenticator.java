package org.openmrs.module.registrationcore.api.mpi.common;

/**
 * This service perform authentication to MPI server. It is a Spring managed bean which is configured in
 * moduleApplicationContext.xml.
 */
public interface MpiAuthenticator {

    /**
     * Checks if Authentication was performed and token is valid.
     *
     * @return true if authenticated, otherwise false.
     */
    boolean isAuthenticated();

    /**
     * Perform authentication to MPI server.
     */
    void performAuthentication();

    /**
     * Returns token value if authentication was performed, otherwise performs authentication before, and return token
     * value.
     *
     * @return authentication token
     */
    String getToken();
}
