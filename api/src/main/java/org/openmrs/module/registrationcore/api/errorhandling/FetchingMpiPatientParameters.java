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
