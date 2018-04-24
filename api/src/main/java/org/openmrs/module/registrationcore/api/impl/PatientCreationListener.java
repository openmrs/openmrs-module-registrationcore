package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import javax.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listen for patient creation event.
 * If MPI is enabled it perform export patient to MPI,
 * and perform update for local patient with new one patient identifier.
 */
public class PatientCreationListener extends PatientActionListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientCreationListener.class);
    
    private MpiProperties mpiProperties;

    private IdentifierBuilder identifierBuilder;

    private UserService userService;

    public void setMpiProperties(MpiProperties mpiProperties) {
        this.mpiProperties = mpiProperties;
    }

    public void setIdentifierBuilder(IdentifierBuilder identifierBuilder) {
        this.identifierBuilder = identifierBuilder;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
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
    
        String personId = null;
        try {
            personId = pushPatientToMpiServer(patient);
        } catch (Exception e) {
            ErrorHandlingService errorHandler = coreProperties.getPixErrorHandlingService();
            if (errorHandler == null) {
                throw new MpiException("PIX patient push exception occurred "
                        + "with not configured PIX error handler", e);
            } else {
                LOGGER.error("PIX patient push exception occurred", e);
                errorHandler.handle(e.getMessage(),
                        "org.openmrs.module.registrationcore.api.mpi.pixpdq.PixPatientExporter",
                        true,
                        ExceptionUtils.getFullStackTrace(e));
            }
        }
        
        if (personId != null) {
            updatePatient(patient, message, personId);
        }
    }

    private String pushPatientToMpiServer(Patient patient) {
        return coreProperties.getMpiProvider().exportPatient(patient);
    }

    private void updatePatient(final Patient patient, final Message message, final String personId) {
        User creator = extractPatientCreator(message);
        patient.addIdentifier(createPersonIdentifier(creator, personId));
        patientService.savePatient(patient);
    }

    private User extractPatientCreator(Message message) {
        String creatorUuid = getMessagePropertyValue(message, RegistrationCoreConstants.KEY_REGISTERER_UUID);
        return userService.getUserByUuid(creatorUuid);
    }

    private PatientIdentifier createPersonIdentifier(User creator, String personId) {
        PatientIdentifier personIdentifier = identifierBuilder
                .createIdentifier(mpiProperties.getMpiPersonIdentifierTypeUuid(), personId, null);
        personIdentifier.setCreator(creator);
        return personIdentifier;
    }
}