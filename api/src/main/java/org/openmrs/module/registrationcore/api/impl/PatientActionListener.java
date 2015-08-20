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

    private PatientService patientService;

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setCoreProperties(RegistrationCoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    public abstract void performMpiAction(Patient patient);

    @Override
    public void onMessage(Message message) {
        Context.openSession();
        if (coreProperties.isMpiEnabled()) {
            Patient patient = extractPatient(message);
            performMpiAction(patient);
        }
        Context.closeSession();
    }

    protected Patient extractPatient(Message message) {
        validateMessage(message);

        String patientUuid = getPatientUuid((MapMessage) message);

        return getPatient(patientUuid);
    }

    private Patient getPatient(String patientUuid) {
        try {
            Context.addProxyPrivilege("Get Patients");
            return patientService.getPatientByUuid(patientUuid);
        } finally {
            Context.removeProxyPrivilege("Get Patients");
        }
    }

    private String getPatientUuid(MapMessage mapMessage) {
        try {
            return mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID);
        } catch (JMSException e) {
            throw new APIException("Exception while get uuid of created patient. " + e);
        }
    }

    private void validateMessage(Message message) {
        if (!(message instanceof MapMessage))
            throw new APIException("Event message should be MapMessage, but it isn't");
    }
}
