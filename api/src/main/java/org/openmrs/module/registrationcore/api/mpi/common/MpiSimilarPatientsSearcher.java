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
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;
import java.util.Map;

/**
 * This service provide finding precise similar and probably similar matched patients.
 */
public interface MpiSimilarPatientsSearcher {
	
	/**
	 * Perform search on MPI server for patients using Hl7 PDQ message.
	 * 
	 * @param patient
	 * @param otherDataPoints
	 * @param cutoff
	 * @param maxResults
	 * @return list of possible matched patients
	 */
	List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
	        Integer maxResults);
	
	/**
	 * Perform search on MPI server for patients using Hl7 PDQ message. You would use this to perform
	 * one final check before actually creating a patient, after all data has been filled.
	 * 
	 * @param patient
	 * @param otherDataPoints
	 * @param cutoff
	 * @param maxResults
	 * @return list of possible matched patients
	 */
	List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
	        Integer maxResults);
}
