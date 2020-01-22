package org.openmrs.module.registrationcore.api.errorhandling;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * Class that holds required parameters for sending patient to MPI
 */
public class SendingPatientToMpiParameters implements Serializable {
	
	private String patientUuid;
	
	public SendingPatientToMpiParameters() {
	}
	
	public SendingPatientToMpiParameters(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	@Override
	public String toString() {
		return "SendingPatientToMpiParameters [patientUuid = " + patientUuid + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SendingPatientToMpiParameters that = (SendingPatientToMpiParameters) o;
		return StringUtils.equals(patientUuid, that.patientUuid);
	}
	
	@Override
	public int hashCode() {
		return patientUuid.hashCode();
	}
}
