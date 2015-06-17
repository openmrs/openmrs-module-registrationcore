package org.openmrs.module.registrationcore.api.mpi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("registrationcore.OpenEmpiPatientSearchAlgorithm")
public class OpenEmpiPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    private MpiCredentials credentials;

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return null;
    }
}
