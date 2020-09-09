package org.openmrs.module.registrationcore.api.mpi.fhir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientUpdater;

public class FhirPatientUpdater implements MpiPatientUpdater {

    private final Log log = LogFactory.getLog(this.getClass());


    @Override
    public void updatePatient(Patient patient) {
        try {
//            Util to update the patient
//            Message response = hl7SenderHolder.getHl7v2Sender().sendPixMessage(updateMsg);

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
