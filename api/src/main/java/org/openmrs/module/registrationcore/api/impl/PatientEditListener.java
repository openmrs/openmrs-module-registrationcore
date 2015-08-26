package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.Message;

public class PatientEditListener extends PatientActionListener {

    public void init() {
        Event.subscribe(RegistrationCoreConstants.PATIENT_EDIT_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);
        coreProperties.getMpiProvider().updatePatient(patient);
    }
}