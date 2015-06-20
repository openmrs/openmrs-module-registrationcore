package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;

public class PatientToQueryMapper {

    public static OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
        patientQuery.setMiddleName(patient.getMiddleName());
        patientQuery.setBirthDate(patient.getBirthdate());
        patientQuery.setGenderDescription(patient.getGender());

        return patientQuery;
    }
}
