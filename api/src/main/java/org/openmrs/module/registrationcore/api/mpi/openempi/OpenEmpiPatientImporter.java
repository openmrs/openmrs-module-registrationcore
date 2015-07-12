package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;

public class OpenEmpiPatientImporter implements MpiPatientImporter {

    private MpiCredentials credentials;

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        return null;
    }

    public void setCredentials(MpiCredentials credentials) {
        this.credentials = credentials;
    }
}
