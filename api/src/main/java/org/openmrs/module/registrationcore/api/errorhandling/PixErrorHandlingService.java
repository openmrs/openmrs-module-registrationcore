package org.openmrs.module.registrationcore.api.errorhandling;

public interface PixErrorHandlingService extends ErrorHandlingService {

    String SENDING_PATIENT_AFTER_PATIENT_CREATION_DESTINATION = "MpiPatientExporter";
    String SENDING_PATIENT_AFTER_PATIENT_UPDATE_DESTINATION = "MpiPatientUpdater";
}
