package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;

import java.util.Collections;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class FindPatientQueryBuilderTest {
	
	@InjectMocks
	private FindPatientQueryBuilder queryMapper = new FindPatientQueryBuilder();
	
	@Mock
	private IdentifierBuilder identifierBuilder;
	
	@Mock
	private PatientIdentifier ecidIdentifier;
	
	@Mock
	private PatientIdentifier openMrsIdentifier;
	
	@Mock
	private PatientIdentifier openEmpiIdentifier;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testConvertFromPatientToQuery() throws Exception {
		String givenName = "Michael";
		String FamilyName = "Louis";
		Patient patient = createPatient(givenName, FamilyName);
		
		OpenEmpiPatientResult query = queryMapper.create(patient);
		
		assertEquals(patient.getGivenName(), query.getGivenName());
		assertEquals(patient.getFamilyName(), query.getFamilyName());
	}
	
	private Patient createPatient(String givenName, String familyName) {
		Patient patient = new Patient();
		PersonName personName = new PersonName();
		personName.setGivenName(givenName);
		personName.setFamilyName(familyName);
		patient.setNames(new TreeSet<PersonName>(Collections.singleton(personName)));
		return patient;
	}
}
