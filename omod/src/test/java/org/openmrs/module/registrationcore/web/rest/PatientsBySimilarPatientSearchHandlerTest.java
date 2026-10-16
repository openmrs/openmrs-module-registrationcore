/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PatientsBySimilarPatientSearchHandlerTest extends MainResourceControllerTest {
	
	private static final String PATIENT_UUID = "dd553355-1691-11df-97a5-7038c432aabf";
	
	@Override
	public String getURI() {
		return "patient";
	}
	
	@Override
	public String getUuid() {
		return PATIENT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return Context.getPatientService().getAllPatients().size();
	}
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("patients_dataset.xml");
	}
	
	@Test
	public void shouldReturnOneSimilarPatient() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("matchSimilar", "true");
		req.addHeader("givenname", "Jarus");
		req.addParameter("middlename", "Agemba");
		req.addParameter("familyname", "Rapondi");
		req.addParameter("gender", "M");
		req.addParameter("city", "Shiseso");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		assertThat(patients.size(), is(1));
	}
	
	@Test
	public void shouldNotReturnSimilarPatient() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET, getURI());
		req.addParameter("matchSimilar", "true");
		req.addHeader("givenname", "New");
		req.addParameter("middlename", "User");
		req.addParameter("familyname", "User");
		req.addParameter("gender", "M");
		req.addParameter("city", "city");
		
		SimpleObject result = deserialize(handle(req));
		List<Object> patients = result.get("results");
		assertThat(patients.size(), is(0));
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
}
