/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiVariables.*;

public class RestUrlBuilder {
	
	@Autowired
	@Qualifier("registrationcore.mpiProperties")
	private MpiProperties properties;
	
	public String createAuthenticationUrl() {
		return getServerUrl() + REST_URL + AUTHENTICATION_URL;
	}
	
	public String createGetPatientUrl(String id) {
		String url = getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PATIENT_BY_ID_URL;
		url += "?personId=" + id;
		return url;
	}
	
	public String createFindPreciseSimilarPatientsUrl() {
		return getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PRECISE_SIMILAR_PATIENTS_URL;
	}
	
	public String createFindProbablySimilarPatientsUrl() {
		return getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PROBABLY_SIMILAR_PATIENTS_URL;
	}
	
	public String createExportPatientUrl() {
		return getServerUrl() + REST_URL + PERSON_MANAGER_URL + IMPORT_PERSON_URL;
	}
	
	public String createUpdatePatientUrl() {
		return getServerUrl() + REST_URL + PERSON_MANAGER_URL + UPDATE_PERSON_URL;
	}
	
	private String getServerUrl() {
		return "http://" + properties.getServerUrl();
	}
}
