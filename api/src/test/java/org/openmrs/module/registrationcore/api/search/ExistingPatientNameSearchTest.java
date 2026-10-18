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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.registrationcore.api.BaseRegistrationCoreSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * Tests ExistingPatientNameSearchTest
 */
public class ExistingPatientNameSearchTest extends BaseRegistrationCoreSensitiveTest {
	
	@Autowired
	ExistingPatientNameSearch search;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("identifiers_dataset.xml");
		executeDataSet("patients_dataset.xml");
	}
	
	@Test
	public void findSimilarGivenNames_shouldFindAmongExistingGivenNames() {
		List<String> names = search.findSimilarGivenNames("Kip");
		
		Assert.assertEquals(Arrays.asList("Kiptogom"), names);
	}
	
	@Test
	public void findSimilarFamilyNames_shouldFindAmongExistingFamilyNames() {
		List<String> names = search.findSimilarFamilyNames("Nar");
		
		Assert.assertEquals(Arrays.asList("Nari"), names);
	}
}
