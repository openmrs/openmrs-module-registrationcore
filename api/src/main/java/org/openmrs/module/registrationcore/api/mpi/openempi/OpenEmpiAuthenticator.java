package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenEmpiAuthenticator implements MpiAuthenticator {

    @Autowired
    private RestQueryCreator queryCreator;

    @Override
    public void performAuthentication() {
        queryCreator.processAuthentication();
    }
}
