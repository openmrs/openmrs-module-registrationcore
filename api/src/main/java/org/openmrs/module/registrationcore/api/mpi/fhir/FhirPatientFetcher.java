package org.openmrs.module.registrationcore.api.mpi.fhir;

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
import org.springframework.beans.factory.annotation.Autowired;

public class FhirPatientFetcher implements MpiPatientFetcher {

    @Autowired
    private PatientIdentifierMapper identifierMapper;

    @Autowired
    private PatientService patientService;

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public Patient fetchMpiPatient(String patientIdentifier, String identifierTypeUuid) {
        String mpiUuid = identifierMapper.getMappedMpiIdentifierTypeId(identifierTypeUuid);



        try {
//            Util to fetch Patient
//            Message response = hl7SenderHolder.getHl7v2Sender().sendPdqMessage(pdqRequest);
//            List<Patient> mpiPatients = pixPdqMessageUtil.interpretPIDSegments(response);
//            if (CollectionUtils.isEmpty(mpiPatients)) {
//                return null;
//            }
//            return toPatientFromMpiPatient(mpiPatients.get(0));
        } catch(Exception e) {
            throw new MpiException("Error in PDQ fetch by identifier", e);
        }
        return null;
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
