package org.openmrs.module.registrationcore.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;

import javax.jms.MapMessage;
import javax.jms.Message;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientEditListenerTest {

    private static final String PATIENT_UUID_EXAMPLE = "af7c3340-0503-11e3-8ffd-0800200c9a66";

    @InjectMocks private PatientEditListener patientEditListener;
    @Mock private PatientService patientService;
    @Mock private MpiPatientUpdater patientUpdater;

    @Mock private MapMessage mapMessage;
    @Mock private Message message;
    @Mock private Patient patient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPerformUpdate() throws Exception {
        when(mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID)).thenReturn(PATIENT_UUID_EXAMPLE);
        when(patientService.getPatientByUuid(PATIENT_UUID_EXAMPLE)).thenReturn(patient);

        patientEditListener.onMessage(message);

        verify(patientUpdater).updatePatient(patient);
    }
}