package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.Message;


/**
 * This class listen for patient edit event.
 * If MPI is enabled it performs update patient in MPI.
 */
public class PatientEditListener extends PatientActionListener {

    /**
     * Subscribes for patient edit event.
     */
    public void init() {
        Event.subscribe(RegistrationCoreConstants.PATIENT_EDIT_EVENT_TOPIC_NAME, this);
    }

    /**
     * Update patient at MPI server.
     *
     * @param message message with properties.
     */
    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);
        coreProperties.getMpiProvider().updatePatient(patient);
    }
}