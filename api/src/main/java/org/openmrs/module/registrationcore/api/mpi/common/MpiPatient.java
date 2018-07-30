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

    /**
     * Default constructor
     */
    public MpiPatient(){}

    /**
     * This constructor creates a new MpiPatient object from the given {@link Patient} object. All
     * attributes are copied over to the new object. NOTE! All child collection objects are copied
     * as pointers, each individual element is not copied. <br>
     * <br>
     *
     * @param patient the patient object to copy onto a new Patient
     * @see MpiPatient#MpiPatient(Patient)
     */
    public MpiPatient (Patient patient) {
        super(patient);
    }
}
