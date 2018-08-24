package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PixPatientExporter implements MpiPatientExporter {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private RegistrationCoreProperties registrationCoreProperties;

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcherPdq")
    private PdqPatientFetcher pdqPatientFetcher;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    @Autowired
    private PatientService patientService;

    @Override
    public String exportPatient(Patient patient) {
        try {
        	// Ensure that patient with that identifier does not already exist on the MPI
	        Patient existingMpiPatient = pdqPatientFetcher.fetchMpiPatient(patient.getPatientIdentifier());
	        if (existingMpiPatient != null) {
		        throw new MpiException("Patient with that identifier is already present on MPI. "
				        + "Unable to create patient");
	        }
	        
            Message admitMessage = pixPdqMessageUtil.createAdmit(patient);
	        Hl7v2Sender hl7v2Sender = (Hl7v2Sender) registrationCoreProperties.getBeanFromName(RegistrationCoreConstants.GP_MPI_HL7_IMPLEMENTATION);
            Message response = hl7v2Sender.sendPixMessage(admitMessage);

            if (pixPdqMessageUtil.isQueryError(response)) {
                throw new MpiException("Error querying patient data during export");
            }

            Patient mpiPatient = pdqPatientFetcher.fetchMpiPatient(patient.getPatientIdentifier());
            if (mpiPatient == null) {
                throw new MpiException("Patient has not been created on MPI. "
                        + "Probably patient with the same IDs already exists");
            }
            return getMpiIdentifier(mpiPatient);
        } catch (Exception e) {
            log.error(e);
            if (e instanceof  MpiException) {
                throw (MpiException) e;
            } else {
                throw new MpiException("Error while exporting patient", e);
            }
        }
    }

    private String getMpiIdentifier(Patient mpiPatient) {
        PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid(
                mpiProperties.getMpiPersonIdentifierTypeUuid());
        return mpiPatient.getPatientIdentifier(patientIdentifierType).getIdentifier();
    }
}
