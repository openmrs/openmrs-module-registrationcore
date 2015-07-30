package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

public interface PatientExport {

    void export(Patient patient);
}
