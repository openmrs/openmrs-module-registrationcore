package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.openmrs.module.registrationcore.RegistrationCoreConstants.MPI_IDENTIFIER_TYPE_ECID_NAME;

public class PixPatientExporter implements MpiPatientExporter {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Autowired
    @Qualifier("registrationcore.mpiPatientFetcherPdq")
    private PdqPatientFetcher pdqPatientFetcher;

    @Override
    public String exportPatient(Patient patient) {
        try {
            Message admitMessage = pixPdqMessageUtil.createAdmit(patient);
            Message response = hl7SenderHolder.getHl7v2Sender().sendPixMessage(admitMessage);

            if (pixPdqMessageUtil.isQueryError(response)) {
                log.info("Error querying patient data during export");
                throw new MpiException("Error querying patient data during export");
            }

            Patient mpiPatient = pdqPatientFetcher.fetchMpiPatient(patient.getPatientIdentifier().getIdentifier());
            if (mpiPatient == null) {
                throw new MpiException("Patient has not been created on MPI. "
                        + "Probably patient with the same IDs already exists");
            }
            return getEcidIdentifier(mpiPatient);
        } catch (Exception e) {
            log.error(e);
            if (e instanceof  MpiException) {
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
