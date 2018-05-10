package org.openmrs.module.registrationcore.api.impl;

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import org.openmrs.module.registrationcore.api.errorhandling.SendingPatientToMpiParameters;

/**
 * Abstract class for event listening.
 */
public abstract class PatientActionListener implements EventListener {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected RegistrationCoreProperties coreProperties;

    protected PatientService patientService;

    private DaemonToken daemonToken;

    public void setCoreProperties(RegistrationCoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setDaemonToken(DaemonToken daemonToken) {
        this.daemonToken = daemonToken;
    }

    @Override
    public void onMessage(final Message message) {
        Daemon.runInDaemonThread(new Runnable() {
            @Override
            public void run() {
                if (coreProperties.isMpiEnabled()) {
                    performMpiAction(message);
                }
            }
        }, daemonToken);
    }

    /**
     * Perform mpi action.
     *
     * @param message message with properties.
     */
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

    protected String prepareParameters(Patient patient) {
        SendingPatientToMpiParameters parameters = new SendingPatientToMpiParameters(patient.getUuid());
        try {
            return OBJECT_MAPPER.writeValueAsString(parameters);
        } catch (IOException e) {
            throw new RuntimeException("Cannot prepare parameters for OutgoingMessageException", e);
        }
    }

    private void validateMessage(Message message) {
        if (!(message instanceof MapMessage))
            throw new APIException("Event message should be MapMessage, but it isn't");
    }

    private Patient getPatient(String patientUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        return patient;
    }
}
