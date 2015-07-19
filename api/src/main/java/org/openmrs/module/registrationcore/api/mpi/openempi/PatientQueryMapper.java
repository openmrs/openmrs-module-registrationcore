package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.*;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class PatientQueryMapper {

    private Location location;

    private PatientIdentifierType openEmpiIdentifierType;

    public OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
//        patientQuery.setMiddleName(patient.getMiddleName());
//        patientQuery.setDateOfBirth(patient.getBirthdate());

        return patientQuery;
    }

    public MpiPatient convert(OpenEmpiPatientQuery patientQuery) {
        MpiPatient patient = new MpiPatient();
        patient.setMpiPatient(true);

        patient.setPatientId(patientQuery.getPersonId());

        setNames(patientQuery, patient);

        setOpenEmpiId(patientQuery, patient);

        patient.setGender(patientQuery.getGender().getGenderCode());

        setBirthdate(patientQuery, patient);

        setAddresses(patientQuery, patient);
        return patient;
    }

    private void setNames(OpenEmpiPatientQuery patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new HashSet<PersonName>(Collections.singleton(names)));
    }

    private void setOpenEmpiId(OpenEmpiPatientQuery patientQuery, Patient patient) {
        Integer mpiPersonId = patientQuery.getPersonId();

        PatientIdentifier identifier =
                new PatientIdentifier(String.valueOf(mpiPersonId), openEmpiIdentifierType, location);

        patient.addIdentifier(identifier);
    }

    private void setAddresses(OpenEmpiPatientQuery patientQuery, Patient patient) {
        HashSet<PersonAddress> addresses = new HashSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }

    private void setBirthdate(OpenEmpiPatientQuery patientQuery, Patient patient) {
        if (patientQuery.getDateOfBirth() == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(patientQuery.getDateOfBirth());
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date clearDate = calendar.getTime();
        patient.setBirthdate(clearDate);
    }
}
