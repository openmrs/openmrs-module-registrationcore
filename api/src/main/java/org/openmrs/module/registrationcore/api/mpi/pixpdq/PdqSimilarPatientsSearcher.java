package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;
import java.util.Map;

public class PdqSimilarPatientsSearcher implements MpiSimilarPatientsSearcher {
    // TODO" move search here
    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Patient> searchPatientsByPDQ(String familyName, String givenName) {
        throw new UnsupportedOperationException();
    }
}
