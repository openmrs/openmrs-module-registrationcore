package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

/**
 * This service perform import patient from MPI server and convert it to local patient.
 */
public interface MpiPatientImporter {

    /**
     * Perform query to mpi server & convert it to local patient.
     * @param patientId patient identifier
     * @return converted patient
     */
    Patient importMpiPatient(String patientId);
}
