package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiSimilarPatientsSearcher;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PdqSimilarPatientsSearcher implements MpiSimilarPatientsSearcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.mpiHl7v2Sender")
    private Hl7v2Sender hl7v2Sender;

    protected final Log log = LogFactory.getLog(this.getClass());

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
        Map<String, String> queryParams = new HashMap<String, String>();
        if(familyName != null && !familyName.isEmpty())
            queryParams.put("@PID.5.1", familyName);
        if(givenName != null && !givenName.isEmpty())
            queryParams.put("@PID.5.2", givenName);

        List<Patient> retVal = new LinkedList<Patient>();

        try
        {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message	response = hl7v2Sender.sendPdqMessage(pdqRequest);

           retVal = pixPdqMessageUtil.interpretPIDSegments(response);

            for(Patient patient: retVal)
            {
                pixPdqMessageUtil.importPatient(patient);
            }

        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        return retVal;
    }
}