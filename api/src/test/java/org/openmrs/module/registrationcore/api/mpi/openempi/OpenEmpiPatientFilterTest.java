package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenEmpiPatientFilterTest {
	
	public static final String PERSON_IDENTIFIER_TYPE_UUID = "5";
	
	private static final String PERSON_IDENTIFIER = "4";
	
	private List<PatientAndMatchQuality> patients;
	
	@Mock
	private MpiProperties mpiProperties;
	
	@Mock
	private PatientIdentifierMapper identifierMapper;
	
	@InjectMocks
	private OpenEmpiPatientFilter filter;
	
	@Mock
	private PatientAndMatchQuality patientWrapper;
	
	@Mock
	private PatientAndMatchQuality mpiPatientWrapper;
	
	@Before
	public void setUp() throws Exception {
		filter = new OpenEmpiPatientFilter();
		MockitoAnnotations.initMocks(this);
		mockProperty();
	}
	
	@Test
	public void testFilterPatientsWithSimilarIdentifiers() throws Exception {
		generatePatients(Patient.class, MpiPatient.class);
		
		filter.filter(patients);
		
		assertTrue(patients.contains(patientWrapper));
		assertFalse(patients.contains(mpiPatientWrapper));
	}
	
	private void mockProperty() {
		when(mpiProperties.getMpiPersonIdentifierTypeUuid()).thenReturn(PERSON_IDENTIFIER_TYPE_UUID);
	}
	
	private void generatePatients(Class<? extends Patient> firstPatientClass, Class<? extends Patient> secondPatientClass) {
		patients = new LinkedList<PatientAndMatchQuality>();
		
		patientWrapper = createPatient(firstPatientClass, patientWrapper);
		mpiPatientWrapper = createPatient(secondPatientClass, mpiPatientWrapper);
		
		patients.add(patientWrapper);
		patients.add(mpiPatientWrapper);
	}
	
	private PatientAndMatchQuality createPatient(Class<? extends Patient> patientClass,
	        PatientAndMatchQuality localPatientWrapper) {
		Patient patient = mock(patientClass);
		PatientIdentifier identifier = mock(PatientIdentifier.class);
		PatientIdentifierType patientIdentifierType = mock(PatientIdentifierType.class);
		when(patient.getIdentifiers()).thenReturn(new HashSet<PatientIdentifier>(Arrays.asList(identifier)));
		when(identifier.getIdentifier()).thenReturn(PERSON_IDENTIFIER);
		when(identifier.getIdentifierType()).thenReturn(patientIdentifierType);
		when(patientIdentifierType.getUuid()).thenReturn(PERSON_IDENTIFIER_TYPE_UUID);
		when(localPatientWrapper.getPatient()).thenReturn(patient);
		return localPatientWrapper;
	}
}
