package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

public class PatientEditListener extends PatientActionListener {

    public void init() {
        Event.subscribe(RegistrationCoreConstants.EDIT_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void performMpiAction(Patient editedPatient) {
        coreProperties.getMpiProvider().updatePatient(editedPatient);
    }
}
