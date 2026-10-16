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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.api.BaseRegistrationCoreSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests BasicPatientNameSearch
 */
public class BasicPatientNameSearchTest extends BaseRegistrationCoreSensitiveTest {
	
	@Autowired
	@Qualifier("registrationcore.BasicPatientNameSearch")
	PatientNameSearch patientNameSearch;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	/**
	 * @see PatientNameSearch#findSimilarGivenNames(String)
	 * @verifies find by partial given name
	 */
	@Test
	public void findSimilarGivenNames_shouldFindByPartialGivenName() throws Exception {
		
		adminService.saveGlobalProperty(new GlobalProperty(BasicPatientNameSearch.GP_GIVEN_NAME_AUTO_SUGGEST_LIST,
		        " Horatio, Horctio,Harison,Horbtio, Kiptogom  "));
		
		List<String> names = patientNameSearch.findSimilarGivenNames("hor");
		Assert.assertNotNull(names);
		Assert.assertEquals(3, names.size());
	}
	
	/**
	 * @see PatientNameSearch#findSimilarFamilyNames(String)
	 * @verifies find by partial family name
	 */
	@Test
	public void findSimilarFamilyNames_shouldFindByPartialFamilyName() throws Exception {
		
		adminService.saveGlobalProperty(new GlobalProperty(BasicPatientNameSearch.GP_FAMILY_NAME_AUTO_SUGGEST_LIST,
		        "Hornblower ,Hornblewer, Hornblower ,Seroney,Rapondi"));
		
		List<String> names = patientNameSearch.findSimilarFamilyNames("blo");
		Assert.assertNotNull(names);
		Assert.assertEquals(2, names.size());
	}
}
