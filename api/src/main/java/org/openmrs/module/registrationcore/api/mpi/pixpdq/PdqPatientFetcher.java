package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFetcher;
import org.openmrs.module.registrationcore.api.mpi.openempi.PatientIdentifierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import org.springframework.util.CollectionUtils;

// TODO add javadocs
public class PdqPatientFetcher implements MpiPatientFetcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Autowired
    private PatientIdentifierMapper identifierMapper;

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public MpiPatient fetchMpiPatient(String patientIdentifier, String identifierTypeUuid) {
        String mappedMpiIdentifierTypeUuid = identifierMapper.getMappedMpiIdentifierTypeId(identifierTypeUuid);

        List<Map.Entry<String, String>> queryParams = new ArrayList<Map.Entry<String, String>>();
        queryParams.add(new AbstractMap.SimpleEntry("@PID.3.1", patientIdentifier));
        queryParams.add(new AbstractMap.SimpleEntry("@PID.3.4", mappedMpiIdentifierTypeUuid));

        try {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

            List<MpiPatient> mpiPatients = pixPdqMessageUtil.interpretPIDSegments(response);
            mpiPatients = pixPdqMessageUtil.filterByIdentifierAndIdentifierType(mpiPatients, patientIdentifier, identifierTypeUuid);
            if (CollectionUtils.isEmpty(mpiPatients)) {
                return null;
            }
            if (mpiPatients.size() != 1){
            	throw new MpiException(String.format("Created patient not uniquely identified in mpi! " +
                                                        "There are %d patients with identifier %s of identifier type %s",
                                                        mpiPatients.size(), patientIdentifier, identifierTypeUuid));
            }
            return mpiPatients.get(0);
        } catch(Exception e) {
            throw new MpiException("Error in PDQ fetch by identifier", e);
        }
    }

    @Override
    public MpiPatient fetchMpiPatient(PatientIdentifier patientIdentifier) {
    	return fetchMpiPatient(patientIdentifier.getIdentifier(), patientIdentifier.getIdentifierType().getUuid());
    }
}
