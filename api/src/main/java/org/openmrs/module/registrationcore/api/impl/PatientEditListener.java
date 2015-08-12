package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.Message;

public class PatientEditListener extends PatientActionListener {

    private final Log log = LogFactory.getLog(this.getClass());

    public void init() {
        Event.subscribe(RegistrationCoreConstants.EDIT_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void performMpiAction(Message message) {
        log.info("Patient edit event handled, try perform update patient in MPI.");
        Context.openSession();
        Patient createdPatient = extractPatient(message);
        coreProperties.getMpiFacade().updatePatient(createdPatient);
        Context.closeSession();
    }
}
