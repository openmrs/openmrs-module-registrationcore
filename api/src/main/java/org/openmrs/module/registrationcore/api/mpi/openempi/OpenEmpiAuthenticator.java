package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;

public class OpenEmpiAuthenticator implements MpiAuthenticator {

    @Autowired
    private RestQueryCreator queryCreator;
    private boolean authenticated;

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void performAuthentication() {
        try {
            queryCreator.processAuthentication();
            authenticated = true;
        } catch (RuntimeException e) {
            throw new APIException("Exception while processing authentication to MPI server.");
        }
    }
}
