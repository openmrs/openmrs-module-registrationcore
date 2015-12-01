package org.openmrs.module.registrationcore.api.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;

import javax.jms.MapMessage;
import javax.jms.Message;

import static org.mockito.Mockito.*;

public class PatientEditListenerTest {

    private static final String PATIENT_UUID_EXAMPLE = "af7c3340-0503-11e3-8ffd-0800200c9a66";

    @InjectMocks private PatientEditListener patientEditListener;
    @Mock private PatientService patientService;
    @Mock private RegistrationCoreProperties coreProperties;
    @Mock private MpiProvider mpiProvider;

    @Mock private MapMessage mapMessage;
    @Mock private Message message;
    @Mock private Patient patient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(coreProperties.getMpiProvider()).thenReturn(mpiProvider);
    }

    @Test
    @Ignore //Can't mock static method : "Context.openSession"
    public void testPerformUpdate() throws Exception {
        when(coreProperties.isMpiEnabled()).thenReturn(true);
        when(mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID)).thenReturn(PATIENT_UUID_EXAMPLE);
        when(patientService.getPatientByUuid(PATIENT_UUID_EXAMPLE)).thenReturn(patient);

        patientEditListener.onMessage(message);

        verify(mpiProvider).updatePatient(patient);
    }

    @Test
    @Ignore //Can't be tested since can't mock daemon class.
    public void testDoNotPerformUpdateOnMpiDisabled() throws Exception {
        when(coreProperties.isMpiEnabled()).thenReturn(false);

        patientEditListener.onMessage(message);

        verify(mpiProvider, never()).updatePatient(patient);
    }
}