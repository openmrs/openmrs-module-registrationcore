package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.Message;


public class PatientCreationListener extends PatientActionListener implements EventListener {

    private final Log log = LogFactory.getLog(this.getClass());

    private RegistrationCoreProperties coreProperties;

    public void setCoreProperties(RegistrationCoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    public void init() {
        Event.subscribe(RegistrationCoreConstants.REGISTRATION_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void onMessage(Message message) {
        if (coreProperties.isMpiEnabled()) {
            log.info("Patient creation event handled, try perform export patient to MPI.");
            Context.openSession();
            Patient createdPatient = extractPatient(message);
            coreProperties.getMpiFacade().exportPatient(createdPatient);
            Context.closeSession();
        }
    }
}
