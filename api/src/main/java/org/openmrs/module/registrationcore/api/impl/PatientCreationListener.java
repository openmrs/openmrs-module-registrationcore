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
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;


public class PatientCreationListener implements EventListener {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.mpiPatientExport")
    private MpiPatientExporter mpiPatientExporter;

    @Autowired
    @Qualifier("patientService")
    private PatientService patientService;

    public void init() {
        log.error("Subscribing PatientCreationListener");
        Event.subscribe(RegistrationCoreConstants.TOPIC_NAME, this);
    }

    @Override
    public void onMessage(Message message) {
        log.error("Handled creation patient message.");

        validateMessage(message);

        MapMessage mapMessage = (MapMessage) message;
        String patientUuid = getPatientUuid(mapMessage);
        Patient createdPatient = getPatient(patientUuid);

        mpiPatientExporter.export(createdPatient);
    }

    private Patient getPatient(String patientUuid) {
        Patient createdPatient;
        try {
            Context.addProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
            Context.openSession();
            createdPatient = patientService.getPatientByUuid(patientUuid);
        } finally {
            Context.closeSession();
            Context.removeProxyPrivilege(PrivilegeConstants.VIEW_PATIENTS);
        }
        return createdPatient;
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
