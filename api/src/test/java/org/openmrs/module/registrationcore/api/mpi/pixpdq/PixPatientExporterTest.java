package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.test.Verifies;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class PixPatientExporterTest {
	
	private List<MpiPatient> RET_VAL = new ArrayList<MpiPatient>();
	
	private List<String> IDENTIFIERS = new ArrayList<String>();
	
	private PatientIdentifierType PID_TYPE = new PatientIdentifierType();
	
	@InjectMocks
	private PixPatientExporter pixPatientExporter;
	
	@Mock
	private PixPdqMessageUtil pixPdqMessageUtil;
	
	@Mock
	private PdqPatientFetcher pdqPatientFetcher;
	
	@Mock
	private Hl7v2Sender hl7v2Sender;
	
	@Mock
	private RegistrationCoreProperties registrationCoreProperties;
	
	@Mock
	private MpiProperties mpiProperties;
	
	@Mock
	private PatientService patientService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		initData();
		initPixPdqMessageUtil();
		initPdqPatientFetcher();
		initHl7SenderHolder();
		initMpiProperties();
		initPatientService();
	}
	
	/*
	 * Initialize data that is returned every time pixPdqMessageUtil.interpretPIDSegments is called
	 */
	private void initData() {
		IDENTIFIERS.add("ABCD1234");
		PID_TYPE.setUuid("XYZ");
		
		MpiPatient patient1 = new MpiPatient();
		patient1.addName(new PersonName("Johny", "Apple", "Smith"));
		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient1.setBirthdate(date);
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier(IDENTIFIERS.get(0));
		identifier.setIdentifierType(PID_TYPE);
		patient1.addIdentifier(identifier);
		patient1.setGender("M");
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCountry("TesT");
		personAddress.setCityVillage("TeSt2");
		personAddress.setCountyDistrict("Test3");
		patient1.addAddress(personAddress);
		RET_VAL.add(patient1);
	}
	
	private void initPixPdqMessageUtil() {
		try {
			ADT_A01 message = new ADT_A01();
			when(pixPdqMessageUtil.createAdmit(Mockito.any(Patient.class))).thenReturn(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initPdqPatientFetcher() {
		try {
			when(pdqPatientFetcher.fetchMpiPatient(Mockito.any(PatientIdentifier.class))).thenReturn(RET_VAL.get(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initHl7SenderHolder() {
		try {
			when(registrationCoreProperties.getBeanFromName(Mockito.anyString())).thenReturn(hl7v2Sender);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initMpiProperties() {
		try {
			when(mpiProperties.getMpiPersonIdentifierTypeUuid()).thenReturn("XYZ");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initPatientService() {
		try {
			when(patientService.getPatientIdentifierTypeByUuid("XYZ")).thenReturn(PID_TYPE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see {@link PixPatientExporter#exportPatient(Patient)}
	 */
	@Test(expected = MpiException.class)
	@Verifies(value = "should fail to export patient to the mpi", method = "exportPatient(Patient)")
	public void exportPatient_shouldFailExportPatient() throws Exception {
		MpiPatient patient1 = new MpiPatient();
		patient1.addName(new PersonName("Johny", null, "Smith"));
		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
		patient1.setBirthdate(date);
		PatientIdentifier identifier = new PatientIdentifier();
		identifier.setIdentifier("ABCD1234");
		patient1.addIdentifier(identifier);
		patient1.setGender("M");
		PersonAddress personAddress = new PersonAddress();
		personAddress.setCountry("TesT");
		personAddress.setCityVillage("TeSt2");
		personAddress.setCountyDistrict("Test3");
		patient1.addAddress(personAddress);
		
		String result = pixPatientExporter.exportPatient(patient1);
		assertEquals(IDENTIFIERS.get(0), result);
	}
}
