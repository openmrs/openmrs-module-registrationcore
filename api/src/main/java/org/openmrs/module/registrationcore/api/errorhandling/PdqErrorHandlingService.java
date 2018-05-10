package org.openmrs.module.registrationcore.api.errorhandling;

public interface PdqErrorHandlingService extends ErrorHandlingService {

    String FIND_MPI_PATIENT_DESTINATION = "MpiPatientFetcher-finding";
    String PERSIST_MPI_PATIENT_DESTINATION = "MpiPatientFetcher-persisting";
}
