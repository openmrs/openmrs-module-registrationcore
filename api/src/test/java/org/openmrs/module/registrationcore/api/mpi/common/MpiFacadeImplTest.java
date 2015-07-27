package org.openmrs.module.registrationcore.api.mpi.common;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MpiFacadeImplTest {

    private static final String PATIENT_ID = "13";

    @InjectMocks private MpiFacade mpiFacade = new MpiFacadeImpl();
    @Mock private MpiSimilarPatientSearchAlgorithm searchAlgorithm;
    @Mock private MpiPatientImporter patientImporter;
    @Mock private MpiAuthenticator authenticator;

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

        Patient patient = mpiFacade.importMpiPatient(PATIENT_ID);

        verify(authenticator).performAuthentication();
        verify(patientImporter).importMpiPatient(PATIENT_ID);
        assertEquals(patient, mockPatient);
    }

    @Test
    public void testImportDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockAuthentication(true);
        Patient mockPatient = mock(Patient.class);
        when(patientImporter.importMpiPatient(PATIENT_ID)).thenReturn(mockPatient);

        mpiFacade.importMpiPatient(PATIENT_ID);

        verify(authenticator, never()).performAuthentication();
    }

    @Test
    public void testFindSimilarPatients() throws Exception {
        mockAuthentication(false);
        when(searchAlgorithm.findSimilarPatients(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        List<PatientAndMatchQuality> similarPatients = mpiFacade.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
        verify(authenticator).performAuthentication();
        verify(searchAlgorithm).findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
        assertEquals(similarPatients, listPatients);
    }

    @Test
    public void testFindSimilarPatientsDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockAuthentication(true);
        when(searchAlgorithm.findSimilarPatients(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        mpiFacade.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);

        verify(authenticator, never()).performAuthentication();
    }

    @Test
    public void testFindPreciseSimilarPatients() throws Exception {
        mockAuthentication(false);
        when(searchAlgorithm.findPreciseSimilarPatients(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        List<PatientAndMatchQuality> similarPatients = mpiFacade.findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
        verify(authenticator).performAuthentication();
        verify(searchAlgorithm).findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
        assertEquals(similarPatients, listPatients);

    }

    @Test
    public void testFindPreciseSimilarPatientsDoNotPerformAuthenticationInCaseAuthenticated() throws Exception {
        mockAuthentication(true);
        when(searchAlgorithm.findPreciseSimilarPatients(any(Patient.class), any(Map.class), any(Double.class), anyInt()))
                .thenReturn(listPatients);

        mpiFacade.findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);

        verify(authenticator, never()).performAuthentication();
    }

    private void mockAuthentication(boolean authenticated) {
        when(authenticator.isAuthenticated()).thenReturn(authenticated);
    }
}