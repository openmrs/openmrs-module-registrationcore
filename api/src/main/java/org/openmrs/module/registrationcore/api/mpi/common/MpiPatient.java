package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

public class MpiPatient extends Patient{

    private boolean mpiPatient;

    public boolean getMpiPatient() {
        return mpiPatient;
    }

    public void setMpiPatient(boolean mpiPatient) {
        this.mpiPatient = mpiPatient;
    }
}
