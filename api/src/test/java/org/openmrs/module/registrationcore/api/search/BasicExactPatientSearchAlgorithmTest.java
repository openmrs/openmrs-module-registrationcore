package org.openmrs.module.registrationcore.api.search;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.registrationcore.api.BaseRegistrationCoreSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class BasicExactPatientSearchAlgorithmTest extends BaseRegistrationCoreSensitiveTest {
	
	@Autowired
	private BasicExactPatientSearchAlgorithm algorithm;
	
	@Autowired
	@Qualifier("personService")
	private PersonService personService;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Test
	public void shouldMatchOnExactMatch() throws Exception {
		// to make sure we're getting the right match, create another patient with the same gender and birthdate
		Patient other = new Patient();
		other.addIdentifier(new PatientIdentifier("12321", new PatientIdentifierType(2), new Location(1)));
		other.addName(new PersonName("Bob", null, "Notthesame"));
		other.setGender("M");
		other.setBirthdate(date("1975-04-08"));
		patientService.savePatient(other);
		
		Patient patient = buildPatient(buildPersonName(), "M", date("1975-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).getPatient().getPatientId(), is(2));
	}
	
	@Test
	public void shouldNotMatchPartialName() throws Exception {
		PersonName name = buildPersonName();
		name.setFamilyName("Horn");
		Patient patient = buildPatient(name, "M", date("1975-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(0));
	}
	
	@Test
	public void shouldMatchWithBlankNameComponents() throws Exception {
		PersonName name = buildPersonName();
		name.setMiddleName("");
		Patient patient = buildPatient(name, "M", date("1975-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).getPatient().getPatientId(), is(2));
	}
	
	@Test
	public void shouldMatchWithBlankNameComponentsInDatabase() throws Exception {
		PersonName name = buildPersonName();
		name.setDegree("III");
		Patient patient = buildPatient(name, "M", date("1975-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).getPatient().getPatientId(), is(2));
	}
	
	@Test
	public void shouldNotMatchDifferentGender() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "F", date("1975-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(0));
	}
	
	@Test
	public void shouldMatchCloseBirthdate() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "M", date("1975-04-01"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).getPatient().getPatientId(), is(2));
	}
	
	@Test
	public void shouldNotMatchFarBirthdate() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "M", date("1970-04-08"));
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, null, 0d, null);
		assertThat(matches.size(), is(0));
	}
	
	@Test
	public void shouldNotMatchForFarEstimatedBirthdate() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "M", null);
		
		Map<String, Object> otherDataPoints = new HashMap<String, Object>();
		otherDataPoints.put("birthdateYears", 80);
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, otherDataPoints, 0d, null);
		assertThat(matches.size(), is(0));
	}
	
	@Test
	public void shouldMatchForCloseEstimatedBirthYear() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "M", null);
		
		Map<String, Object> otherDataPoints = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		otherDataPoints.put("birthdateYears", cal.get(Calendar.YEAR) - 1975);
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, otherDataPoints, 0d, null);
		assertThat(matches.size(), is(1));
	}
	
	@Test
	public void shouldMatchForCloseEstimatedBirthMonth() throws Exception {
		Patient patient = buildPatient(buildPersonName(), "M", null);
		
		Map<String, Object> otherDataPoints = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		otherDataPoints.put("birthdateMonth", (cal.get(Calendar.YEAR) - 1975) * 12); // kind of cheat, should really only have months < 12
		
		List<PatientAndMatchQuality> matches = algorithm.findSimilarPatients(patient, otherDataPoints, 0d, null);
		assertThat(matches.size(), is(1));
	}
	
	private PersonName buildPersonName() {
		PersonName name = new PersonName();
		name.setPrefix("Mr.");
		name.setGivenName("Horatio");
		name.setMiddleName("Test");
		name.setFamilyName("Hornblower");
		name.setFamilyNameSuffix("Esq.");
		return name;
	}
	
	private Date date(String ymd) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd").parse(ymd);
	}
	
	private Patient buildPatient(PersonName name, String gender, Date birthdate) {
		Patient patient = new Patient();
		patient.addName(name);
		patient.setGender(gender);
		patient.setBirthdate(birthdate);
		patient.setBirthdateEstimated(false);
		
		return patient;
	}
	
}
