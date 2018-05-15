package org.openmrs.module.registrationcore.api.errorhandling;

public interface ErrorHandlingService {
    
    void handle(String messageBody, String destination, boolean failure, String failureReason);
}
