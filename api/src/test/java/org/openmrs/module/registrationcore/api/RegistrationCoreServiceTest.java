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
package org.openmrs.module.registrationcore.api;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests {@link $ RegistrationCoreService} .
 */
public class RegistrationCoreServiceTest extends BaseModuleContextSensitiveTest {
	
	private RegistrationCoreService service;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("personService")
	private PersonService personService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/module/idgen/include/TestData.xml");
		service = Context.getService(RegistrationCoreService.class);
		adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID, "1"));
	}
	
	private Patient createBasicPatient() {
		Patient patient = new Patient();
		PersonName pName = new PersonName();
		pName.setGivenName("Demo");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		
		patient.setBirthdate(new Date());
		patient.setDeathDate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("M");
		
		return patient;
	}
	
	/**
	 * @see {@link RegistrationCoreService#registerPatient(Patient,List<Relationship>)}
	 */
	@Test
	@Verifies(value = "should create a patient from record with relationships", method = "registerPatient(Patient,List<Relationship>)")
	public void registerPatient_shouldCreateAPatientFromRecordWithRelationships() throws Exception {
		Relationship r1 = new Relationship(null, personService.getPerson(2), personService.getRelationshipType(2));
		Relationship r2 = new Relationship(personService.getPerson(7), null, personService.getRelationshipType(2));
		Patient createdPatient = service.registerPatient(createBasicPatient(), Arrays.asList(r1, r2));
		assertNotNull(createdPatient.getPatientId());
		assertNotNull(createdPatient.getPatientIdentifier());
		assertNotNull(patientService.getPatient(createdPatient.getPatientId()));
		assertNotNull(createdPatient.getPersonId());
		assertNotNull(personService.getPerson(createdPatient.getPersonId()));
		Assert.assertEquals(2, personService.getRelationshipsByPerson(createdPatient).size());
	}
}
