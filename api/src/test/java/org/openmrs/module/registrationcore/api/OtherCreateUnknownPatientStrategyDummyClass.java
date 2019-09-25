package org.openmrs.module.registrationcore.api;
import org.openmrs.Patient;
import org.openmrs.PersonName;

/*Dummy class for testing another createUnknownPatientStrategy ie if implemented in a separate module*/
public class OtherCreateUnknownPatientStrategyDummyClass implements CreateUnknownPatientStrategy {
	@Override
	public Patient createUnknownPatient() {
		Patient patient = new Patient();
		PersonName name = new PersonName();
		name.setFamilyName("UNKNOWN_STRATEGY2");
        name.setGivenName("UNKNOWN_STRATEGY2");
        patient.addName(name);
		return patient;
	}	
}