package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("registrationcore.mpiFacade")
public class MpiFacadeImpl implements MpiFacade {

    @Autowired
    private MpiSimilarPatientSearchAlgorithm searchAlgorithm;
    @Autowired
    private MpiPatientImporter patientImporter;
    @Autowired
    private MpiAuthenticator authenticator;

    @Override
    public void performAuthentication() {
        authenticator.performAuthentication();
    }

    @Override
    public MpiPatient importMpiPatient(String patientId) {
        return null;
    }

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return null;
    }

    @Override
    public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return null;
    }
}
