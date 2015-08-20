package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;


public class PatientCreationListener extends PatientActionListener {

    private final Log log = LogFactory.getLog(this.getClass());

    public void init() {
        Event.subscribe(RegistrationCoreConstants.REGISTRATION_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void performMpiAction(Patient createdPatient) {
        coreProperties.getMpiProvider().exportPatient(createdPatient);
    }
}
