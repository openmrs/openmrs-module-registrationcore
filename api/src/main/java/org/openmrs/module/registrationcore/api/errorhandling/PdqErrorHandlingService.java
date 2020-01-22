package org.openmrs.module.registrationcore.api.errorhandling;

/**
 * Interface for Pdq error handling service that is provided by the OutgoingMessageExceptions Module
 * These strings are displayed in the Outgoing Message Exceptions PDQ error list
 */
public interface PdqErrorHandlingService extends ErrorHandlingService {
	
	String FIND_MPI_PATIENT_DESTINATION = "MpiPatientFetcher-finding";
	
	String PERSIST_MPI_PATIENT_DESTINATION = "MpiPatientFetcher-persisting";
}
