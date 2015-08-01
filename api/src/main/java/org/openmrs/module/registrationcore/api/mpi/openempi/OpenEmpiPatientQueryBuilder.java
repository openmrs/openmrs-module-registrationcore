package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.Set;

public class OpenEmpiPatientQueryBuilder {

    @Autowired
    @Qualifier("registrationcore.identifierMapper")
    private PatientIdentifierMapper identifierMapper;

    public OpenEmpiPatientQuery build(Patient patient) {
        OpenEmpiPatientQuery query = new OpenEmpiPatientQuery();

        setGender(query, patient);

        setNames(query, patient);

        setBirthdate(query, patient);

        setAddresses(query, patient);

        setIdentifiers(query, patient);

        return query;
    }

    private void setGender(OpenEmpiPatientQuery query, Patient patient) {
        Gender gender = new Gender();
        gender.setGenderCode(patient.getGender());
        query.setGender(gender);
    }

    private void setNames(OpenEmpiPatientQuery query, Patient patient) {
        query.setFamilyName(patient.getFamilyName());
        query.setGivenName(patient.getGivenName());
        query.setMiddleName(patient.getMiddleName());
    }

    private void setBirthdate(OpenEmpiPatientQuery query, Patient patient) {
        query.setDateOfBirth(patient.getBirthdate());
    }

    private void setAddresses(OpenEmpiPatientQuery query, Patient patient) {
        //TODO improve it by setting all available addresses.
        Set<PersonAddress> addresses = patient.getAddresses();
        for (PersonAddress address : addresses) {
            query.setAddress1(address.getAddress1());
            return;
        }
    }

    private void setIdentifiers(OpenEmpiPatientQuery query, Patient patient) {
        LinkedList<PersonIdentifier> personIdentifiers = new LinkedList<PersonIdentifier>();

        for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
            Integer identifierTypeId = patientIdentifier.getIdentifierType().getId();
            String identifier = patientIdentifier.getIdentifier();
            Integer mpiIdentifierTypeId = identifierMapper.getMappedMpiIdentifierTypeId(identifierTypeId);

            if (mpiIdentifierTypeId != null) {
                personIdentifiers.add(createPersonIdentifier(mpiIdentifierTypeId, identifier));
            }
        }
        query.setPersonIdentifiers(personIdentifiers);
    }

    private PersonIdentifier createPersonIdentifier(Integer mpiIdentifierTypeId, String identifier) {
        PersonIdentifier personIdentifier = new PersonIdentifier();

        IdentifierDomain identifierDomain = new IdentifierDomain();
        identifierDomain.setIdentifierDomainId(mpiIdentifierTypeId);
        personIdentifier.setIdentifierDomain(identifierDomain);

        personIdentifier.setIdentifier(identifier);
        return personIdentifier;
    }
}
