package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFetcher;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

public class PixPdqProvider implements MpiProvider<Patient> {

    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcherPdq")
    private MpiPatientFetcher patientFetcher;

    @Autowired
    @Qualifier("registrationcore.mpiPatientExporterPix")
    private MpiPatientExporter patientExporter;

    @Autowired
    @Qualifier("registrationcore.mpiPatientUpdaterPix")
    private MpiPatientUpdater patientUpdater;

    @Autowired
    @Qualifier("registrationcore.mpiPatientSearcherPdq")
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
    public Patient fetchMpiPatient(String patientId, String identifierTypeName) {
        return patientFetcher.fetchMpiPatient(patientId, identifierTypeName);
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
