package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OpenEmpiPatientSearchAlgorithmTest {

    private static final Integer MAX_RESULTS = 10;
    private static final Double cutoff = 2.0;
    private static final String TOKEN_VALUE = "token_value";
    private static final List<String> MATCHED_FIELDS = Arrays.asList("personName", "gender", "birthdate");

    @InjectMocks private OpenEmpiPatientSearchAlgorithm searchAlgorithm;
    @Mock private RestQueryCreator queryCreator;
    @Mock private PatientQueryMapper queryMapper;
    @Mock private MpiAuthenticator authenticator;

    @Mock private Patient patient;
    @Mock private OpenEmpiPatientQuery patientQuery;
    @Mock private Patient mpiPatient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindSimilarPatientsReturnMoreThanMaxValue() throws Exception {
        mockPatientConversion();
        mockAuthenticationToken();
        mockPatientsResult(createPatients(MAX_RESULTS + 2));
        mockOpenEmpiQueryConversion();

        List<PatientAndMatchQuality> actualPatients = searchAlgorithm.findSimilarPatients(patient, null, cutoff, MAX_RESULTS);

        verify(queryMapper).convert(patient);
        verify(queryCreator).findPatients(TOKEN_VALUE, patientQuery);
        verify(queryMapper, times(MAX_RESULTS)).convert(any(OpenEmpiPatientQuery.class));
        assertTrue(actualPatients.size() == MAX_RESULTS);
        for (PatientAndMatchQuality actualPatient : actualPatients) {
            assertEquals(actualPatient.getPatient(), mpiPatient);
            assertEquals(actualPatient.getMatchedFields(), MATCHED_FIELDS);
            assertEquals(actualPatient.getScore(), cutoff);
        }
    }

    private void mockPatientConversion() {
        when(queryMapper.convert(patient)).thenReturn(patientQuery);
    }

    private void mockAuthenticationToken() {
        when(authenticator.getToken()).thenReturn(TOKEN_VALUE);
    }

    private List<OpenEmpiPatientQuery> createPatients(int count) {
        List<OpenEmpiPatientQuery> patientsResult = new LinkedList<OpenEmpiPatientQuery>();
        for (int i = 0; i < count; i++) {
            OpenEmpiPatientQuery patientQuery = mock(OpenEmpiPatientQuery.class);
            patientsResult.add(patientQuery);
        }
        return patientsResult;
    }

    private void mockPatientsResult(List<OpenEmpiPatientQuery> patientsResult) {
        when(queryCreator.findPatients(TOKEN_VALUE, patientQuery)).thenReturn(patientsResult);
    }

    private void mockOpenEmpiQueryConversion() {
        when(queryMapper.convert(any(OpenEmpiPatientQuery.class))).thenReturn(mpiPatient);
    }
}