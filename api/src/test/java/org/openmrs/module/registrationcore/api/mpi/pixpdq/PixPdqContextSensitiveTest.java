package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.BaseRegistrationCoreSensitiveTest;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PixPdqContextSensitiveTest extends BaseRegistrationCoreSensitiveTest {
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
	@Test (expected = MpiException.class)
	@Verifies(value = "should not export patient to the mpi",
			method = "exportPatient(Patient)")
	public void exportPatient_ContextSensitiveShouldFailExportPatient() throws Exception {
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
		List<MpiPatient> mpiPatients = pixPdqMessageUtil.interpretPIDSegments(PDQ_RESPONSE);

		assertEquals(patient.getIdentifiers().toString(), mpiPatients.get(0).getIdentifiers().toString());
		assertEquals(patient.getAddresses().toString(), mpiPatients.get(0).getAddresses().toString());
		assertEquals(patient.getGender(), mpiPatients.get(0).getGender());
		assertEquals(patient.getGivenName(), mpiPatients.get(0).getGivenName());
	}


	/**
	 * Note: Requires openmrs-core version 2.2.0-SNAPSHOT for constructor method to create patient from a provided patient
	 *
	 * @see <a href="https://issues.openmrs.org/browse/TRUNK-5408">TRUNK-5408</a>
	*/
	@Test
	@Ignore
	public void filterByIdentifierAndIdentifierType_ContextSensitiveShouldReturnEmptyList() throws Exception {
		List<MpiPatient> inputList = new ArrayList<MpiPatient>();


		inputList.add(new MpiPatient(patientService.getPatient(100)));
		inputList.add(new MpiPatient(patientService.getPatient(101)));
		inputList.add(new MpiPatient(patientService.getPatient(102)));
		List<MpiPatient> resultList = pixPdqMessageUtil.filterByIdentifierAndIdentifierType(
						inputList, "1000X1",
						patientService.getPatientIdentifierType(3).getUuid());
		assertEquals(true, resultList.isEmpty());
	}

	/**
	 * Note: Requires openmrs-core version 2.2.0-SNAPSHOT for constructor method to create patient from a provided patient
	 *
	 * @see <a href="https://issues.openmrs.org/browse/TRUNK-5408">TRUNK-5408</a>
	 */
	@Test
	@Ignore
	public void filterByIdentifierAndIdentifierType_ContextSensitiveShouldReturnPatient() throws Exception {
		List<MpiPatient> inputList = new ArrayList<MpiPatient>();
		inputList.add(new MpiPatient(patientService.getPatient(99)));
		inputList.add(new MpiPatient(patientService.getPatient(100)));
		inputList.add(new MpiPatient(patientService.getPatient(101)));
		inputList.add(new MpiPatient(patientService.getPatient(102)));
		List<MpiPatient> resultList = pixPdqMessageUtil.filterByIdentifierAndIdentifierType(
				inputList, "1000X1",
				patientService.getPatientIdentifierType(3).getUuid());
		assertEquals(1, resultList.size());
	}

	@Test
	public void patientToQPD3Params_ContextSensitiveShouldReturnQPD3Params() throws Exception {
		List<Map.Entry<String, String>>  result = pixPdqMessageUtil.patientToQPD3Params(patientService.getPatient(99));
		assertEquals(14, result.size());
	}

	@Test
	public void patientToQPD3Params_ContextSensitiveShouldHandleBlankDemographics() throws Exception {
		List<Map.Entry<String, String>>  result = pixPdqMessageUtil.patientToQPD3Params(patientService.getPatient(100));
		assertEquals(9, result.size());
	}

	@Test
	public void patientToQPD3Params_ContextSensitiveShouldReturnNullWhenPassedNullPatient() throws Exception {
		List<Map.Entry<String, String>>  result = pixPdqMessageUtil.patientToQPD3Params(null);
		assertNull(result);
	}

	@Test
	public void patientToQPD3Params_ContextSensitiveShouldReturnNameParams() throws Exception {
		List<Map.Entry<String, String>>  result = pixPdqMessageUtil.patientToQPD3Params(patientService.getPatient(103));
		assertEquals(2, result.size());
	}
}
