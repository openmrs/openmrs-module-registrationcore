package org.openmrs.module.registrationcore.api.mpi.fhir;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.*;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.santedb.mpiclient.model.MpiPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

public class FhirProvider implements MpiProvider<Patient> {

    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcherFhir")
    private MpiPatientFetcher patientFetcher;

    @Autowired
    @Qualifier("registrationcore.mpiPatientExporterPix")
    private MpiPatientExporter patientExporter;

    @Autowired
    @Qualifier("registrationcore.mpiPatientUpdaterPix")
    private MpiPatientUpdater patientUpdater;

    @Autowired
    @Qualifier("registrationcore.mpiPatientSearcherFhir")
    private MpiSimilarPatientsSearcher searchAlgorithm;

    @Override
    public void updatePatient(Patient patient) {
        patientUpdater.updatePatient(patient);
    }

    @Override
    public String exportPatient(Patient patient) {
        return patientExporter.exportPatient(patient);
    }

    @Override
    public Patient fetchMpiPatient(String patientId) {
        return patientFetcher.fetchMpiPatient(patientId);
    }

    @Override
    public Patient fetchMpiPatient(String patientId, String identifierTypeUuid) {
        return patientFetcher.fetchMpiPatient(patientId, identifierTypeUuid);
    }

    @Override
    public MpiPatient fetchMpiPatientWithObservations(String patientId, String identifierTypeUuid) {
        return patientFetcher.fetchMpiPatientWithObservations(patientId, identifierTypeUuid);
    }

    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return searchAlgorithm.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return searchAlgorithm.findExactMatches(patient, otherDataPoints, cutoff, maxResults);
    }
}