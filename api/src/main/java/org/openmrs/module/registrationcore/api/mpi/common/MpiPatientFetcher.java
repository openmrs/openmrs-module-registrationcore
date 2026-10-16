/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
