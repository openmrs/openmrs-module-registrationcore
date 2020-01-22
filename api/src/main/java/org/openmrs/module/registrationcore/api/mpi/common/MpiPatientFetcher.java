package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.PatientIdentifier;

/**
 * This service perform fetch patient from MPI server
 */
public interface MpiPatientFetcher {
	
	/**
	 * Perform query to mpi server to get patient by identifier.
	 * 
	 * @param patientIdentifier patient identifier
	 * @return mpiPatient object not yet saved to local DB.
	 */
	MpiPatient fetchMpiPatient(PatientIdentifier patientIdentifier);
	
	/**
	 * Perform query to mpi server to get patient by specific identifier.
	 * 
	 * @param patientId patient identifier
	 * @param identifierTypeUuid uuid of {@link org.openmrs.PatientIdentifierType }
	 * @return mpiPatient object not yet saved to local DB.
	 */
	MpiPatient fetchMpiPatient(String patientId, String identifierTypeUuid);
}
