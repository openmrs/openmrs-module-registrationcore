package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PdqSimilarPatientsSearcher implements MpiSimilarPatientsSearcher<Patient> {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.mpiHl7v2Sender")
    private Hl7v2Sender hl7v2Sender;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public List<Patient> findSimilarMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        Map<String, String> queryParams = new HashMap<String, String>();
        if(patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
            queryParams.put("@PID.5.1", patient.getFamilyName());
        if(patient.getGivenName() != null && !patient.getGivenName().isEmpty())
            queryParams.put("@PID.5.2", patient.getGivenName());

        List<Patient> retVal = new LinkedList<Patient>();

        try
        {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message	response = hl7v2Sender.sendPdqMessage(pdqRequest);

            retVal = pixPdqMessageUtil.interpretPIDSegments(response);

            for(Patient result: retVal)
            {
                pixPdqMessageUtil.importPatient(result);
            }

        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        return retVal;
    }

    @Override
    public List<Patient> findExactMatches(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        Map<String, String> queryParams = new HashMap<String, String>();
        if(patient.getFamilyName() != null && !patient.getFamilyName().isEmpty())
            queryParams.put("@PID.5.1", patient.getFamilyName());
        if(patient.getGivenName() != null && !patient.getGivenName().isEmpty())
            queryParams.put("@PID.5.2", patient.getGivenName());

        List<Patient> retVal = new LinkedList<Patient>();

        try
        {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message	response = hl7v2Sender.sendPdqMessage(pdqRequest);

            retVal = pixPdqMessageUtil.interpretPIDSegments(response);

            for(Patient result: retVal)
            {
                pixPdqMessageUtil.importPatient(result);
            }

        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        return retVal;
    }
}
