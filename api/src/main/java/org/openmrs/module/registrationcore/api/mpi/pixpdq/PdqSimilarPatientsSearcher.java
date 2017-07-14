package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PdqSimilarPatientsSearcher implements MpiSimilarPatientsSearcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public List<PatientAndMatchQuality> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return find(patient, maxResults);
    }

    @Override
    public List<PatientAndMatchQuality> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        return find(patient, maxResults);
    }

    private List<PatientAndMatchQuality> find(Patient patient, Integer maxResults) {
        Map<String, String> queryParams = new HashMap<String, String>();
        if(patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
            queryParams.put("@PID.5.1", patient.getFamilyName());
        if(patient.getGivenName() != null && !patient.getGivenName().isEmpty())
            queryParams.put("@PID.5.2", patient.getGivenName());

        List<Patient> retVal = new LinkedList<Patient>();

        try
        {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

            retVal = pixPdqMessageUtil.interpretPIDSegments(response);

        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        if (maxResults != null && retVal.size() > maxResults) {
            retVal = retVal.subList(0, maxResults);
        }

        return toMatchList(retVal);
    }

    private List<PatientAndMatchQuality> toMatchList(List<Patient> patients) {
        List<PatientAndMatchQuality> matchList = new ArrayList<PatientAndMatchQuality>();

        for (Patient match : patients) {
            List<String> matchedFields = new ArrayList<String>();
            Double score = 0.0;

            if(patient.getBirthdate() != null && match.getBirthdate() != null) {
                long birthdateDistance = Math.abs(patient.getBirthdate().getTime() - match.getBirthdate().getTime());
                if(birthdateDistance == 0){
                    matchedFields.add("bithdate");
                    score += 0.2;
                }
            }

            for (PersonName patientName : patient.getNames()) {
                for (PersonName matchName : match.getNames()) {
                    if(patientName.getFamilyName().equals(matchName.getFamilyName())) {
                        matchedFields.add("names.familyName");
                        score += 0.2;
                    }
                    if(patientName.getMiddleName().equals(matchName.getMiddleName())) {
                        matchedFields.add("names.middleName");
                        score += 0.2;
                    }
                    if(patientName.getGivenName().equals(matchName.getGivenName())) {
                        matchedFields.add("names.givenName");
                        score += 0.2;
                    }
                }
            }

            for(PersonAddress personAddress : patient.getAddresses()) {
                for(PersonAddress matchAddress : match.getAddresses()) {
                    if(personAddress.getCountry().equals(matchAddress.getCountry())) {
                        matchedFields.add("addresses.country");
                        score += 0.2;
                    }
                    if(personAddress.getCityVillage().equals(matchAddress.getCityVillage())) {
                        matchedFields.add("addresses.cityVillage");
                        score += 0.2;
                    }
                }
            }

            matchList.add(new PatientAndMatchQuality(match, score, matchedFields));
        }

        return matchList;
    }
}
