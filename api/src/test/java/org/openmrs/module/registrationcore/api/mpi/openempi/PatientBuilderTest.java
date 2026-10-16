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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientBuilderTest {
	
	private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";
	
	private static final String LOCAL_OPENMRS_IDENTIFIER_TYPE_UUID = "3";
	
	private static final String MPI_OPENMRS_IDENTIFIER_TYPE_ID = "13";
	
	private static final String LOCAL_ECID_IDENTIFIER_TYPE_UUID = "5";
	
	private static final String MPI_ECID_IDENTIFIER_TYPE_ID = "60";
	
	private static final String PATIENT_OPENMRS_IDENTIFIER_VALUE = "1001NF";
	
	private static final String PATIENT_ECID_IDENTIFIER_VALUE = "019bb820-1bd3-11e5-8a7f-040158db6201";
	
	private static final String PERSON_IDENTIFIER_TYPE_UUID = "6";
	
	@InjectMocks
	private PatientBuilder patientBuilder;
	
	@Mock
	private PatientIdentifierMapper identifierMapper;
	
	@Mock
	private IdentifierBuilder identifierBuilder;
	
	@Mock
	private MpiProperties mpiProperties;
	
	@Mock
	private RegistrationCoreProperties registrationCoreProperties;
	
	private XmlMarshaller xmlMarshaller = new XmlMarshaller();
	
	@Mock
	private PatientIdentifier openmrsidentifier;
	
	@Mock
	private PatientIdentifier ecidIdentifier;
	
	@Mock
	private PatientIdentifier personIdentifier;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockIdentifierMapper();
		mockIdentifierBuilder();
		mockRegistrationCoreProperties();
		when(mpiProperties.getMpiPersonIdentifierTypeUuid()).thenReturn(PERSON_IDENTIFIER_TYPE_UUID);
	}
	
	private void mockIdentifierMapper() {
		when(identifierMapper.getMappedLocalIdentifierTypeUuid(MPI_OPENMRS_IDENTIFIER_TYPE_ID))
		        .thenReturn(LOCAL_OPENMRS_IDENTIFIER_TYPE_UUID);
		when(identifierMapper.getMappedLocalIdentifierTypeUuid(MPI_ECID_IDENTIFIER_TYPE_ID))
		        .thenReturn(LOCAL_ECID_IDENTIFIER_TYPE_UUID);
	}
	
	private void mockIdentifierBuilder() {
		when(identifierBuilder.createIdentifier(LOCAL_OPENMRS_IDENTIFIER_TYPE_UUID, PATIENT_OPENMRS_IDENTIFIER_VALUE, null))
		        .thenReturn(openmrsidentifier);
		when(identifierBuilder.createIdentifier(LOCAL_ECID_IDENTIFIER_TYPE_UUID, PATIENT_ECID_IDENTIFIER_VALUE, null))
		        .thenReturn(ecidIdentifier);
		// the MPI person identifier is added to the patient as well; patient identifiers can't contain null
		when(identifierBuilder.createIdentifier(eq(PERSON_IDENTIFIER_TYPE_UUID), anyString(), isNull()))
		        .thenReturn(personIdentifier);
	}
	
	private void mockRegistrationCoreProperties() {
		when(registrationCoreProperties.getOpenMrsIdentifierUuid()).thenReturn(LOCAL_OPENMRS_IDENTIFIER_TYPE_UUID);
	}
	
	@Test
	public void testBuildPatient() throws Exception {
		OpenEmpiPatientResult query = readPatient(PATIENT_WITH_OPENMRS_ID);
		
		patientBuilder.setPatient(new MpiPatient());
		Patient patient = patientBuilder.buildPatient(query);
		
		assertEquals(patient.getGender(), query.getGender().getGenderCode());
		
		assertEquals(patient.getFamilyName(), query.getFamilyName());
		assertEquals(patient.getGivenName(), query.getGivenName());
		assertEquals(patient.getMiddleName(), query.getMiddleName());
		
		verify(openmrsidentifier).setPreferred(true);
		// compare by identity: patient identifiers are a sorted set, which can't look up mocks reliably
		assertTrue(patient.getIdentifiers().stream().anyMatch(i -> i == openmrsidentifier));
		assertTrue(patient.getIdentifiers().stream().anyMatch(i -> i == ecidIdentifier));
	}
	
	private OpenEmpiPatientResult readPatient(String path) throws Exception {
		return xmlMarshaller.getQuery(path);
	}
}
