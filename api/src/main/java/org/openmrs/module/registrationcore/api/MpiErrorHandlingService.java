package org.openmrs.module.registrationcore.api;

public interface MpiErrorHandlingService {

    void handle(String messageBody, String failureReason, String destination, String type, Boolean failure);
}
