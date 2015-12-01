package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiAuthenticator implements MpiAuthenticator {

    @Autowired
    @Qualifier("registrationcore.restQueryExecutor")
    private RestQueryExecutor queryExecutor;

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
        token = queryExecutor.processAuthentication(mpiProperties.getMpiCredentials());
    }
}
