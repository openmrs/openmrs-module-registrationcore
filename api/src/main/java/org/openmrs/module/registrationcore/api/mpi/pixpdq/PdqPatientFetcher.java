package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFetcher;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdqPatientFetcher implements MpiPatientFetcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Autowired
    private MpiProperties mpiProperties;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public Patient fetchMpiPatient(String patientId) {
        Patient patient = Context.getPatientService().getPatient(Integer.valueOf(patientId));
        if (patient == null) {
            patient = Context.getPatientService().getPatientByUuid(patientId);
        }

        Map<String, String> queryParams = new HashMap<String, String>();
        List<String> uuidList = mpiProperties.getPdqIdentifierTypeUuidList();
        for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
            if (uuidList.contains(patientIdentifier.getIdentifierType().getUuid())) {
                queryParams.put("@PID.3.1", patientIdentifier.getIdentifier());
                queryParams.put("@PID.3.4", patientIdentifier.getIdentifierType().getName());
            }
        }

        Patient retVal = new Patient();

        try
        {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

            retVal = pixPdqMessageUtil.interpretPIDSegments(response).get(0);

            return retVal;
        }
        catch(Exception e)
        {
            log.error("Error in PDQ Search", e);
        }

        return retVal;

    }
}
