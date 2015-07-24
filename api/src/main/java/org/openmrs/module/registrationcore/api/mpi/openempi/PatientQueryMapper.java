package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.IdentifierGenerator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

import java.util.*;

public class PatientQueryMapper {

    private static final String OPENMRS_IDENTIFIER_NAME = "OpenMRS";
    protected final Log log = LogFactory.getLog(this.getClass());

    private IdentifierGenerator identifierGenerator;

    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    public OpenEmpiPatientQuery convert(Patient patient) {
        OpenEmpiPatientQuery patientQuery = new OpenEmpiPatientQuery();

        //perform search by Family and Given names
        patientQuery.setFamilyName(patient.getFamilyName());
        patientQuery.setGivenName(patient.getGivenName());
        return patientQuery;
    }

    public Patient convert(OpenEmpiPatientQuery patientQuery) {
        Patient patient = convertPatient(new MpiPatient(), patientQuery);
        patient.setPatientId(patientQuery.getPersonId());
        return patient;
    }

    public Patient importPatient(OpenEmpiPatientQuery patientQuery) {
        Patient patient = convertPatient(new Patient(), patientQuery);
        if (!containsOpenMrsIdentifier(patientQuery)) {
            generateOpenMrsIdentifier(patient);
        }
        return patient;
    }

    private Patient convertPatient(Patient patient, OpenEmpiPatientQuery patientQuery) {
        patient.setDateCreated(new Date());

        patient.setGender(patientQuery.getGender().getGenderCode());

        setNames(patientQuery, patient);

        setBirthdate(patientQuery, patient);

        setAddresses(patientQuery, patient);

        setIdentifiers(patientQuery, patient);
        return patient;
    }

    private void setNames(OpenEmpiPatientQuery patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new TreeSet<PersonName>(Collections.singleton(names)));
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

    private void setAddresses(OpenEmpiPatientQuery patientQuery, Patient patient) {
        Set<PersonAddress> addresses = new TreeSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }

    private boolean containsOpenMrsIdentifier(OpenEmpiPatientQuery patientQuery) {
        for (PersonIdentifier personIdentifier : patientQuery.getPersonIdentifiers()) {
            if (OPENMRS_IDENTIFIER_NAME.equals(personIdentifier.getIdentifierDomain().getIdentifierDomainName())) {
                return true;
            }
        }
        return false;
    }

    private void generateOpenMrsIdentifier(Patient patient) {
        log.info("Generate OpenMRS identifier for imported Mpi patient.");
        Integer openMrsIdentifierId = identifierGenerator.getOpenMrsIdentifier();
        PatientIdentifier identifier = identifierGenerator.generateIdentifier(openMrsIdentifierId, null);
        identifier.setPreferred(true);
        patient.addIdentifier(identifier);
    }

    private void setIdentifiers(OpenEmpiPatientQuery patientQuery, Patient patient) {
        for (PersonIdentifier identifier : patientQuery.getPersonIdentifiers()) {
            String identifierName = identifier.getIdentifierDomain().getIdentifierDomainName();
            Integer identifierId = identifierGenerator.getIdentifierIdByName(identifierName);
            String identifierValue = identifier.getIdentifier();

            PatientIdentifier patientIdentifier = createIdentifier(identifierName, identifierId, identifierValue);

            patient.addIdentifier(patientIdentifier);
        }
    }

    private PatientIdentifier createIdentifier(String identifierName, Integer identifierId, String identifierValue) {
        log.info("Create identifier for imported Mpi patient. Identifier name: " + identifierName + ". Identifier Id: "
                + identifierId + ". Identifier value: " + identifierValue);
        PatientIdentifier identifier = identifierGenerator.createIdentifier(identifierId, identifierValue, null);
        if (OPENMRS_IDENTIFIER_NAME.equals(identifierName)) {
            identifier.setPreferred(true);
        }
        return identifier;
    }
}
