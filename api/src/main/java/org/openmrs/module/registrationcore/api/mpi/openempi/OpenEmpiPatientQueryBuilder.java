package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.Set;

public class OpenEmpiPatientQueryBuilder {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.identifierMapper")
    private PatientIdentifierMapper identifierMapper;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public OpenEmpiPatientResult build(Patient patient) {
        OpenEmpiPatientResult query = new OpenEmpiPatientResult();

        setGender(query, patient);

        setNames(query, patient);

        setBirthdate(query, patient);

        setAddresses(query, patient);

        setIdentifiers(query, patient);

        return query;
    }

    private void setGender(OpenEmpiPatientResult query, Patient patient) {
        Gender gender = new Gender();
        gender.setGenderCode(patient.getGender());
        query.setGender(gender);
    }

    private void setNames(OpenEmpiPatientResult query, Patient patient) {
        query.setFamilyName(patient.getFamilyName());
        query.setGivenName(patient.getGivenName());
        query.setMiddleName(patient.getMiddleName());
    }

    private void setBirthdate(OpenEmpiPatientResult query, Patient patient) {
        query.setDateOfBirth(patient.getBirthdate());
    }

    private void setAddresses(OpenEmpiPatientResult query, Patient patient) {
        //TODO improve it by setting all available addresses.
        Set<PersonAddress> addresses = patient.getAddresses();
        for (PersonAddress address : addresses) {
            query.setAddress1(address.getAddress1());
            return;
        }
    }

    private void setIdentifiers(OpenEmpiPatientResult query, Patient patient) {
        LinkedList<PersonIdentifier> personIdentifiers = new LinkedList<PersonIdentifier>();

        for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
            Integer identifierTypeId = patientIdentifier.getIdentifierType().getId();
            String identifier = patientIdentifier.getIdentifier();
            if (identifierTypeId.equals(mpiProperties.getMpiPersonIdentifierTypeId())) {
                //person identifier should be placed in separate field, not in list of identifiers.
                setPersonIdentifier(identifier, query);
                continue;
            }
            Integer mpiIdentifierTypeId = identifierMapper.getMappedMpiIdentifierTypeId(identifierTypeId);

            if (mpiIdentifierTypeId != null) {
                personIdentifiers.add(createPersonIdentifier(mpiIdentifierTypeId, identifier));
            } else {
                log.error("Do not add patient identifier type id: " + identifierTypeId + " to Exported patient. " +
                        "Reason: there is no matched appropriate MPI identifier for this type.");
            }
        }
        query.setPersonIdentifiers(personIdentifiers);
    }

    private void setPersonIdentifier(String identifier, OpenEmpiPatientResult patientQuery) {
        log.error("set person idetnfier: " + identifier);
        patientQuery.setPersonId(Integer.parseInt(identifier));
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
