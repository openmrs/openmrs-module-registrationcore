package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiAuthenticator implements MpiAuthenticator {

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator queryCreator;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    private String token;

    @Override
    public boolean isAuthenticated() {
        return token != null;
    }

    @Override
    public String getToken() {
        if (token == null) {
            performAuthentication();
        }
        return token;
    }

    @Override
    public void performAuthentication() {
        try {
            token = queryCreator.processAuthentication(mpiProperties.getMpiCredentials());
        } catch (RuntimeException e) {
            throw new APIException("Exception while processing authentication to MPI server.");
        }
    }
}
