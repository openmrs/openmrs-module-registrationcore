package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

public abstract class PatientActionListener {

    private PatientService patientService;

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    protected Patient extractPatient(Message message) {
        validateMessage(message);

        String patientUuid = getPatientUuid((MapMessage) message);

        return getPatient(patientUuid);
    }

    private Patient getPatient(String patientUuid) {
        Patient createdPatient;
        //TODO fix it: Add Privelege role in correct way.
        Context.authenticate("admin", "Admin123");
        createdPatient = patientService.getPatientByUuid(patientUuid);
        return createdPatient;
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
