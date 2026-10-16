/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.search;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.namephonetics.NamePhoneticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A namephonetics implementation of {@link PatientNameSearch}.
 */
@Service("registrationcore.NamePhoneticsPatientNameSearch")
public class NamePhoneticsPatientNameSearch implements PatientNameSearch {
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarGivenNames(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarGivenNames(String searchPhrase) {
		NamePhoneticsService service = Context.getService(NamePhoneticsService.class);
		return service.findSimilarGivenNames(searchPhrase);
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarFamilyNames(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		NamePhoneticsService service = Context.getService(NamePhoneticsService.class);
		return service.findSimilarFamilyNames(searchPhrase);
	}
}
