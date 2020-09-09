package org.openmrs.module.registrationcore.api.mpi.fhir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.openmrs.module.registrationcore.RegistrationCoreConstants.MPI_IDENTIFIER_TYPE_ECID_NAME;

public class FhirPatientExporter implements MpiPatientExporter {

    private final Log log = LogFactory.getLog(this.getClass());


    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcherFhir")
    private FhirPatientFetcher fhirPatientFetcher;

    @Override
    public String exportPatient(Patient patient) {
        try {
//            Util to export patient
//            Message response = hl7SenderHolder.getHl7v2Sender().sendPixMessage(admitMessage);

            Patient mpiPatient = fhirPatientFetcher.fetchMpiPatient(patient.getPatientIdentifier().getIdentifier());
            if (mpiPatient == null) {
                throw new MpiException("Patient has not been created on MPI. "
                        + "Probably patient with the same IDs already exists");
            }
            return getEcidIdentifier(mpiPatient);
        } catch (Exception e) {
            log.error(e);
            if (e instanceof MpiException) {
                throw (MpiException) e;
            } else {
                throw new MpiException("Error while exporting patient", e);
            }
        }
    }

    private String getEcidIdentifier(Patient mpiPatient) {
        return mpiPatient.getPatientIdentifier(MPI_IDENTIFIER_TYPE_ECID_NAME).getIdentifier();
    }

}
