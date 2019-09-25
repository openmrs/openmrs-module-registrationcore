package org.openmrs.module.registrationcore.api;

import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;


public class DefaultCreateUnknownPatientStrategyImpl implements CreateUnknownPatientStrategy {
		
	@Override
	public Patient createUnknownPatient() {
		Patient patient = new Patient();
		PersonName name = new PersonName();
		name.setFamilyName("UNKNOWN");
        name.setGivenName("UNKNOWN");
        patient.addName(name);
		return patient;
	}	
}
