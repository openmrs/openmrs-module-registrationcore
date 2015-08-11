package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;

public class FindPatientQueryBuilder {

    public OpenEmpiPatientQuery create(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        //perform search by Family and Given names
        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
        return patientQuery;
    }
}
