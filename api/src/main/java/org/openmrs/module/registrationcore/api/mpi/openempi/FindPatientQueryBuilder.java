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

public class FindPatientQueryBuilder {
	
	public OpenEmpiPatientResult create(Patient patient) {
		OpenEmpiPatientResult patientQuery = new OpenEmpiPatientResult();
		
		//perform search by Family and Given names
		patientQuery.setFamilyName(patient.getFamilyName());
		patientQuery.setGivenName(patient.getGivenName());
		return patientQuery;
	}
}
