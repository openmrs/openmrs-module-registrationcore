package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openmrs.Patient;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;

import javax.jms.Message;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class listen for patient edit event.
 * If MPI is enabled it performs update patient in MPI.
 */
public class PatientEditListener extends PatientActionListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientCreationListener.class);
    
    /**
     * Subscribes for patient edit event.
     */
    public void init() {
        Event.subscribe(RegistrationCoreConstants.PATIENT_EDIT_EVENT_TOPIC_NAME, this);
    }

    /**
     * Update patient at MPI server.
     *
     * @param message message with properties.
     */
    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);
        
        try {
            coreProperties.getMpiProvider().updatePatient(patient);
        } catch (Exception e) {
            ErrorHandlingService errorHandler = coreProperties.getPixErrorHandlingService();
            if (errorHandler == null) {
                throw new MpiException("PIX patient update exception occurred "
                        + "with not configured PIX error handler", e);
            } else {
                LOGGER.error("PIX patient update exception occurred", e);
                errorHandler.handle(e.getMessage(),
                        "org.openmrs.module.registrationcore.api.mpi.pixpdq.PixPatientUpdater",
                        true,
                        ExceptionUtils.getFullStackTrace(e));
            }
    
        }
    }
}