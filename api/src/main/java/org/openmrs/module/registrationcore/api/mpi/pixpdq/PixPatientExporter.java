package org.openmrs.module.registrationcore.api.mpi.pixpdq;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PixPatientExporter implements MpiPatientExporter {

    @Autowired
    @Qualifier("registrationcore.mpiHl7v2Sender")
    private Hl7v2Sender hl7v2Sender;

    @Autowired
    @Qualifier("registrationcore.mpiPixPdqMessageUtil")
    private PixPdqMessageUtil pixPdqMessageUtil;

    @Override
    public String exportPatient(Patient patient) {
        throw new UnsupportedOperationException();
    }
}
