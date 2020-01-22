package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenEmpiPatientUpdaterTest {

    private static final String TOKEN = "TOKEN_VALUE";

    @Mock
    private OpenEmpiPatientQueryBuilder queryBuilder;
    @Mock
    private MpiAuthenticator authenticator;
    @Mock
    private RestQueryExecutor queryCreator;
    @InjectMocks
    private OpenEmpiPatientUpdater patientUpdater;

    @Mock
    private Patient patient;
    @Mock
    private OpenEmpiPatientResult query;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdatePatient() throws Exception {
        when(authenticator.getToken()).thenReturn(TOKEN);
        when(queryBuilder.build(patient)).thenReturn(query);

        patientUpdater.updatePatient(patient);

        verify(queryCreator).updatePatient(TOKEN, query);
    }
}