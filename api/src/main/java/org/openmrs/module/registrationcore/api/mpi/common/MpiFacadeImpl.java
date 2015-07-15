package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

public class MpiFacadeImpl implements MpiFacade {

    @Autowired
    @Qualifier("registrationcore.mpiPatientSearcher")
    private MpiSimilarPatientSearchAlgorithm searchAlgorithm;
    @Autowired
    @Qualifier("registrationcore.mpiPatientImporter")
    private MpiPatientImporter patientImporter;
    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        validateAuthentication();
        return patientImporter.importMpiPatient(patientId);
    }

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        validateAuthentication();
        return searchAlgorithm.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
    }

    @Override
    public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
                                                                   Double cutoff, Integer maxResults) {
        validateAuthentication();
        return searchAlgorithm.findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
    }

    private void validateAuthentication() {
        if (!authenticator.isAuthenticated())
            authenticator.performAuthentication();
    }
}
