package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;
import org.openmrs.module.santedb.mpiclient.model.MpiPatient;

/**
 * This service perform fetch patient from MPI server and convert it to local patient.
 */
public interface MpiPatientFetcher {

    /**
     * Perform query to mpi server to get patient by ECID identifier & convert it to local patient.
     * @param patientEcidId patient identifier
     * @return converted patient not yet saved to local DB.
     */
    Patient fetchMpiPatient(String patientEcidId);

    /**
     * Perform query to mpi server to get patient by specific identifier & convert it to local patient.
     * @param patientId patient identifier
     * @param identifierTypeUuid uuid of {@link org.openmrs.PatientIdentifierType }
     * @return converted patient not yet saved to local DB.
     */
    Patient fetchMpiPatient(String patientId, String identifierTypeUuid);


    /**
     * Perform query to mpi server to get patient by specific identifier & converts it to local patient alongside related
     * observations
     * @param patientId patient identifier
     * @param identifierTypeUuid uuid of {@link org.openmrs.PatientIdentifierType }
     * @return converted patient not yet saved to local DB alongside the observations
     */
    MpiPatient fetchMpiPatientWithObservations(String patientId, String identifierTypeUuid);
}
