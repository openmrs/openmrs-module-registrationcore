package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;

import javax.jms.Message;


public class PatientCreationListener extends PatientActionListener implements EventListener {

    private final Log log = LogFactory.getLog(this.getClass());

    private MpiPatientExporter mpiPatientExporter;

    public void setMpiPatientExporter(MpiPatientExporter mpiPatientExporter) {
        this.mpiPatientExporter = mpiPatientExporter;
    }

    public void init() {
        Event.subscribe(RegistrationCoreConstants.REGISTRATION_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void onMessage(Message message) {
        log.info("Patient creation event handled, try perform export patient to MPI.");
        Context.openSession();
        Patient createdPatient = extractPatient(message);
        mpiPatientExporter.exportPatient(createdPatient);
        Context.closeSession();
    }
}
