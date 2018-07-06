package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreSensitiveTestBase;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.model.Message;



import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class PixPdqContextSensitiveTest extends BaseModuleContextSensitiveTest {
	private Message PIX_RESPONSE;
	private Message PDQ_RESPONSE;

	@InjectMocks
	@Autowired
	private PixPatientExporter pixPatientExporter;

	@Mock
	private Hl7SenderHolder hl7SenderHolder;

	@Mock
	private Hl7v2Sender hl7v2Sender;

	@Autowired
	private PatientService patientService;

	@Autowired
	private PixPdqMessageUtil pixPdqMessageUtil;

	@InjectMocks
	@Autowired
	private PdqPatientFetcher pdqPatientFetcher;

	@Before
	public void setUp() throws Exception {
		executeDataSet("identifiers_dataset.xml");
		executeDataSet("mpi_global_properties_dataset.xml");
		executeDataSet("patients_dataset.xml");
		PipeParser parser = new PipeParser();
		String pixResponseString = "MSH|^~\\&|MESA_XREF|XYZ_HOSPITAL|OpenMRS|Demo|20180706150503-0400||ACK^A01|a0ad85c16470292a741|P|2.3.1\r"
				+ "MSA|AA|3f0684d2-f4d1-4b4e-8608-29db94b497e3\r";
		String pdqResponseString = "MSH|^~\\&|MESA_PD_SUPPLIER|XYZ_HOSPITAL|OpenMRS|Demo|20180706150504-0400||RSP^K22^RSP_K21|a0ad85c164702a18843|P|2.5\r"
				+ "MSA|AA|b0e566cd-e9c5-4afc-bbf1-03c153c71553\r"
				+ "QAK|cf4725fb-ce77-4b24-8cfa-f00c117d71ed|OK||1|1|0\r"
				+ "QPD|Q22^Find Candidates^HL7|cf4725fb-ce77-4b24-8cfa-f00c117d71ed|@PID.3.1^1000X1~@PID.3.4^2.16.840.1.113883.4.357\r"
				+ "PID|1||1000X1^^^2.25.71280592878078638113873461180761116318&2.25.71280592878078638113873461180761116318&PI^PI~5c11da40-812f-11e8-8a2c-06c4d099933a^^^2.16.840.1.113883.4.357&2.16.840.1.113883.4.357&hl7^PI||familyN99^givenN99^^^^^L||19550311|F|||addr1^addr2^city^state^1234^country\r";
		PDQ_RESPONSE = parser.parse(pdqResponseString);
		PIX_RESPONSE = parser.parse(pixResponseString);
	}

	/**
	 * @see {@link PixPatientExporter#exportPatient(Patient)}
	 */
	@Test
	@Verifies(value = "should export patient to the mpi and retrieve the patient from the mpi",
			method = "exportPatient(Patient)")
	public void exportPatient_ContextSensitiveShouldExportPatientAndRetrievePatient() throws Exception {
		Patient patient = patientService.getPatient(99);
		when(hl7SenderHolder.getHl7v2Sender()).thenReturn(hl7v2Sender);
		when(hl7v2Sender.sendPixMessage(Mockito.any(Message.class))).thenReturn(PIX_RESPONSE);
		when(hl7v2Sender.sendPdqMessage(Mockito.any(Message.class))).thenReturn(PDQ_RESPONSE);

		String result = pixPatientExporter.exportPatient(patient);
		assertEquals(patient.getPatientIdentifier(4).toString(), result);
	}

	@Test
	public void interpretPIDSegments_ContextSensitiveShouldReturnPatient() throws Exception {
		Patient patient = patientService.getPatient(99);
		List<Patient> mpiPatients = pixPdqMessageUtil.interpretPIDSegments(PDQ_RESPONSE);

		assertEquals(patient.getIdentifiers().toString(), mpiPatients.get(0).getIdentifiers().toString());
		assertEquals(patient.getAddresses().toString(), mpiPatients.get(0).getAddresses().toString());
		assertEquals(patient.getGender(), mpiPatients.get(0).getGender());
		assertEquals(patient.getGivenName(), mpiPatients.get(0).getGivenName());
	}
}
