package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.Event;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import javax.jms.Message;

/**
 * This class listen for patient creation event.
 * If MPI is enabled it perform export patient to MPI,
 * and perform update for local patient with new one patient identifier.
 */
public class PatientCreationListener extends PatientActionListener {

    private MpiProperties mpiProperties;

    private IdentifierBuilder identifierBuilder;

    private UserService userService;

    private DaemonToken daemonToken;

    public void setMpiProperties(MpiProperties mpiProperties) {
        this.mpiProperties = mpiProperties;
    }

    public void setIdentifierBuilder(IdentifierBuilder identifierBuilder) {
        this.identifierBuilder = identifierBuilder;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setDaemonToken(DaemonToken daemonToken) {
        this.daemonToken = daemonToken;
    }

    /**
     * Subscribes for patient creation event.
     */
    public void init() {
        Event.subscribe(RegistrationCoreConstants.PATIENT_REGISTRATION_EVENT_TOPIC_NAME, this);
    }

    /**
     * Export patient to MPI server & update local
     * patient with new one patient identifier.
     *
     * @param message message with properties.
     */
    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);

        String personId = pushPatientToMpiServer(patient);

        updatePatient(patient, message, personId);
    }

    private String pushPatientToMpiServer(Patient patient) {
        return coreProperties.getMpiProvider().exportPatient(patient);
    }

    private void updatePatient(final Patient patient, final Message message, final String personId) {
        Daemon.runInDaemonThread(new Runnable() {
            @Override
            public void run() {
                Context.openSession();
                try {
                    User creator = extractPatientCreator(message);
                    patient.addIdentifier(createPersonIdentifier(creator, personId));
                    patientService.savePatient(patient);
                } finally {
                    Context.closeSession();
                }
            }
        }, daemonToken);
    }

    private User extractPatientCreator(Message message) {
        String creatorId = getMessagePropertyValue(message, RegistrationCoreConstants.KEY_REGISTERER_ID);
        //TODO Darius, check received uuid:
        String creatorUuid = getMessagePropertyValue(message, RegistrationCoreConstants.KEY_REGISTERER_UUID);
        return userService.getUser(Integer.parseInt(creatorId));
    }

    private PatientIdentifier createPersonIdentifier(User creator, String personId) {
        PatientIdentifier personIdentifier = identifierBuilder
                .createIdentifier(mpiProperties.getMpiPersonIdentifierTypeId(), personId, null);
        personIdentifier.setCreator(creator);
        return personIdentifier;
    }
}