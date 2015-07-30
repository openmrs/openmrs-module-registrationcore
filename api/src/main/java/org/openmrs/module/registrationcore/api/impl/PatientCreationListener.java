package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.event.EventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.PatientExport;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;


public class PatientCreationListener implements EventListener {

    private PatientExport patientExport;
    private PatientService patientService;

    public void setPatientExport(PatientExport patientExport) {
        this.patientExport = patientExport;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override
    public void onMessage(Message message) {
        validateMessage(message);

        MapMessage mapMessage = (MapMessage) message;
        String patientUuid = getPatientUuid(mapMessage);
        Patient createdPatient = patientService.getPatientByUuid(patientUuid);

        patientExport.export(createdPatient);
    }

    private void validateMessage(Message message) {
        if (!(message instanceof MapMessage))
            throw new APIException("Event message should be MapMessage, but it isn't");
    }

    private String getPatientUuid(MapMessage mapMessage) {
        try {
            return mapMessage.getString(RegistrationCoreConstants.KEY_PATIENT_UUID);
        } catch (JMSException e) {
            throw new APIException("Exception while get uuid of created patient. " + e);
        }
    }
}
