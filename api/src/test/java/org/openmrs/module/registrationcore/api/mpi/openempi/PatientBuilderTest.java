package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientBuilderTest {

    private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";

    private static final Integer LOCAL_OPENMRS_IDENTIFIER_TYPE_ID = 3;
    private static final Integer MPI_OPENMRS_IDENTIFIER_TYPE_ID = 13;
    private static final Integer LOCAL_ECID_IDENTIFIER_TYPE_ID = 5;
    private static final Integer MPI_ECID_IDENTIFIER_TYPE_ID = 60;
    private static final String PATIENT_OPENMRS_IDENTIFIER_VALUE = "1001NF";
    private static final String PATIENT_ECID_IDENTIFIER_VALUE = "019bb820-1bd3-11e5-8a7f-040158db6201";
    private static final int PERSON_IDENTIFIER_TYPE_ID = 6;

    @InjectMocks private PatientBuilder patientBuilder;
    @Mock private PatientIdentifierMapper identifierMapper;
    @Mock private IdentifierBuilder identifierBuilder;
    @Mock private MpiProperties mpiProperties;
    private XmlMarshaller xmlMarshaller = new XmlMarshaller();


    @Mock private PatientIdentifier openmrsidentifier;
    @Mock private PatientIdentifier ecidIdentifier;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockIdentifierMapper();
        mockIdentifierBuilder();
        when(mpiProperties.getMpiPersonIdentifierTypeId()).thenReturn(PERSON_IDENTIFIER_TYPE_ID);
    }

    private void mockIdentifierMapper() {
        when(identifierMapper.getMappedLocalIdentifierTypeId(MPI_OPENMRS_IDENTIFIER_TYPE_ID.toString())).thenReturn(LOCAL_OPENMRS_IDENTIFIER_TYPE_ID.toString());
        when(identifierMapper.getMappedLocalIdentifierTypeId(MPI_ECID_IDENTIFIER_TYPE_ID.toString())).thenReturn(LOCAL_ECID_IDENTIFIER_TYPE_ID.toString());
    }

    private void mockIdentifierBuilder() {
        when(identifierBuilder.createIdentifier(LOCAL_OPENMRS_IDENTIFIER_TYPE_ID, PATIENT_OPENMRS_IDENTIFIER_VALUE, null))
                .thenReturn(openmrsidentifier);
        when(identifierBuilder.createIdentifier(LOCAL_ECID_IDENTIFIER_TYPE_ID, PATIENT_ECID_IDENTIFIER_VALUE, null))
                .thenReturn(ecidIdentifier);
    }

    @Test
    public void testBuildPatient() throws Exception {
        OpenEmpiPatientResult query = readPatient(PATIENT_WITH_OPENMRS_ID);

        patientBuilder.setPatient(new Patient());
        Patient patient = patientBuilder.buildPatient(query);

        assertEquals(patient.getGender(), query.getGender().getGenderCode());

        assertEquals(patient.getFamilyName(), query.getFamilyName());
        assertEquals(patient.getGivenName(), query.getGivenName());
        assertEquals(patient.getMiddleName(), query.getMiddleName());

        verify(openmrsidentifier).setPreferred(true);
        assertTrue(patient.getIdentifiers().contains(openmrsidentifier));
        assertTrue(patient.getIdentifiers().contains(ecidIdentifier));
    }

    private OpenEmpiPatientResult readPatient(String path) throws Exception {
        return xmlMarshaller.getQuery(path);
    }
}