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

public class BasicSimilarPatientSearchAlgorithmTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("registrationcore.BasicSimilarPatientSearchAlgorithm")
	SimilarPatientSearchAlgorithm patientSearch;

	@Autowired
	@Qualifier("registrationcore.NamePhoneticsPatientSearchAlgorithm")
	SimilarPatientSearchAlgorithm namePhoneticsPatientSearch;
	
	@Autowired
	@Qualifier("personService")
	PersonService personService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("patients_dataset.xml");
	}
	
	/**
	 * @see BasicSimilarPatientSearchAlgorithm#findSimilarPatients(Patient,Integer)
	 * @verifies find by birthday within two weeks
	 */
	@Test
	public void findSimilarPatients_shouldFindByBirthdayWithinTwoWeeks() throws Exception {
		Person existingPatient = personService.getPerson(17);
		
		Patient patient = new Patient();
		long twoWeeks = 3600000L * 24 * 7 * 2;
		patient.setBirthdate(new Date(existingPatient.getBirthdate().getTime() - twoWeeks));
		
		List<PatientAndMatchQuality> results = patientSearch.findSimilarPatients(patient, null, null, 10);
		
		assertThat(results.size(), is(1));
		Patient foundPatient = results.iterator().next().getPatient();
		assertThat(foundPatient, is(existingPatient));
	}
	
	/**
	 * @see BasicSimilarPatientSearchAlgorithm#findSimilarPatients(Patient,Integer)
	 * @verifies find by exact country and exact city
	 */
	@Test
	public void findSimilarPatients_shouldFindByExactCountryAndExactCity() throws Exception {
		Patient patientInUSA = new Patient();
		PersonAddress addressInUSA = new PersonAddress();
		addressInUSA.setCountry("USA");
		patientInUSA.addAddress(addressInUSA);
		
		List<PatientAndMatchQuality> patientsInUSA = patientSearch.findSimilarPatients(patientInUSA, null, null, 10);
		
		assertThat(patientsInUSA.size(), is(1));
		
		PersonAddress personAddress = personService.getPersonAddressByUuid("7418fe01-e7e0-4f0c-ad1b-4b4d845577f9");
		personAddress.setCountry("USA");
		personService.savePerson(personAddress.getPerson());
		
		Patient patient = new Patient();
		
		PersonAddress address = new PersonAddress();
		address.setCountry("USA");
		address.setCityVillage("Indianapolis");
		
		patient.addAddress(address);
		
		List<PatientAndMatchQuality> results = patientSearch.findSimilarPatients(patient, null, null, 10);
		
		assertThat(results.size(), is(1));
		Patient result = results.iterator().next().getPatient();
		assertThat(result.getPersonAddress().getCountry(), is("USA"));
		assertThat(result.getPersonAddress().getCityVillage(), is("Indianapolis"));
	}
	
	/**
	 * @see BasicSimilarPatientSearchAlgorithm#findSimilarPatients(Patient,Integer)
	 * @verifies find by exact gender
	 */
	@Test
	public void findSimilarPatients_shouldFindByExactGender() throws Exception {
		Patient patient = new Patient();
		
		patient.setGender("M");
		
		List<PatientAndMatchQuality> results = patientSearch.findSimilarPatients(patient, null, null, 10);
		
		assertThat(results.size(), is(10));
		for (PatientAndMatchQuality result : results) {
			assertThat(result.getPatient().getGender(), is("M"));
		}
	}
	
	/**
	 * @see BasicSimilarPatientSearchAlgorithm#findSimilarPatients(Patient,Integer)
	 * @verifies find by partial given and partial family name
	 */
	@Test
	public void findSimilarPatients_shouldFindByPartialGivenAndPartialFamilyName() throws Exception {
		Patient patient = new Patient();
		
		PersonName name = new PersonName();
		patient.addName(name);
		name.setGivenName("Sh");
		
		List<PatientAndMatchQuality> results = patientSearch.findSimilarPatients(patient, null, null, 10);
		
		assertThat(results.size(), is(2));
		for (PatientAndMatchQuality result : results) {
			assertThat(result.getPatient().getPersonName().getGivenName(), startsWith("Sh"));
		}
		
		name.setFamilyName("Lenye");
		
		List<PatientAndMatchQuality> finalResults = patientSearch.findSimilarPatients(patient, null, null, 10);
		
		assertThat(finalResults.size(), is(2));
		Patient bestPatient = finalResults.iterator().next().getPatient();
		assertThat(bestPatient.getPersonName().getGivenName(), is("Sharolyne"));
		assertThat(bestPatient.getPersonName().getFamilyName(), is("Lenye"));
	}

    @Test
    public void findSimilarPatients_shouldReturnEmptyListIfNoDataToQueryOn() throws Exception {
        Patient patient = new Patient();
        assertThat(patientSearch.findSimilarPatients(patient, null, null, 10).size(), is(0));

    }

	/**
	 * @see BasicSimilarPatientSearchAlgorithm#findSimilarPatients(Patient,Integer)
	 * @verifies find by phonetic name match
	 */
	@Test
	public void findSimilarPatients_shouldFindByPhoneticNameMatch() throws Exception {
		executeDataSet("name_phonetics_dataset.xml");

		Patient patient = new Patient();

		PersonName name = new PersonName();
		patient.addName(name);
		name.setGivenName("Jarusz");
		name.setFamilyName("Rapondee");

		List<PatientAndMatchQuality> results = namePhoneticsPatientSearch.findSimilarPatients(patient, null, null, 10);

		assertThat(results.size(), is(1));
		assertThat(results.get(0).getPatient().getPersonName().getGivenName(), is("Jarus"));
		assertThat(results.get(0).getPatient().getPersonName().getFamilyName(), is("Rapondi"));
	}
}
