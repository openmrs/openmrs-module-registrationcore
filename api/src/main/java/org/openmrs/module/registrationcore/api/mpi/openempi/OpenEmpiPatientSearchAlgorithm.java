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
    @Qualifier("registrationcore.patientBuilder")
    private PatientBuilder patientBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    private FindPatientQueryBuilder queryMapper = new FindPatientQueryBuilder();

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        OpenEmpiPatientQuery patientQuery = queryMapper.create(patient);

        List<OpenEmpiPatientQuery> mpiPatients = restQueryCreator
                .findProbablySimilarPatients(authenticator.getToken(), patientQuery);

        mpiPatients = limitResults(maxResults, mpiPatients);

        return convertMpiPatients(mpiPatients, cutoff);
    }

    @Override
    public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient,
                                                                   Map<String, Object> otherDataPoints,
                                                                   Double cutoff, Integer maxResults) {
        OpenEmpiPatientQuery patientQuery = queryMapper.create(patient);

        List<OpenEmpiPatientQuery> mpiPatients = restQueryCreator
                .findPreciseSimilarPatients(authenticator.getToken(), patientQuery);

        mpiPatients = limitResults(maxResults, mpiPatients);

        return convertMpiPatients(mpiPatients, cutoff);
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

    private List<OpenEmpiPatientQuery> limitResults(Integer maxResults, List<OpenEmpiPatientQuery> mpiPatients) {
        return mpiPatients.size() > maxResults ? mpiPatients.subList(0, maxResults) : mpiPatients;
    }

    private Patient convertMpiPatient(OpenEmpiPatientQuery mpiPatient) {
        patientBuilder.setPatient(new MpiPatient());
        Patient patient = patientBuilder.buildPatient(mpiPatient);
        patient.setUuid(String.valueOf(mpiPatient.getPersonId()));
        return patient;
    }
}