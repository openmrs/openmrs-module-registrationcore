package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFetcher;
import org.openmrs.module.registrationcore.api.mpi.openempi.PatientIdentifierMapper;
import org.openmrs.module.santedb.mpiclient.model.MpiPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;
import org.springframework.util.CollectionUtils;

public class PdqPatientFetcher implements MpiPatientFetcher {

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Autowired
    private PatientIdentifierMapper identifierMapper;

    @Autowired
    private PatientService patientService;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public Patient fetchMpiPatient(String patientIdentifier, String identifierTypeUuid) {
        String mpiUuid = identifierMapper.getMappedMpiIdentifierTypeId(identifierTypeUuid);

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("@PID.3.1", patientIdentifier);
        queryParams.put("@PID.3.4", mpiUuid);

        try {
            Message pdqRequest = pixPdqMessageUtil.createPdqMessage(queryParams);
            Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);

            List<Patient> mpiPatients = pixPdqMessageUtil.interpretPIDSegments(response);
            if (CollectionUtils.isEmpty(mpiPatients)) {
                return null;
            }
            return toPatientFromMpiPatient(mpiPatients.get(0));
        } catch(Exception e) {
            throw new MpiException("Error in PDQ fetch by identifier", e);
        }
    }

    @Override
    public MpiPatient fetchMpiPatientWithObservations(String patientId, String identifierTypeUuid) {
        throw new NotImplementedException("Method fetchMpiPatientWithObservations for PdqPatientFetcher is not implemented yet");
    }

    @Override
    public Patient fetchMpiPatient(String patientIdentifier) {
        PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByName(
                RegistrationCoreConstants.MPI_IDENTIFIER_TYPE_ECID_NAME);
        return fetchMpiPatient(patientIdentifier, patientIdentifierType.getUuid());
    }


    private Patient toPatientFromMpiPatient(Patient mpiPatient) {
        // it is a hack in order to save the MpiPatient class to DB (converting to the Patient class)
        Patient patient = new Patient(mpiPatient);
        patient.setIdentifiers(mpiPatient.getIdentifiers());
        for (PatientIdentifier pid : patient.getIdentifiers()) {
            pid.setPatient(patient);
        }
        return patient;
    }
}
