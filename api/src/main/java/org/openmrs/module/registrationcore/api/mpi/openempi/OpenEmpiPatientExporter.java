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

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Iterator;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier;

public class OpenEmpiPatientExporter implements MpiPatientExporter {
	
	@Autowired
	@Qualifier("registrationcore.openEmpiPatientQueryBuilder")
	private OpenEmpiPatientQueryBuilder queryBuilder;
	
	@Autowired
	@Qualifier("registrationcore.mpiProperties")
	private MpiProperties mpiProperties;
	
	@Autowired
	@Qualifier("registrationcore.mpiAuthenticator")
	private MpiAuthenticator authenticator;
	
	@Autowired
	@Qualifier("registrationcore.restQueryExecutor")
	private RestQueryExecutor queryExecutor;
	
	@Override
	public String exportPatient(Patient patient) {
		OpenEmpiPatientResult patientQuery = queryBuilder.build(patient);
		removeOpenEmpiGlobalIdentifier(patientQuery);
		
		OpenEmpiPatientResult mpiPerson = queryExecutor.exportPatient(authenticator.getToken(), patientQuery);
		return String.valueOf(mpiPerson.getPersonId());
	}
	
	//patient can't be exported to MPI server if it contains identifier which is Global in MPI.
	//The reason is that MPI server should manually generate Global identifier.
	private void removeOpenEmpiGlobalIdentifier(OpenEmpiPatientResult patientQuery) {
		Integer mpiGlobalIdentifierId = mpiProperties.getGlobalIdentifierDomainId();
		
		Iterator<PersonIdentifier> i = patientQuery.getPersonIdentifiers().iterator();
		
		while (i.hasNext()) {
			PersonIdentifier personIdentifier = i.next();
			if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(mpiGlobalIdentifierId)) {
				i.remove();
			}
		}
	}
}
