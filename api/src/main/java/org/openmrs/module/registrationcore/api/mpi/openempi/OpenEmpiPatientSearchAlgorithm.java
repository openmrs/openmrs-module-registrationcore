package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpenEmpiPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator restQueryCreator;

    @Autowired
    @Qualifier("registrationcore.queryMapper")
    private PatientQueryMapper queryMapper;

    @Autowired
    @Qualifier("registrationcore.patientBuilder")
    private PatientBuilder patientBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        OpenEmpiPatientQuery patientQuery = queryMapper.convert(patient);

        List<OpenEmpiPatientQuery> mpiPatients = restQueryCreator.findPatients(authenticator.getToken(), patientQuery);

        if (mpiPatients.size() > maxResults) {
            mpiPatients = mpiPatients.subList(0, maxResults);
        }

        return convertMpiPatients(mpiPatients, cutoff);
    }

    @Override
    public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        //TODO should be changed to implementation which query to SimilarPatients, not exact.
        return findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
    }

    private List<PatientAndMatchQuality> convertMpiPatients(List<OpenEmpiPatientQuery> mpiPatients,
                                                            Double cutoff) {
        List<String> matchedFields = Arrays.asList("personName", "gender", "birthdate");
        List<PatientAndMatchQuality> result = new LinkedList<PatientAndMatchQuality>();
        for (OpenEmpiPatientQuery mpiPatient : mpiPatients) {
            Patient convertedPatient = convertMpiPatient(mpiPatient);
            PatientAndMatchQuality resultPatient =
                    new PatientAndMatchQuality(convertedPatient, cutoff, matchedFields);
            result.add(resultPatient);
        }
        return result;
    }

    private Patient convertMpiPatient(OpenEmpiPatientQuery mpiPatient) {
        patientBuilder.setPatient(new MpiPatient());
        Patient patient = patientBuilder.buildPatient(mpiPatient);
        patient.setUuid(String.valueOf(mpiPatient.getPersonId()));
        return patient;
    }
}
