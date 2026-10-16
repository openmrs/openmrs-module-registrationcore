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

import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("registrationcore.ExistingPatientNameSearch")
public class ExistingPatientNameSearch implements PatientNameSearch {
	
	@Autowired
	private RegistrationCoreDAO dao;
	
	@Override
	public List<String> findSimilarGivenNames(String searchPhrase) {
		return dao.findExistingSimilarGivenNames(searchPhrase);
	}
	
	@Override
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		return dao.findExistingSimilarFamilyNames(searchPhrase);
	}
}
