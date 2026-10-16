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

import org.openmrs.Patient;

/**
 * This service perform export patient to Mpi server.
 */
public interface MpiPatientExporter {
	
	/**
	 * Perform export patient to MPI server
	 * 
	 * @param patient patient which should be exported.
	 * @return exported MPI patient generated identifier
	 */
	String exportPatient(Patient patient);
}
