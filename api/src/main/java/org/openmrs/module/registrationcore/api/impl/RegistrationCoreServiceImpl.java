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
package org.openmrs.module.registrationcore.api.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;

/**
 * It is a default implementation of {@link RegistrationCoreService}.
 */
public class RegistrationCoreServiceImpl extends BaseOpenmrsService implements RegistrationCoreService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private RegistrationCoreDAO dao;
	
	private SimilarPatientSearchAlgorithm fastSimilarPatientSearchAlgorithm;
	
	private SimilarPatientSearchAlgorithm preciseSimilarPatientSearchAlgorithm;
	
	public void setDao(RegistrationCoreDAO dao) {
		this.dao = dao;
	}
	
	public void setFastSimilarPatientSearchAlgorithm(SimilarPatientSearchAlgorithm fastSimilarPatientSearchAlgorithm) {
		this.fastSimilarPatientSearchAlgorithm = fastSimilarPatientSearchAlgorithm;
	}
	
	public void setPreciseSimilarPatientSearchAlgorithm(SimilarPatientSearchAlgorithm preciseSimilarPatientSearchAlgorithm) {
		this.preciseSimilarPatientSearchAlgorithm = preciseSimilarPatientSearchAlgorithm;
	}
	
	@Override
	public List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                            Double cutoff, Integer maxResults) {
		return fastSimilarPatientSearchAlgorithm.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}
	
	@Override
	public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                               Double cutoff, Integer maxResults) {
		return preciseSimilarPatientSearchAlgorithm.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}
}
