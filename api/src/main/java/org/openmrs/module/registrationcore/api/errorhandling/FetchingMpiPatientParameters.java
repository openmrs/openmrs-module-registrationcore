/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.errorhandling;

import java.io.Serializable;

/**
 * Class that holds required parameters for fetching patient from MPI
 */
public class FetchingMpiPatientParameters implements Serializable {
	
	private String identifier;
	
	private String identifierTypeUuid;
	
	public FetchingMpiPatientParameters() {
	}
	
	public FetchingMpiPatientParameters(String identifier, String identifierTypeUuid) {
		this.identifier = identifier;
		this.identifierTypeUuid = identifierTypeUuid;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		
		FetchingMpiPatientParameters that = (FetchingMpiPatientParameters) o;
		
		if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
			return false;
		}
		return identifierTypeUuid != null ? identifierTypeUuid.equals(that.identifierTypeUuid)
		        : that.identifierTypeUuid == null;
	}
	
	@Override
	public int hashCode() {
		int result = identifier != null ? identifier.hashCode() : 0;
		result = 31 * result + (identifierTypeUuid != null ? identifierTypeUuid.hashCode() : 0);
		return result;
	}
	
	public String getIdentifier() {
		
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifierTypeUuid() {
		return identifierTypeUuid;
	}
	
	public void setIdentifierTypeUuid(String identifierTypeUuid) {
		this.identifierTypeUuid = identifierTypeUuid;
	}
}
