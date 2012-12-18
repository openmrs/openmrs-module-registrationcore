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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.search.PatientSearch;

/**
 * It is a default implementation of {@link RegistrationCoreService}.
 */
public class RegistrationCoreServiceImpl extends BaseOpenmrsService implements RegistrationCoreService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private final RegistrationCoreDAO dao;
	
	private final PatientSearch patientSearch;
	
	public RegistrationCoreServiceImpl(RegistrationCoreDAO dao, PatientSearch patientSearch) {
		this.dao = dao;
		this.patientSearch = patientSearch;
	}

	/**
     * @see org.openmrs.module.registrationcore.api.search.PatientSearch#findSimilarPatients(org.openmrs.Patient, java.lang.Integer)
     */
    @Override
    public List<Patient> findSimilarPatients(Patient patient, Integer maxResults) {
	    return patientSearch.findSimilarPatients(patient, maxResults);
    }

	/**
     * @see org.openmrs.module.registrationcore.api.search.PatientSearch#findDuplicatePatients(org.openmrs.Patient, java.lang.Integer)
     */
    @Override
    public List<Patient> findDuplicatePatients(Patient patient, Integer maxResults) {
	    return patientSearch.findDuplicatePatients(patient, maxResults);
    }
}