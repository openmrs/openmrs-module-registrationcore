package org.openmrs.module.registrationcore.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import static org.mockito.Mockito.*;

public class PatientCreationListenerTest {

    private static final String PATIENT_UUID_EXAMPLE = "af7c3340-0503-11e3-8ffd-0800200c9a66";

    @InjectMocks private PatientCreationListener patientCreationListener;
    @Mock private PatientService patientService;
    @Mock private RegistrationCoreProperties coreProperties;
    @Mock private MpiProvider mpiProvider;

    @Mock private MapMessage mapMessage;
    @Mock private Message message;
    @Mock private Patient patient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(coreProperties.isMpiEnabled()).thenReturn(true);
        when(coreProperties.getMpiFacade()).thenReturn(mpiProvider);
    }

    @Test(expected = APIException.class)
    public void testThrowApiExceptionOnNotMapMessageInstance() throws Exception {
        patientCreationListener.onMessage(message);
    }

    @Test(expected = APIException.class)
    @SuppressWarnings("unchecked")
    public void testThrowApiExceptionOnJMSException() throws Exception {
        when(mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID)).thenThrow(JMSException.class);
        patientCreationListener.onMessage(message);
    }

    @Test
    public void testPerformExportOfCorrectPatient() throws Exception {
        when(mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID)).thenReturn(PATIENT_UUID_EXAMPLE);
        when(patientService.getPatientByUuid(PATIENT_UUID_EXAMPLE)).thenReturn(patient);

        patientCreationListener.onMessage(mapMessage);

        verify(mpiProvider).exportPatient(patient);
    }

    @Test
    public void testDoNotPerformExportIfMpiIsDisabled() throws Exception {
        when(coreProperties.isMpiEnabled()).thenReturn(false);

        patientCreationListener.onMessage(mapMessage);

        verify(mpiProvider, never());
    }
}