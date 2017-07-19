package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.*;

public class OpenEmpiPatientResultBuilderTest {

    private static final Integer LOCAL_IDENTIFIER_TYPE_ID = 5;
    private static final Integer MPI_IDENTIFIER_TYPE_ID = 6;
    private static final String IDENTIFIER_VALUE = "identifierValue";

    private static final String FAMILY_NAME = "familyName";
    private static final String GIVEN_NAME = "givenName";
    private static final String MIDDLE_NAME = "middleName";
    private static final String GENDER_CODE = "M";
    private static final Date BIRTHDATE = new Date();
    private static final Integer PERSON_IDENTIFIER_TYPE = 6;

    @InjectMocks OpenEmpiPatientQueryBuilder queryBuilder;
    @Mock PatientIdentifierMapper identifierMapper;
    @Mock private MpiProperties mpiProperties;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockIdentifierMapper();
        when(mpiProperties.getMpiPersonIdentifierTypeId()).thenReturn(PERSON_IDENTIFIER_TYPE);
    }

    @Test
    public void testCorrectPatientConverting() throws Exception {
        Patient patient = createPatient();

        OpenEmpiPatientResult query = queryBuilder.build(patient);

        assertEquals(patient.getGender(), query.getGender().getGenderCode());

        assertEquals(patient.getFamilyName(), query.getFamilyName());
        assertEquals(patient.getGivenName(), query.getGivenName());
        assertEquals(patient.getMiddleName(), query.getMiddleName());

        assertEquals(patient.getBirthdate(), query.getDateOfBirth());

        PersonIdentifier personIdentifier = query.getPersonIdentifiers().get(0);
        assertEquals(personIdentifier.getIdentifierDomain().getIdentifierDomainId(), MPI_IDENTIFIER_TYPE_ID);
        assertEquals(personIdentifier.getIdentifier(), IDENTIFIER_VALUE);
    }

    private Patient createPatient() {
        Patient patient = new Patient();

        patient.setGender(GENDER_CODE);

        HashSet<PersonName> names = new HashSet<PersonName>();
        PersonName personName = new PersonName();
        personName.setFamilyName(FAMILY_NAME);
        personName.setGivenName(GIVEN_NAME);
        personName.setMiddleName(MIDDLE_NAME);
        names.add(personName);
        patient.setNames(names);

        patient.setBirthdate(BIRTHDATE);

        PatientIdentifier patientIdentifier = new PatientIdentifier(IDENTIFIER_VALUE,
                new PatientIdentifierType(LOCAL_IDENTIFIER_TYPE_ID), null);
        patient.addIdentifier(patientIdentifier);
        return patient;
    }

    private void mockIdentifierMapper() {
        when(identifierMapper.getMappedMpiIdentifierTypeId(LOCAL_IDENTIFIER_TYPE_ID.toString())).thenReturn(MPI_IDENTIFIER_TYPE_ID.toString());
    }
}