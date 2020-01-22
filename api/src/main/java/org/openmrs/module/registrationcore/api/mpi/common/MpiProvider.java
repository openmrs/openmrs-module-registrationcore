package org.openmrs.module.registrationcore.api.mpi.common;

/**
 * This service exposes MpiProvider service. It is a Spring managed bean which is configured by Global Properties.
 */
public interface MpiProvider<T>
        extends MpiSimilarPatientsSearcher, MpiPatientFetcher, MpiPatientExporter, MpiPatientUpdater {
}
