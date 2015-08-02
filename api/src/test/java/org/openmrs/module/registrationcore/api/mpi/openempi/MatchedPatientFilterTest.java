package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MatchedPatientFilterTest {

    public static final Integer MPI_GLOBAL_IDENTIFIER_TYPE_ID = 5;
    private static final String PATIENT_IDENTIFIER_VALUE = "647f2ca0-3240-11e5-8a7f-040158db6201";
    List<PatientAndMatchQuality> patients;
    @Mock private MpiProperties mpiProperties;
    @InjectMocks private MatchedPatientFilter filter;

    @Mock private PatientAndMatchQuality patientWrapper;
    @Mock private PatientAndMatchQuality mpiPatientWrapper;

    @Before
    public void setUp() throws Exception {
        filter = new MatchedPatientFilter();
        MockitoAnnotations.initMocks(this);
        mockProperty();
    }

    private void mockProperty() {
        when(mpiProperties.getGlobalIdentifierDomainId()).thenReturn(MPI_GLOBAL_IDENTIFIER_TYPE_ID);
    }

    @Test
    public void testFilterPatientsWithSimilarIdentifiers() throws Exception {
        generatePatients(Patient.class, MpiPatient.class);
        filter.filter(patients);

        verify(mpiProperties).getGlobalIdentifierDomainId();
        assertTrue(patients.contains(patientWrapper));
        assertFalse(patients.contains(mpiPatientWrapper));
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
        when(patient.getPatientIdentifier(MPI_GLOBAL_IDENTIFIER_TYPE_ID)).thenReturn(identifier);
        when(identifier.getIdentifier()).thenReturn(PATIENT_IDENTIFIER_VALUE);
        when(localPatientWrapper.getPatient()).thenReturn(patient);
        return localPatientWrapper;
    }

    @Test(expected = APIException.class)
    public void testFilterPatientsThrowExceptionOnLocalPatientsHaveSameIdentifier() throws Exception {
        generatePatients(Patient.class, Patient.class);

        filter.filter(patients);
    }
}