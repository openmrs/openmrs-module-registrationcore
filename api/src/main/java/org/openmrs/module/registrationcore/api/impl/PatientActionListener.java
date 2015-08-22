package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.event.EventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

public abstract class PatientActionListener implements EventListener {

    protected RegistrationCoreProperties coreProperties;

    protected PatientService patientService;

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setCoreProperties(RegistrationCoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Override
    public void onMessage(Message message) {
        Context.openSession();
        try {
            if (coreProperties.isMpiEnabled()) {
                performMpiAction(message);
            }
        } finally {
            Context.closeSession();
        }
    }

    public abstract void performMpiAction(Message message);

    protected Patient extractPatient(Message message) {
        validateMessage(message);

        String patientUuid = getMessagePropertyValue(message, RegistrationCoreConstants.KEY_PATIENT_UUID);

        return getPatient(patientUuid);
    }

    protected String getMessagePropertyValue(Message message, String propertyName) {
        validateMessage(message);
        try {
            return ((MapMessage) message).getString(propertyName);
        } catch (JMSException e) {
            throw new APIException("Exception while get uuid of created patient from JMS message. " + e);
        }
    }

    private void validateMessage(Message message) {
        if (!(message instanceof MapMessage))
            throw new APIException("Event message should be MapMessage, but it isn't");
    }

    private Patient getPatient(String patientUuid) {
        try {
            Context.addProxyPrivilege("Get Patients");
            return patientService.getPatientByUuid(patientUuid);
        } finally {
            Context.removeProxyPrivilege("Get Patients");
        }
    }
}
