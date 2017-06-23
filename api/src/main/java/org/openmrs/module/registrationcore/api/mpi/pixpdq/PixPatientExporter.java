package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PixPatientExporter implements MpiPatientExporter {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.mpiHl7v2Sender")
    private Hl7v2Sender hl7v2Sender;

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Override
    public String exportPatient(Patient patient) {
        try {
            Message admitMessage = pixPdqMessageUtil.createAdmit(patient);
            Message response = hl7v2Sender.sendPdqMessage(admitMessage);

            if (pixPdqMessageUtil.isQueryError(response)) {
                throw new MpiException("Error querying patient data during export");
            }

            // TODO: return ID
            return null;
        } catch (Exception e) {
            log.error(e);
            if (e instanceof  MpiException) {
                throw (MpiException) e;
            } else {
                throw new MpiException("Error while exporting patient", e);
            }
        }
    }
}
