package org.openmrs.module.registrationcore.api.errorhandling;

/**
 * Interface for PIX error handling service that is provided by the OutgoingMessageExceptions Module These strings are
 * displayed in the Outgoing Message Exceptions PIX error list
 */
public interface PixErrorHandlingService extends ErrorHandlingService {

    String SENDING_PATIENT_AFTER_PATIENT_CREATION_DESTINATION = "MpiPatientExporter";
    String SENDING_PATIENT_AFTER_PATIENT_UPDATE_DESTINATION = "MpiPatientUpdater";
}
