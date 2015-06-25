package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.PersonName;

import java.util.Collections;
import java.util.HashSet;

public class PatientQueryMapper {

    public static OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName("Zayats");
        patientQuery.setGivenName("Roman");
//        patientQuery.setMiddleName(patient.getMiddleName());
//        patientQuery.setDateOfBirth(patient.getBirthdate());
//        patientQuery.setGenderDescription(patient.getGender());

        return patientQuery;
    }

    public static Patient convert(OpenEmpiPatientQuery patientQuery) {
        Patient patient = new Patient();

        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        names.setMiddleName(patientQuery.getMiddleName());
        patient.setNames(new HashSet<PersonName>(Collections.singleton(names)));
        patient.setBirthdate(patientQuery.getDateOfBirth());
        patient.setGender(patientQuery.getGenderDescription());

        return patient;
    }
}
