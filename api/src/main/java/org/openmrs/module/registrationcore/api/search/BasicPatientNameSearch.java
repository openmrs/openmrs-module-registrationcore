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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * It's a default implementation of {@link PatientNameSearch}.
 */
@Service("registrationcore.BasicPatientNameSearch")
public class BasicPatientNameSearch implements PatientNameSearch {
	
	public static final String GP_GIVEN_NAME_AUTO_SUGGEST_LIST = "registrationcore.givenNameAutoSuggestList";
	
	public static final String GP_FAMILY_NAME_AUTO_SUGGEST_LIST = "registrationcore.familyNameAutoSuggestList";
	
	private static final String NAME_SEPARATOR = ",";
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarGivenNames(String)
	 * @should find by partial given name
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarGivenNames(String searchPhrase) {
		return findSimilarStrings(searchPhrase, GP_GIVEN_NAME_AUTO_SUGGEST_LIST);
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarFamilyNames(String)
	 * @should find by partial family name
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		return findSimilarStrings(searchPhrase, GP_FAMILY_NAME_AUTO_SUGGEST_LIST);
	}
	
	/**
	 * Finds a list of strings, in a global property, that match a given search phrase.
	 * 
	 * @param searchPhrase the search phrase.
	 * @param gpKey the global property key.
	 * @return list of strings that match the search phrase.
	 */
	private List<String> findSimilarStrings(String searchPhrase, String gpKey) {
		List<String> names = new ArrayList<String>();
		
		String nameList = adminService.getGlobalProperty(gpKey);
		if (!StringUtils.isBlank(nameList)) {
			searchPhrase = searchPhrase.toLowerCase().trim();
			String[] nameArray = nameList.split(NAME_SEPARATOR);
			for (String name : nameArray) {
				if (name.toLowerCase().trim().contains(searchPhrase)) {
					names.add(name);
				}
			}
		}
		
		return names;
	}
}
