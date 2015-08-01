package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;


public class PatientCreationListener implements EventListener {

    private static final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.mpiPatientExport")
    private MpiPatientExporter mpiPatientExporter;

    @Autowired
    @Qualifier("patientService")
    private PatientService patientService;

    public void init() {
        Event.subscribe(RegistrationCoreConstants.TOPIC_NAME, this);
    }

    @Override
    public void onMessage(Message message) {
        validateMessage(message);

        MapMessage mapMessage = (MapMessage) message;
        String patientUuid = getPatientUuid(mapMessage);
        Patient createdPatient = patientService.getPatientByUuid(patientUuid);

        mpiPatientExporter.export(createdPatient);
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
