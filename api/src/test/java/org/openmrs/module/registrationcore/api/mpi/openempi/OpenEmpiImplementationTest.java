package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.*;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OpenEmpiImplementationTest {

    private static final String PATIENT_ID = "13";

    @InjectMocks private MpiProvider mpiProvider = new OpenEmpiImplementation();
    @Mock private MpiSimilarPatientsSearcher searchAlgorithm;
    @Mock private MpiPatientImporter patientImporter;
    @Mock private MpiAuthenticator authenticator;
    @Mock private MpiProperties mpiProperties;

    @Mock private List<PatientAndMatchQuality> listPatients;
    @Mock private Map<String, Object> otherDataPoints;
    @Mock private Patient patient;
    private Double cutoff = 3d;
    private Integer maxResults = 10;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testImportMpiPatient() throws Exception {
        mockAuthentication(false);
        Patient mockPatient = mock(Patient.class);
        when(patientImporter.importMpiPatient(PATIENT_ID)).thenReturn(mockPatient);

        Patient patient = mpiProvider.importMpiPatient(PATIENT_ID);

        verify(authenticator).performAuthentication();
        verify(patientImporter).importMpiPatient(PATIENT_ID);
        assertEquals(patient, mockPatient);
    }

    @Test
    public void testImportDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockAuthentication(true);
        Patient mockPatient = mock(Patient.class);
        when(patientImporter.importMpiPatient(PATIENT_ID)).thenReturn(mockPatient);

        mpiProvider.importMpiPatient(PATIENT_ID);

        verify(authenticator, never()).performAuthentication();
    }

    @Test
    public void testFindSimilarPatients() throws Exception {
        mockProbablyMatchingEnabled(true);
        mockAuthentication(false);
        when(searchAlgorithm.findSimilarMatches(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        List<PatientAndMatchQuality> similarPatients = mpiProvider.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
        verify(authenticator).performAuthentication();
        verify(searchAlgorithm).findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
        assertEquals(similarPatients, listPatients);
    }

    @Test
    public void testFindSimilarPatientsDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockProbablyMatchingEnabled(true);
        mockAuthentication(true);
        when(searchAlgorithm.findSimilarMatches(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        mpiProvider.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);

        verify(authenticator, never()).performAuthentication();
    }

    @Test
    public void testPerformExactlyMatchingSearchIfProbablyMatchingIsDisabled() throws Exception {
        mockProbablyMatchingEnabled(false);
        mockAuthentication(true);

        mpiProvider.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);

        verify(searchAlgorithm).findPreciseSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
    }

    @Test
    public void testFindPreciseSimilarPatients() throws Exception {
        mockProbablyMatchingEnabled(false);
        mockAuthentication(false);
        when(searchAlgorithm.findPreciseSimilarMatches(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        List<PatientAndMatchQuality> similarPatients = mpiProvider.findPreciseSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
        verify(authenticator).performAuthentication();
        verify(searchAlgorithm).findPreciseSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
        assertEquals(similarPatients, listPatients);
    }

    @Test
    public void testFindPreciseSimilarPatientsDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockAuthentication(true);
        when(searchAlgorithm.findPreciseSimilarMatches(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        mpiProvider.findPreciseSimilarMatches(patient, otherDataPoints, cutoff, maxResults);

        verify(authenticator, never()).performAuthentication();
    }

    private void mockAuthentication(boolean authenticated) {
        when(authenticator.isAuthenticated()).thenReturn(authenticated);
    }

    private void mockProbablyMatchingEnabled(boolean b) {
        when(mpiProperties.isProbablyMatchingEnabled()).thenReturn(b);
    }
}