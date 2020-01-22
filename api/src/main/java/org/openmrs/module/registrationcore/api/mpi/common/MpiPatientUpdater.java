package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

/**
 * This service perform update for patient located in MPI server.
 */
public interface MpiPatientUpdater {

    /**
     * Query to remote server to perform update according to changed patient fields.
     * 
     * @param patient
     *            which should be updated
     */
    void updatePatient(Patient patient);
}
