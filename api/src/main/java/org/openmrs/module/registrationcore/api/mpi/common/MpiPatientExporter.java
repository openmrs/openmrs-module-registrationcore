package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

/**
 * This service perform export patient to Mpi server.
 */
public interface MpiPatientExporter {

    /**
     * Perform export patient to MPI server
     *
     * @param patient
     *            patient which should be exported.
     * @return exported MPI patient generated identifier
     */
    String exportPatient(Patient patient);
}
