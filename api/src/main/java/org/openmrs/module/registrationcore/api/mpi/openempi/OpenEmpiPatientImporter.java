package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
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

    @Override
    public Patient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery mpiPatient = restQueryCreator.getPatientById(patientId);
        return queryMapper.importPatient(mpiPatient);
    }
}
