package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;

import java.util.Collections;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class PatientQueryMapperTest {
    private static final String PATIENT_WITH_OPENMRS_ID_PERSON_ID = "1";
    private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";
    private static final String PATIENT_WITHOUT_OPENMRS_ID = "patient_without_openmrs_id.xml";

    private static final String OPEMMRS_IDENTIFIER_NAME = "OpenMRS";
    private static final String OPENEMPI_IDENTIFIER_NAME = "OpenEMPI"; //OpenEmpu is custom OpenEmpi identifier.
    private static final String ECID_IDENTIFIER_NAME = "ECID"; //Ecid is custom OpenEMPI identifier.

    private static final int OPENMRS_IDENTIFIER_TYPE_ID = 3;
    private static final int OPENEMPI_IDENTIFIER_TYPE_ID = 4;
    private static final int ECID_IDENTIFIER_TYPE_ID = 5;

    private static final String OPENMRS_IDENTIFIER_VALUE = "1001NF";
    private static final String OPENEMPI_IDENTIFIER_VALUE = "1";
    private static final String ECID_IDENTIFIER_VALUE = "019bb820-1bd3-11e5-8a7f-040158db6201";

    private static final int OPENMRS_IDENTIFIER_SOURCE_ID = 1;

    private XmlMarshaller xmlMarshaller = new XmlMarshaller();
    @InjectMocks private PatientQueryMapper queryMapper = new PatientQueryMapper();
    @Mock private IdentifierBuilder identifierBuilder;

    @Mock private PatientIdentifier ecidIdentifier;
    @Mock private PatientIdentifier openMrsIdentifier;
    @Mock private PatientIdentifier openEmpiIdentifier;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockIdentifierGeneratorGetIdentifierId();
        mockIdentifierGeneratorCreation();

    }

    @Test
    public void testConvertFromPatientToQuery() throws Exception {
        String givenName = "Michael";
        String FamilyName = "Louis";
        Patient patient = createPatient(givenName, FamilyName);

        OpenEmpiPatientQuery query = queryMapper.convert(patient);

        assertEquals(patient.getGivenName(), query.getGivenName());
        assertEquals(patient.getFamilyName(), query.getFamilyName());
    }

    @Test
    public void testCorrectConvertingPatientWithOpenMRSID() throws Exception {
        OpenEmpiPatientQuery patientQuery = readPatient(PATIENT_WITH_OPENMRS_ID);

        Patient convertedPatient = queryMapper.convert(patientQuery);

        //verify correct querying to IdentifierBuilder class:
        verify(identifierBuilder).createIdentifier(ECID_IDENTIFIER_TYPE_ID, ECID_IDENTIFIER_VALUE, null);
        verify(identifierBuilder).createIdentifier(OPENMRS_IDENTIFIER_TYPE_ID, OPENMRS_IDENTIFIER_VALUE, null);
        verify(openMrsIdentifier).setPreferred(true);

        //verify actual fields:
        assertPatientEquals(patientQuery, convertedPatient);
        assertTrue(convertedPatient.getIdentifiers().contains(openMrsIdentifier));
        assertTrue(convertedPatient.getIdentifiers().contains(ecidIdentifier));
        assertEquals(convertedPatient.getUuid(), PATIENT_WITH_OPENMRS_ID_PERSON_ID);
    }

    @Test
    public void testCorrectImportPatientWithoOpenMrsId() throws Exception {
        OpenEmpiPatientQuery patientQuery = readPatient(PATIENT_WITH_OPENMRS_ID);
        mockIdentifierGenerating();

        Patient convertedPatient = queryMapper.importPatient(patientQuery);

        //verify correct querying to IdentifierBuilder class:
        verify(identifierBuilder).createIdentifier(ECID_IDENTIFIER_TYPE_ID, ECID_IDENTIFIER_VALUE, null);
        verify(identifierBuilder).createIdentifier(OPENMRS_IDENTIFIER_TYPE_ID, OPENMRS_IDENTIFIER_VALUE, null);
        verify(openMrsIdentifier).setPreferred(true);
        //verify that wasn't interaction with generating identifier method:
        verify(identifierBuilder, never()).generateIdentifier(anyInt(), any(Location.class));

        //verify actual fields:
        assertPatientEquals(patientQuery, convertedPatient);
        assertTrue(convertedPatient.getIdentifiers().contains(openMrsIdentifier));
        assertTrue(convertedPatient.getIdentifiers().contains(ecidIdentifier));
    }

    @Test
    public void testCorrectImportPatientWithoutOpenMrsId() throws Exception {
        OpenEmpiPatientQuery patientQuery = readPatient(PATIENT_WITHOUT_OPENMRS_ID);
        mockIdentifierGenerating();

        Patient convertedPatient = queryMapper.importPatient(patientQuery);

        //verify correct querying to IdentifierBuilder class:
        verify(identifierBuilder).generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_ID, null);
        verify(openMrsIdentifier).setPreferred(true);

        //verify actual fields:
        assertPatientEquals(patientQuery, convertedPatient);
        assertTrue(convertedPatient.getIdentifiers().contains(openMrsIdentifier));
        assertTrue(convertedPatient.getIdentifiers().contains(ecidIdentifier));
        assertTrue(convertedPatient.getIdentifiers().contains(openEmpiIdentifier));
    }

    private void assertPatientEquals(OpenEmpiPatientQuery mpiPatient, Patient savedPatient) {
        assertEquals(mpiPatient.getGivenName(), savedPatient.getGivenName());
        assertEquals(mpiPatient.getFamilyName(), savedPatient.getFamilyName());
        assertEquals(mpiPatient.getGender().getGenderCode(), savedPatient.getGender());
    }

    private Patient createPatient(String givenName, String familyName) {
        Patient patient = new Patient();
        PersonName personName = new PersonName();
        personName.setGivenName(givenName);
        personName.setFamilyName(familyName);
        patient.setNames(new TreeSet<PersonName>(Collections.singleton(personName)));
        return patient;
    }

    private OpenEmpiPatientQuery readPatient(String path) throws Exception {
        return xmlMarshaller.getQuery(path);
    }

    private void mockIdentifierGeneratorGetIdentifierId() {
        when(identifierBuilder.getIdentifierIdByName(OPEMMRS_IDENTIFIER_NAME)).thenReturn(OPENMRS_IDENTIFIER_TYPE_ID);
        when(identifierBuilder.getIdentifierIdByName(ECID_IDENTIFIER_NAME)).thenReturn(ECID_IDENTIFIER_TYPE_ID);
        when(identifierBuilder.getIdentifierIdByName(OPENEMPI_IDENTIFIER_NAME)).thenReturn(OPENEMPI_IDENTIFIER_TYPE_ID);
    }

    private void mockIdentifierGeneratorCreation() {
        when(identifierBuilder.createIdentifier(eq(OPENMRS_IDENTIFIER_TYPE_ID), anyString(), any(Location.class)))
                .thenReturn(openMrsIdentifier);
        when(identifierBuilder.createIdentifier(eq(ECID_IDENTIFIER_TYPE_ID), anyString(), any(Location.class)))
                .thenReturn(ecidIdentifier);
        when(identifierBuilder.createIdentifier(eq(OPENEMPI_IDENTIFIER_TYPE_ID), anyString(), any(Location.class)))
                .thenReturn(openEmpiIdentifier);
    }

    private void mockIdentifierGenerating() {
        when(identifierBuilder.getOpenMrsIdentifierSourceId()).thenReturn(OPENMRS_IDENTIFIER_SOURCE_ID);
        when(identifierBuilder.generateIdentifier(OPENMRS_IDENTIFIER_SOURCE_ID, null)).thenReturn(openMrsIdentifier);
    }
}