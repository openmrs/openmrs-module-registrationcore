package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiPatientUpdater implements MpiPatientUpdater {

    @Autowired
    @Qualifier("registrationcore.openEmpiPatientQueryBuilder")
    private OpenEmpiPatientQueryBuilder queryBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Autowired
    @Qualifier("registrationcore.restQueryExecutor")
    private RestQueryExecutor queryExecutor;

    @Override
    public void updatePatient(Patient patient) {
        OpenEmpiPatientQuery patientQuery = queryBuilder.build(patient);

        queryExecutor.updatePatient(authenticator.getToken(), patientQuery);
    }
}
