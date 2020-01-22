package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PdqSimilarPatientsSearcherTest {
	
	private List<MpiPatient> RET_VAL = new ArrayList<MpiPatient>();
	
	@InjectMocks
	private PdqSimilarPatientsSearcher pdqSimilarPatientsSearcher;
	
	@Mock
	private PixPdqMessageUtil pixPdqMessageUtil;
	
	@Mock
	private Hl7v2Sender hl7v2Sender;
	
	@Mock
	private RegistrationCoreProperties registrationCoreProperties;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		initData();
		initPixPdqMessageUtil();
		initRegistrationCoreProperties();
	}
	
	/**
	 * Initialize data that is returned every time pixPdqMessageUtil.interpretPIDSegments is called
	 */
	private void initData() {
		MpiPatient patient1 = new MpiPatient();
		patient1.addName(new PersonName("Johny", "Apple", "Smith"));
		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient1.setBirthdate(date);
		patient1.setGender("M");
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCountry("TesT");
		personAddress.setCityVillage("TeSt2");
		personAddress.setCountyDistrict("Test3");
		patient1.addAddress(personAddress);
		RET_VAL.add(patient1);
		
		MpiPatient patient2 = new MpiPatient();
		patient2.addName(new PersonName("tOM", null, "SmItH"));
		Date date2 = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient2.setBirthdate(date2);
		patient2.setGender("M");
		personAddress.setCountry("TEST");
		personAddress.setCityVillage("TesT2");
		personAddress.setCountyDistrict("teST3");
		patient2.addAddress(personAddress);
		RET_VAL.add(patient2);
		
		MpiPatient patient3 = new MpiPatient();
		PatientIdentifier identifierA = new PatientIdentifier();
		identifierA.setIdentifier("ABCD1234");
		patient3.addIdentifier(identifierA);
		patient3.setGender("F");
		personAddress.setCountry("TEST");
		personAddress.setCityVillage("TesT2");
		personAddress.setCountyDistrict("teST3");
		patient3.addAddress(personAddress);
		RET_VAL.add(patient3);
	}
	
	private void initPixPdqMessageUtil() {
		try {
			when(pixPdqMessageUtil.interpretPIDSegments(Mockito.any(Message.class))).thenReturn(RET_VAL);
			List<Map.Entry<String, String>> queryParams = new ArrayList<Map.Entry<String, String>>();
			queryParams.add(new AbstractMap.SimpleEntry("@PID.5.1", "Test"));
			when(pixPdqMessageUtil.patientToQPD3Params(Mockito.any(Patient.class))).thenReturn(queryParams);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initRegistrationCoreProperties() {
		try {
			when(registrationCoreProperties.getBeanFromName(Mockito.anyString())).thenReturn(hl7v2Sender);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void findSimilarMatches_shouldMatchName() throws Exception {
		Patient patient = new Patient();
		patient.addName(new PersonName("Johny", null, "Smith"));
		
		List<PatientAndMatchQuality> result = pdqSimilarPatientsSearcher.findSimilarMatches(patient, null, null, 10);
		assertEquals(2, result.get(0).getMatchedFields().size());
	}
	
	@Test
	public void findSimilarMatches_shouldMatchNameAndBirthday() throws Exception {
		Patient patient2 = new Patient();
		patient2.addName(new PersonName("Johny", null, "Smith"));
		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient2.setBirthdate(date);
		
		List<PatientAndMatchQuality> result = pdqSimilarPatientsSearcher.findSimilarMatches(patient2, null, null, 10);
		assertEquals(3, result.get(0).getMatchedFields().size());
	}
	
	@Test
	public void findSimilarMatches_shouldMatchNameBirthdayAndAddress() throws Exception {
		Patient patient3 = new Patient();
		patient3.addName(new PersonName("Johny", null, "Smith"));
		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient3.setBirthdate(date);
		patient3.setGender("M");
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCountry("TesT");
		personAddress.setCityVillage("TeSt2");
		personAddress.setCountyDistrict("Test3");
		patient3.addAddress(personAddress);
		
		List<PatientAndMatchQuality> result = pdqSimilarPatientsSearcher.findSimilarMatches(patient3, null, null, 10);
		assertEquals(7, result.get(0).getMatchedFields().size());
	}
	
	@Test
	public void findSimilarMatches_shouldBeCaseInsensitive() throws Exception {
		Patient patient4 = new Patient();
		patient4.addName(new PersonName("tom", null, "smith"));
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCountry("test");
		personAddress.setCityVillage("TEST2");
		personAddress.setCountyDistrict("TeSt3");
		patient4.addAddress(personAddress);
		
		List<PatientAndMatchQuality> result = pdqSimilarPatientsSearcher.findSimilarMatches(patient4, null, null, 10);
		assertEquals(5, result.get(1).getMatchedFields().size());
	}
}
