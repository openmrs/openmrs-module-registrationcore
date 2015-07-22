package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

public interface MpiPatientImporter {

    Patient importMpiPatient(String patientId);
}
