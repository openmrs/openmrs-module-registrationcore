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
package org.openmrs.module.registrationcore.api.search;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests BasicPatientNameSearch
 */
public class BasicPatientNameSearchTest extends BaseModuleContextSensitiveTest{

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
		
		adminService.saveGlobalProperty(new GlobalProperty(BasicPatientNameSearch.GP_GIVEN_NAME_AUTO_SUGGEST_LIST, " Horatio, Horctio,Harison,Horbtio, Kiptogom  "));
		
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
		
		adminService.saveGlobalProperty(new GlobalProperty(BasicPatientNameSearch.GP_FAMILY_NAME_AUTO_SUGGEST_LIST, "Hornblower ,Hornblewer, Hornblower ,Seroney,Rapondi"));

		List<String> names = patientNameSearch.findSimilarFamilyNames("blo");
		Assert.assertNotNull(names);
		Assert.assertEquals(2, names.size());
	}
}
