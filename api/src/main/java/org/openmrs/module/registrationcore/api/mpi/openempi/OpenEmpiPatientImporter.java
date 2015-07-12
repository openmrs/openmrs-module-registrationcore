package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;

public class OpenEmpiPatientImporter implements MpiPatientImporter {

    private RestQueryCreator restQueryCreator;

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery patientById = restQueryCreator.getPatientById(patientId);
        return PatientQueryMapper.convert(patientById);
    }

    public void setRestQueryCreator(RestQueryCreator restQueryCreator) {
        this.restQueryCreator = restQueryCreator;
    }
}
