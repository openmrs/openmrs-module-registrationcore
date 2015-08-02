package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
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

    private final Log log = LogFactory.getLog(this.getClass());

    private MpiPatientExporter mpiPatientExporter;

    private PatientService patientService;

    public void setMpiPatientExporter(MpiPatientExporter mpiPatientExporter) {
        this.mpiPatientExporter = mpiPatientExporter;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void init() {
        Event.subscribe(RegistrationCoreConstants.TOPIC_NAME, this);
    }

    @Override
    public void onMessage(Message message) {
        log.info("Patient creation event handled, try perform export patient to MPI.");
        validateMessage(message);
        MapMessage mapMessage = (MapMessage) message;
        String patientUuid = getPatientUuid(mapMessage);

        Context.openSession();
        Patient createdPatient = getPatient(patientUuid);
        mpiPatientExporter.export(createdPatient);
        Context.closeSession();
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

    private Patient getPatient(String patientUuid) {
        Patient createdPatient;
        //TODO fix it: Add Privelege role in correct way.
        Context.authenticate("admin", "Admin123");
        createdPatient = patientService.getPatientByUuid(patientUuid);
        return createdPatient;
    }
}
