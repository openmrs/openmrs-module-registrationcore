package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.*;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

    public static MpiPatient convert(OpenEmpiPatientQuery patientQuery) {
        MpiPatient patient = new MpiPatient();
        patient.setMpiPatient(true);

        patient.setPatientId(patientQuery.getPersonId());

        setNames(patientQuery, patient);

        setIdentifiers(patientQuery, patient);

        patient.setGender(patientQuery.getGender().getGenderCode());

        setBirthdate(patientQuery, patient);

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

    private static void setBirthdate(OpenEmpiPatientQuery patientQuery, Patient patient) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(patientQuery.getDateOfBirth());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date clearDate = calendar.getTime();
        patient.setBirthdate(clearDate);
    }
}
