package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;

/**
 * Defines a MpiPatient in the system. A mpi patient is simply an extension of a patient and all that that
 * implies.
 */
public class MpiPatient extends Patient {

    /**
     * Checks if patients is MpiPatient
     * @return true if it is mpi patient, otherwise false
     */
    public boolean getMpiPatient() {
        return true;
    }
}
