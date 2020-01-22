package org.openmrs.module.registrationcore.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class IdentifierBuilderTest {
	
	private static final Integer OPENMRS_IDENTIFIER_SOURCE_ID = 1;
	
	private static final String OPENMRS_IDENTIFIER_SOURCE_STRING_ID = "1";
	
	private static final String OPENMRS_GENERATED_IDENTIFIER = "10012NF";
	
	private static final String PERSON_IDENTIFIER_TYPE_UUID = "2";
	
	private static final String CUSTOM_MPI_IDENTIFIER_VALUE = "16340061NF";
	
	@InjectMocks
	private IdentifierBuilder generator;
	
	@Mock
	private LocationService locationService;
	
	@Mock
	private IdentifierSourceService iss;
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private Location defaultLocation;
	
	@Mock
	private Location customLocation;
	
	@Mock
	private IdentifierSource identifierSource;
	
	@Mock
	private PatientIdentifierType identifierType;
	
	@Mock
	private PatientIdentifierType customMpiPatientIdentifierType;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGenerateIdentifierWithNullLocation() throws Exception {
		mockDefaultLocation(defaultLocation);
		mockIdentifierSource(identifierSource);
		mockGeneratedIdentifier();
		
		PatientIdentifier patientIdentifier = generator.generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_STRING_ID, null);
		
		verify(locationService).getDefaultLocation();
		verify(iss).getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID));
		verify(iss).generateIdentifier(identifierSource, null);
		assertEquals(patientIdentifier.getIdentifier(), OPENMRS_GENERATED_IDENTIFIER);
		assertEquals(patientIdentifier.getIdentifierType(), identifierType);
		assertEquals(patientIdentifier.getLocation(), defaultLocation);
	}
	
	private void mockDefaultLocation(Location location) {
		when(locationService.getDefaultLocation()).thenReturn(location);
	}
	
	private void mockIdentifierSource(IdentifierSource identifierSource) {
		when(iss.getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID))).thenReturn(identifierSource);
		if (identifierSource != null)
			when(identifierSource.getIdentifierType()).thenReturn(identifierType);
	}
	
	private void mockGeneratedIdentifier() {
		when(iss.generateIdentifier(identifierSource, null)).thenReturn(OPENMRS_GENERATED_IDENTIFIER);
	}
	
	@Test
	public void testGenerateIdentifierWithCustomLocation() throws Exception {
		mockDefaultLocation(defaultLocation);
		mockIdentifierSource(identifierSource);
		mockGeneratedIdentifier();
		
		PatientIdentifier patientIdentifier = generator.generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_STRING_ID,
		    customLocation);
		
		verify(locationService, never()).getDefaultLocation();
		verify(iss).getIdentifierSource(Integer.valueOf(OPENMRS_IDENTIFIER_SOURCE_ID));
		verify(iss).generateIdentifier(identifierSource, null);
		assertEquals(patientIdentifier.getIdentifier(), OPENMRS_GENERATED_IDENTIFIER);
		assertEquals(patientIdentifier.getIdentifierType(), identifierType);
		assertEquals(patientIdentifier.getLocation(), customLocation);
	}
	
	@Test(expected = APIException.class)
	public void testGenerateIdentifierThrowExceptionOnEmptyDefaultLocation() throws Exception {
		mockDefaultLocation(null);
		
		generator.generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_STRING_ID, null);
	}
	
	@Test(expected = APIException.class)
	public void testGenerateIdentifierThrowExceptionOnEmptyIdentifierSource() throws Exception {
		mockDefaultLocation(defaultLocation);
		mockIdentifierSource(null);
		
		generator.generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_STRING_ID, null);
	}
	
	@Test
	public void testCreateIdentifierWithNullLocation() throws Exception {
		mockDefaultLocation(defaultLocation);
		mockPatientIdentifierType(customMpiPatientIdentifierType);
		
		PatientIdentifier identifier = generator.createIdentifier(PERSON_IDENTIFIER_TYPE_UUID, CUSTOM_MPI_IDENTIFIER_VALUE,
		    null);
		
		verify(locationService).getDefaultLocation();
		verify(patientService).getPatientIdentifierTypeByUuid(PERSON_IDENTIFIER_TYPE_UUID);
		assertEquals(identifier.getIdentifier(), CUSTOM_MPI_IDENTIFIER_VALUE);
		assertEquals(identifier.getIdentifierType(), customMpiPatientIdentifierType);
		assertEquals(identifier.getLocation(), defaultLocation);
	}
	
	private void mockPatientIdentifierType(PatientIdentifierType customMpiPatientIdentifierType) {
		when(patientService.getPatientIdentifierTypeByUuid(PERSON_IDENTIFIER_TYPE_UUID))
		        .thenReturn(customMpiPatientIdentifierType);
	}
	
	@Test
	public void testCreateIdentifierWithCustomLocation() throws Exception {
		mockDefaultLocation(defaultLocation);
		mockPatientIdentifierType(customMpiPatientIdentifierType);
		
		PatientIdentifier identifier = generator.createIdentifier(PERSON_IDENTIFIER_TYPE_UUID, CUSTOM_MPI_IDENTIFIER_VALUE,
		    customLocation);
		
		verify(locationService, never()).getDefaultLocation();
		verify(patientService).getPatientIdentifierTypeByUuid(PERSON_IDENTIFIER_TYPE_UUID);
		assertEquals(identifier.getIdentifier(), CUSTOM_MPI_IDENTIFIER_VALUE);
		assertEquals(identifier.getIdentifierType(), customMpiPatientIdentifierType);
		assertEquals(identifier.getLocation(), customLocation);
	}
	
	@Test(expected = APIException.class)
	public void testCreateIdentifierThrowExceptionOnEmptyDefaultLocation() throws Exception {
		mockDefaultLocation(null);
		
		generator.createIdentifier(PERSON_IDENTIFIER_TYPE_UUID, CUSTOM_MPI_IDENTIFIER_VALUE, null);
	}
	
	@Test
	public void generateIdentifier_shouldConsumeUuidAsSourceId() {
		String identifierSourceUuid = "0d47284f-9e9b-4a81-a88b-8bb42bc0a901";
		mockDefaultLocation(defaultLocation);
		when(iss.getIdentifierSourceByUuid(identifierSourceUuid)).thenReturn(identifierSource);
		when(identifierSource.getIdentifierType()).thenReturn(identifierType);
		mockGeneratedIdentifier();
		
		PatientIdentifier patientIdentifier = generator.generateIdentifier(identifierSourceUuid, null);
		
		assertEquals(OPENMRS_GENERATED_IDENTIFIER, patientIdentifier.getIdentifier());
		assertEquals(identifierType, patientIdentifier.getIdentifierType());
		assertEquals(defaultLocation, patientIdentifier.getLocation());
	}
}
