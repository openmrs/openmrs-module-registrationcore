package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiPatientImporter implements MpiPatientImporter {

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator restQueryCreator;

    @Autowired
    @Qualifier("registrationcore.queryMapper")
    private PatientQueryMapper queryMapper;
    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Override
    public Patient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery mpiPatient = restQueryCreator.getPatientById(authenticator.getToken(), patientId);
        return queryMapper.importPatient(mpiPatient);
    }
}
