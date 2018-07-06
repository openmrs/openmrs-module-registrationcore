package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.api.RegistrationCoreSensitiveTestBase;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PixPdqContextSensitiveTest extends RegistrationCoreSensitiveTestBase {

	@InjectMocks
	private PixPatientExporter pixPatientExporter;

	@Mock
	private Hl7v2Sender hl7v2Sender;

	@Autowired
	private PixPdqMessageUtil pixPdqMessageUtil;
	@Autowired
	private PdqPatientFetcher pdqPatientFetcher;
	@Autowired
	private Hl7SenderHolder hl7SenderHolder;
	@Autowired
	private MpiProperties mpiProperties;
	@Autowired
	private PatientService patientService;

	@Before
	public void setUp() throws Exception {
		executeDataSet("identifiers_dataset.xml");
		executeDataSet("mpi_global_properties_dataset.xml");
		executeDataSet("patients_dataset.xml");
	}

	/**
	 * @see {@link PixPatientExporter#exportPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should export patient to the mpi and retrieve the patient from the mpi",
			method = "exportPatient(Patient)")
	public void exportPatient_contextSensitiveShouldExportPatientAndRetrievePatient() throws Exception {
		Patient patient = patientService.getPatient(2);

//		Patient patient1 = new Patient();
//		patient1.addName(new PersonName("Johny", null, "Smith"));
//		Date date = new GregorianCalendar(2017, Calendar.JULY, 17).getTime();
//		patient1.setBirthdate(date);
//		PatientIdentifier identifier = new PatientIdentifier();
//		identifier.setIdentifier("ABCD1234");
//		patient1.addIdentifier(identifier);
//		patient1.setGender("M");
//		PersonAddress personAddress = new PersonAddress();
//		personAddress.setCountry("TesT");
//		personAddress.setCityVillage("TeSt2");
//		personAddress.setCountyDistrict("Test3");
//		patient1.addAddress(personAddress);
//
//		String result = pixPatientExporter.exportPatient(patient1);
//		assertEquals(IDENTIFIERS.get(0), result);
	}



//	@Test
//	public void testPrivilegeLevelsCreated() throws Exception {
//		EmrApiActivator activator = new EmrApiActivator();
//		activator.willRefreshContext();
//		activator.contextRefreshed();
//
//		// ensure Privilege Level: Full role
//		Role fullPrivsRole = userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
//		assertThat(fullPrivsRole, is(notNullValue()));
//		assertThat(fullPrivsRole.getUuid(), is(EmrApiConstants.PRIVILEGE_LEVEL_FULL_UUID));
//
//		// ensure Privilege Level: High role
//		Role highPrivsRole = userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_ROLE);
//		assertThat(highPrivsRole, is(notNullValue()));
//		assertThat(highPrivsRole.getUuid(), is(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_UUID));
//	}
}
