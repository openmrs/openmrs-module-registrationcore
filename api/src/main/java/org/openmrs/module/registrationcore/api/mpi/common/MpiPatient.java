package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;

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
     * Note: Requires openmrs-core version 2.2.0 for constructor method to create patient from a provided patient
     * refer to <a href="https://issues.openmrs.org/browse/TRUNK-5408">TRUNK-5408</a>
     *
     * @param patient the patient object to copy onto a new Patient
     * @see MpiPatient#MpiPatient(Patient)
     */
    public MpiPatient (Patient patient) {
        super(patient);
    }

    /**
     * Converts MpiPatient to Patient object.
     *
     * Currently required because trying to save an MpiPatient using PatientService.savePatient results in a Hibernate Error
     * org.hibernate.HibernateException: Unable to resolve entity name from Class [org.openmrs.module.registrationcore.api.mpi.common.MpiPatient] expected instance/subclass of [org.openmrs.Person]
     *
     * Note: Allergy status is not duplicated as the version of core required by the module does not include the allergy api integration
     * Note: This method is a hack and dirties the original patient as the identifiers within the original patient will
     * reference the new patient. This method should be deprecated when openmrs-core is upgraded to at least V 2.2.0
     *
     * @return patient
     * @see <a href="https://issues.openmrs.org/browse/TRUNK-5408">TRUNK-5408</a>
     */
    public Patient convertToPatient () {
        Patient patient = new Patient(this);
        patient.setIdentifiers(this.getIdentifiers());
        for (PatientIdentifier pid : patient.getIdentifiers()) {
            pid.setPatient(patient);
        }
        return patient;
    }
}
