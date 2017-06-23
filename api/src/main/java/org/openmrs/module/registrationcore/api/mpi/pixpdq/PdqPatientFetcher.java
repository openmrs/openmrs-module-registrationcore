package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFetcher;

import java.util.HashMap;
import java.util.Map;

public class PdqPatientFetcher implements MpiPatientFetcher {

    private PDQMessageUtil pdqMessageUtil = PDQMessageUtil.getInstance();

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public Patient fetchMpiPatient(String patientId) {
        PatientIdentifier identifier = new PatientIdentifier(patientId, null, null);
        Map<String, String> queryParams = new HashMap<String, String>();

        queryParams.put("@PID.3.1", identifier.getIdentifier());

        if(identifier.getIdentifierType() != null)
            queryParams.put("@PID.3.4", identifier.getIdentifierType().getName());

        Patient retVal = new Patient();

        try
        {
            Message pdqRequest = pdqMessageUtil.createPdqMessage(queryParams);
            Message	response = pdqMessageUtil.sendMessage(pdqRequest, pdqMessageUtil.getPdqEndpoint(), pdqMessageUtil.getPdqPort());

            retVal = pdqMessageUtil.interpretPIDSegments(response).get(0);

            pdqMessageUtil.importPatient(retVal);

            return retVal;
        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        return retVal;

    }
}
