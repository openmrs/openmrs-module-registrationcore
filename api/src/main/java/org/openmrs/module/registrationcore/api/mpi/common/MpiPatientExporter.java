package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

public interface MpiPatientExporter {

    void export(Patient patient);
}
