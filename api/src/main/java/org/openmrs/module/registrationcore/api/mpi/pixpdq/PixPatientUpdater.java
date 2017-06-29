package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PixPatientUpdater implements MpiPatientUpdater {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Autowired
    @Qualifier("registrationcore.Hl7SenderHolder")
    private Hl7SenderHolder hl7SenderHolder;

    @Override
    public void updatePatient(Patient patient) {
        try {
            Message updateMsg = pixPdqMessageUtil.createUpdate(patient);

            Message response = hl7SenderHolder.getHl7v2Sender().sendPixMessage(updateMsg);

            if (pixPdqMessageUtil.isQueryError(response)) {
                throw new MpiException("Error querying patient data during update");
            }

        } catch (Exception e) {
            log.error(e);
            if (e instanceof  MpiException) {
                throw (MpiException) e;
            } else {
                throw new MpiException("Error while updating patient", e);
            }
        }
    }
}
