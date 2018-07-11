package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

/**
 * This service perform fetch patient from MPI server and convert it to local patient.
 */
public interface MpiPatientFetcher {

    /**
     * Perform query to mpi server to get patient by identifier & convert it to local patient.
     * @param patientIdentifier patient identifier
     * @return converted patient not yet saved to local DB.
     */
    Patient fetchMpiPatient(PatientIdentifier patientIdentifier);

    /**
     * Perform query to mpi server to get patient by specific identifier & convert it to local patient.
     * @param patientId patient identifier
     * @param identifierTypeUuid uuid of {@link org.openmrs.PatientIdentifierType }
     * @return converted patient not yet saved to local DB.
     */
    Patient fetchMpiPatient(String patientId, String identifierTypeUuid);
}
