package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;

public class FindPatientQueryBuilder {

    public OpenEmpiPatientResult create(Patient patient) {
        OpenEmpiPatientResult patientQuery = new OpenEmpiPatientResult();

        //perform search by Family and Given names
        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
        return patientQuery;
    }
}
