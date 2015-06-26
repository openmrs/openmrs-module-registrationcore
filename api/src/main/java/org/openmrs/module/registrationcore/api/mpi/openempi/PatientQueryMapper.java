package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.*;

import java.util.Collections;
import java.util.HashSet;

public class PatientQueryMapper {

    public static OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
//        patientQuery.setMiddleName(patient.getMiddleName());
//        patientQuery.setDateOfBirth(patient.getBirthdate());

        return patientQuery;
    }

    public static Patient convert(OpenEmpiPatientQuery patientQuery) {
        Patient patient = new Patient();

        patient.setPatientId(patientQuery.getPersonId());

        setNames(patientQuery, patient);

        setIdentifiers(patientQuery, patient);

        patient.setGender(patientQuery.getGender().getGenderCode());

        patient.setBirthdate(patientQuery.getDateOfBirth());

        setAddresses(patientQuery, patient);
        return patient;
    }

    private static void setNames(OpenEmpiPatientQuery patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new HashSet<PersonName>(Collections.singleton(names)));
    }

    private static void setIdentifiers(OpenEmpiPatientQuery patientQuery, Patient patient) {
        HashSet<PatientIdentifier> identifiers = new HashSet<PatientIdentifier>();
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier(patientQuery.getPersonIdentifiers().getIdentifier());
        identifiers.add(identifier);
        patient.setIdentifiers(identifiers);
    }

    private static void setAddresses(OpenEmpiPatientQuery patientQuery, Patient patient) {
        HashSet<PersonAddress> addresses = new HashSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }
}
