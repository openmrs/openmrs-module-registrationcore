/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.registrationcore.api;

import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(RegistrationCoreService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
public interface RegistrationCoreService extends OpenmrsService {
	
	/**
	 * Returns a list of matching patients using the fast algorithm.
	 * 
	 * @param patient
	 * @param otherDataPoints
	 * @param cutoff
	 * @param maxResults
	 * @return the list
	 */
	List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
	                                                 Integer maxResults);

	/**
     * Returns a list of matching patients using the precise algorithm.
     * 
     * @param patient
     * @param otherDataPoints
     * @param cutoff
     * @param maxResults
     * @return the list
     */
    List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff,
                                                     Integer maxResults);
}
