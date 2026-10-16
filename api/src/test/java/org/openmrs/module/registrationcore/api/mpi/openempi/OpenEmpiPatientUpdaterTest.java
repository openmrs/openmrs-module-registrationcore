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
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenEmpiPatientUpdaterTest {
	
	private static final String TOKEN = "TOKEN_VALUE";
	
	@Mock
	private OpenEmpiPatientQueryBuilder queryBuilder;
	
	@Mock
	private MpiAuthenticator authenticator;
	
	@Mock
	private RestQueryExecutor queryCreator;
	
	@InjectMocks
	private OpenEmpiPatientUpdater patientUpdater;
	
	@Mock
	private Patient patient;
	
	@Mock
	private OpenEmpiPatientResult query;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testUpdatePatient() throws Exception {
		when(authenticator.getToken()).thenReturn(TOKEN);
		when(queryBuilder.build(patient)).thenReturn(query);
		
		patientUpdater.updatePatient(patient);
		
		verify(queryCreator).updatePatient(TOKEN, query);
	}
}
