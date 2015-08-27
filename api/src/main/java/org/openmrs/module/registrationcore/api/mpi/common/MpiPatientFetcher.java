package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

/**
 * This service perform fetch patient from MPI server and convert it to local patient.
 */
public interface MpiPatientFetcher {

    /**
     * Perform query to mpi server to get patient by identifier & convert it to local patient.
     * @param patientId patient identifier
     * @return converted patient
     */
    Patient fetchMpiPatient(String patientId);
}
