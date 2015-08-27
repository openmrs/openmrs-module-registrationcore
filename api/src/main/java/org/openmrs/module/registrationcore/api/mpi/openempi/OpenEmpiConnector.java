package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.*;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

public class OpenEmpiConnector implements MpiProvider {

    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcher")
    private MpiPatientFetcher patientImporter;

    @Autowired
    @Qualifier("registrationcore.mpiPatientExporter")
    private MpiPatientExporter patientExporter;

    @Autowired
    @Qualifier("registrationcore.mpiPatientUpdater")
    private MpiPatientUpdater patientUpdater;

    @Autowired
    @Qualifier("registrationcore.mpiPatientSearcher")
    private MpiSimilarPatientsSearcher searchAlgorithm;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    @Override
    public Patient fetchMpiPatient(String patientId) {
        authenticateIfNeeded();
        return patientImporter.fetchMpiPatient(patientId);
    }

    @Override
    public String exportPatient(Patient patient) {
        authenticateIfNeeded();
        return patientExporter.exportPatient(patient);
    }

    @Override
    public void updatePatient(Patient patient) {
        authenticateIfNeeded();
        patientUpdater.updatePatient(patient);
    }

    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints,
                                                           Double cutoff, Integer maxResults) {
        authenticateIfNeeded();
        if (mpiProperties.isProbabilisticMatchingEnabled()) {
            return searchAlgorithm.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
        } else {
            return searchAlgorithm.findExactMatches(patient, otherDataPoints, cutoff, maxResults);
        }
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints,
                                                         Double cutoff, Integer maxResults) {
        authenticateIfNeeded();
        return searchAlgorithm.findExactMatches(patient, otherDataPoints, cutoff, maxResults);
    }

    private void authenticateIfNeeded() {
        if (!authenticator.isAuthenticated())
            authenticator.performAuthentication();
    }
}
