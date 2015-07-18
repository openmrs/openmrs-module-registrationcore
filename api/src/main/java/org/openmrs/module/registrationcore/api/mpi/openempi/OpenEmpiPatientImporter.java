package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiPatientImporter implements MpiPatientImporter {

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator restQueryCreator;

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery patientById = restQueryCreator.getPatientById(patientId);
        return PatientQueryMapper.convert(patientById);
    }
}
