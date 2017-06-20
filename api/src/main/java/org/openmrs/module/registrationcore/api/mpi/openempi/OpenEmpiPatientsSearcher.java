package org.openmrs.module.registrationcore.api.mpi.openempi;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.mpi.pixpdq.PDQMessageUtil;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpenEmpiPatientsSearcher implements MpiSimilarPatientsSearcher {

    @Autowired
    @Qualifier("registrationcore.restQueryExecutor")
    private RestQueryExecutor queryExecutor;

    @Autowired
    @Qualifier("registrationcore.patientBuilder")
    private PatientBuilder patientBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    private FindPatientQueryBuilder queryMapper = new FindPatientQueryBuilder();

    private PDQMessageUtil pdqMessageUtil = PDQMessageUtil.getInstance();

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient,
                                                           Map<String, Object> otherDataPoints,
                                                           Double cutoff, Integer maxResults) {
        OpenEmpiPatientResult patientQuery = queryMapper.create(patient);

        List<OpenEmpiPatientResult> mpiPatients = queryExecutor
                .findProbablySimilarPatients(authenticator.getToken(), patientQuery);

        mpiPatients = limitResults(maxResults, mpiPatients);

        return convertMpiPatients(mpiPatients, cutoff);
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient,
                                                         Map<String, Object> otherDataPoints,
                                                         Double cutoff, Integer maxResults) {
        OpenEmpiPatientResult patientQuery = queryMapper.create(patient);

        List<OpenEmpiPatientResult> mpiPatients = queryExecutor
                .findPreciseSimilarPatients(authenticator.getToken(), patientQuery);

        mpiPatients = limitResults(maxResults, mpiPatients);

        return convertMpiPatients(mpiPatients, cutoff);
    }


    @Override
    public List<Patient> searchPatientsByPDQ(String familyName, String givenName) {
        Map<String, String> queryParams = new HashMap<String, String>();
        if(familyName != null && !familyName.isEmpty())
            queryParams.put("@PID.5.1", familyName);
        if(givenName != null && !givenName.isEmpty())
            queryParams.put("@PID.5.2", givenName);

        try
        {
            Message pdqRequest = pdqMessageUtil.createPdqMessage(queryParams);
            Message	response = pdqMessageUtil.sendMessage(pdqRequest, pdqMessageUtil.getPdqEndpoint(), pdqMessageUtil.getPdqPort());

            List<Patient> retVal = pdqMessageUtil.interpretPIDSegments(response);

            return retVal;
        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }
        return new LinkedList<Patient>();
    }

    private List<PatientAndMatchQuality> convertMpiPatients(List<OpenEmpiPatientResult> mpiPatients,
                                                            Double cutoff) {
        List<String> matchedFields = Arrays.asList("personName", "gender", "birthdate");
        List<PatientAndMatchQuality> result = new LinkedList<PatientAndMatchQuality>();
        for (OpenEmpiPatientResult mpiPatient : mpiPatients) {
            Patient convertedPatient = convertMpiPatient(mpiPatient);
            PatientAndMatchQuality resultPatient =
                    new PatientAndMatchQuality(convertedPatient, cutoff, matchedFields);
            result.add(resultPatient);
        }
        return result;
    }

    private List<OpenEmpiPatientResult> limitResults(Integer maxResults, List<OpenEmpiPatientResult> mpiPatients) {
        return mpiPatients.size() > maxResults ? mpiPatients.subList(0, maxResults) : mpiPatients;
    }

    private Patient convertMpiPatient(OpenEmpiPatientResult mpiPatient) {
        patientBuilder.setPatient(new MpiPatient());
        Patient patient = patientBuilder.buildPatient(mpiPatient);
        patient.setUuid(String.valueOf(mpiPatient.getPersonId()));
        return patient;
    }
}