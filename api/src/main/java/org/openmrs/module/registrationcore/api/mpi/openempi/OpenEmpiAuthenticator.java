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

import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiAuthenticator implements MpiAuthenticator {
	
	@Autowired
	@Qualifier("registrationcore.restQueryExecutor")
	private RestQueryExecutor queryExecutor;
	
	@Autowired
	@Qualifier("registrationcore.mpiProperties")
	private MpiProperties mpiProperties;
	
	private String token;
	
	@Override
	public boolean isAuthenticated() {
		return token != null;
	}
	
	@Override
	public String getToken() {
		if (token == null) {
			performAuthentication();
		}
		return token;
	}
	
	@Override
	public void performAuthentication() {
		token = queryExecutor.processAuthentication(mpiProperties.getMpiCredentials());
	}
}
