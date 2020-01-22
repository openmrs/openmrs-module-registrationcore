package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PixPatientUpdater implements MpiPatientUpdater {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private RegistrationCoreProperties registrationCoreProperties;

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Override
    public void updatePatient(Patient patient) {
        try {
            Message updateMsg = pixPdqMessageUtil.createUpdate(patient);
            Hl7v2Sender hl7v2Sender = (Hl7v2Sender) registrationCoreProperties
                    .getBeanFromName(RegistrationCoreConstants.GP_MPI_HL7_IMPLEMENTATION);
            Message response = hl7v2Sender.sendPixMessage(updateMsg);

            if (pixPdqMessageUtil.isQueryError(response)) {
                throw new MpiException("Error querying patient data during update");
            }

        } catch (Exception e) {
            log.error(e);
            if (e instanceof MpiException) {
                throw (MpiException) e;
            } else {
                throw new MpiException("Error while updating patient", e);
            }
        }
    }
}
