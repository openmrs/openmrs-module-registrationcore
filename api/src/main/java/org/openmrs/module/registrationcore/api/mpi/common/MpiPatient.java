package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

public class MpiPatient extends Patient {

    private static final boolean mpiPatient = true;

    public boolean getMpiPatient() {
        return mpiPatient;
    }
}
