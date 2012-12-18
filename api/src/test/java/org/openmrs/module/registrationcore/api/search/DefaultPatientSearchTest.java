package org.openmrs.module.registrationcore.api.search;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DefaultPatientSearchTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("registrationcore.DefaultPatientSearch")
	PatientSearch patientSearch;
	
	@Autowired
	@Qualifier("personService")
	PersonService personService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("openmrs_1_9_2_demo_patients.xml");
	}
	
	/**
	 * @see DefaultPatientSearch#findSimilarPatients(Patient,Integer)
	 * @verifies find by birthday within two weeks
	 */
	@Test
	public void findSimilarPatients_shouldFindByBirthdayWithinTwoWeeks() throws Exception {
		Person existingPatient = personService.getPerson(17);
		
		Patient patient = new Patient();
		long twoWeeks = 3600000L * 24 * 7 * 2;
		patient.setBirthdate(new Date(existingPatient.getBirthdate().getTime() - twoWeeks));
		
		List<Patient> results = patientSearch.findSimilarPatients(patient, 10);
		
		assertThat(results.size(), is(1));
		Patient foundPatient = results.iterator().next();
		assertThat(foundPatient, is(existingPatient));
	}
	
	/**
	 * @see DefaultPatientSearch#findSimilarPatients(Patient,Integer)
	 * @verifies find by exact country and exact city
	 */
	@Test
	public void findSimilarPatients_shouldFindByExactCountryAndExactCity() throws Exception {
		Patient patientInUSA = new Patient();
		PersonAddress addressInUSA = new PersonAddress();
		addressInUSA.setCountry("USA");
		patientInUSA.addAddress(addressInUSA);
		
		List<Patient> patientsInUSA = patientSearch.findSimilarPatients(patientInUSA, 10);
		
		assertThat(patientsInUSA.size(), is(1));
		
		PersonAddress personAddress = personService.getPersonAddressByUuid("7418fe01-e7e0-4f0c-ad1b-4b4d845577f9");
		personAddress.setCountry("USA");
		personService.savePerson(personAddress.getPerson());
		
		Patient patient = new Patient();
		
		PersonAddress address = new PersonAddress();
		address.setCountry("USA");
		address.setCityVillage("Indianapolis");
		
		patient.addAddress(address);
		
		List<Patient> results = patientSearch.findSimilarPatients(patient, 10);
		
		assertThat(results.size(), is(1));
		Patient result = results.iterator().next();
		assertThat(result.getPersonAddress().getCountry(), is("USA"));
		assertThat(result.getPersonAddress().getCityVillage(), is("Indianapolis"));
	}
	
	/**
	 * @see DefaultPatientSearch#findSimilarPatients(Patient,Integer)
	 * @verifies find by exact gender
	 */
	@Test
	public void findSimilarPatients_shouldFindByExactGender() throws Exception {
		Patient patient = new Patient();
		
		patient.setGender("M");
		
		List<Patient> results = patientSearch.findSimilarPatients(patient, 10);
		
		assertThat(results.size(), is(18));
		for (Patient result : results) {
			assertThat(result.getGender(), is("M"));
		}
	}
	
	/**
	 * @see DefaultPatientSearch#findSimilarPatients(Patient,Integer)
	 * @verifies find by partial given and partial family name
	 */
	@Test
	public void findSimilarPatients_shouldFindByPartialGivenAndPartialFamilyName() throws Exception {
		Patient patient = new Patient();
		
		PersonName name = new PersonName();
		patient.addName(name);
		name.setGivenName("Sh");
		
		List<Patient> results = patientSearch.findSimilarPatients(patient, 10);
		
		assertThat(results.size(), is(2));
		for (Patient result : results) {
			assertThat(result.getPersonName().getGivenName(), startsWith("Sh"));
		}
		
		name.setFamilyName("Lenye");
		
		List<Patient> finalResults = patientSearch.findSimilarPatients(patient, 10);
		
		assertThat(finalResults.size(), is(1));
		Patient existingPatient = finalResults.iterator().next();
		assertThat(existingPatient.getPersonName().getGivenName(), is("Sharolyne"));
		assertThat(existingPatient.getPersonName().getFamilyName(), is("Lenye"));
	}
	
	/**
	 * @see DefaultPatientSearch#findDuplicatePatients(Patient,Integer)
	 * @verifies find by birthday within three days
	 */
	@Test
	public void findDuplicatePatients_shouldFindByBirthdayWithinThreeDays() throws Exception {
		Person existingPatient = personService.getPerson(17);
		
		Patient patient = new Patient();
		long threeDays = 3600000L * 24 * 3;
		patient.setBirthdate(new Date(existingPatient.getBirthdate().getTime() - threeDays));
		
		List<Patient> results = patientSearch.findDuplicatePatients(patient, 10);
		
		assertThat(results.size(), is(1));
		Patient foundPatient = results.iterator().next();
		assertThat(foundPatient, is(existingPatient));
	}
	
	/**
	 * @see DefaultPatientSearch#findDuplicatePatients(Patient,Integer)
	 * @verifies find by exact country and exact city
	 */
	@Test
	public void findDuplicatePatients_shouldFindByExactCountryAndExactCity() throws Exception {
		Patient patientInUSA = new Patient();
		PersonAddress addressInUSA = new PersonAddress();
		addressInUSA.setCountry("USA");
		patientInUSA.addAddress(addressInUSA);
		
		List<Patient> patientsInUSA = patientSearch.findDuplicatePatients(patientInUSA, 10);
		
		assertThat(patientsInUSA.size(), is(1));
		
		PersonAddress personAddress = personService.getPersonAddressByUuid("7418fe01-e7e0-4f0c-ad1b-4b4d845577f9");
		personAddress.setCountry("USA");
		personService.savePerson(personAddress.getPerson());
		
		Patient patient = new Patient();
		
		PersonAddress address = new PersonAddress();
		address.setCountry("USA");
		address.setCityVillage("Indianapolis");
		
		patient.addAddress(address);
		
		List<Patient> results = patientSearch.findDuplicatePatients(patient, 10);
		
		assertThat(results.size(), is(1));
		Patient result = results.iterator().next();
		assertThat(result.getPersonAddress().getCountry(), is("USA"));
		assertThat(result.getPersonAddress().getCityVillage(), is("Indianapolis"));
	}
	
	/**
	 * @see DefaultPatientSearch#findDuplicatePatients(Patient,Integer)
	 * @verifies find by exact gender
	 */
	@Test
	public void findDuplicatePatients_shouldFindByExactGender() throws Exception {
		Patient patient = new Patient();
		
		patient.setGender("M");
		
		List<Patient> results = patientSearch.findDuplicatePatients(patient, 10);
		
		assertThat(results.size(), is(18));
		for (Patient result : results) {
			assertThat(result.getGender(), is("M"));
		}
	}
	
	/**
	 * @see DefaultPatientSearch#findDuplicatePatients(Patient,Integer)
	 * @verifies find by exact given and exact family name
	 */
	@Test
	public void findDuplicatePatients_shouldFindByExactGivenAndExactFamilyName() throws Exception {
		Patient patient = new Patient();
		
		PersonName name = new PersonName();
		patient.addName(name);
		name.setGivenName("Sh");
		
		List<Patient> results = patientSearch.findDuplicatePatients(patient, 10);
		
		assertThat(results.size(), is(0));
		
		name.setGivenName("Sharolyne");
		name.setFamilyName("Lenye");
		
		List<Patient> finalResults = patientSearch.findDuplicatePatients(patient, 10);
		
		assertThat(finalResults.size(), is(1));
		Patient existingPatient = finalResults.iterator().next();
		assertThat(existingPatient.getPersonName().getGivenName(), is("Sharolyne"));
		assertThat(existingPatient.getPersonName().getFamilyName(), is("Lenye"));
	}
}
