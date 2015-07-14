package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenEmpiPatientImporter implements MpiPatientImporter {

    @Autowired
    private RestQueryCreator restQueryCreator;

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery patientById = restQueryCreator.getPatientById(patientId);
        return PatientQueryMapper.convert(patientById);
    }
}
