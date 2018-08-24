package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

public class PatientBuilder {

    private final Log log = LogFactory.getLog(this.getClass());

    private RegistrationCoreProperties registrationCoreProperties;

    @Autowired
    @Qualifier("registrationcore.identifierMapper")
    private PatientIdentifierMapper identifierMapper;

    @Autowired
    @Qualifier("registrationcore.identifierBuilder")
    private IdentifierBuilder identifierBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    private MpiPatient patient;

    public void setPatient(MpiPatient patient) {
        this.patient = patient;
    }

    public MpiPatient buildPatient(OpenEmpiPatientResult patientQuery) {
        patient.setGender(patientQuery.getGender().getGenderCode());

        setNames(patientQuery, patient);

        setBirthdate(patientQuery, patient);

        setAddresses(patientQuery, patient);

        setIdentifiers(patientQuery, patient);
        return patient;
    }

    private void setNames(OpenEmpiPatientResult patientQuery, Patient patient) {
        PersonName names = new PersonName();
        names.setFamilyName(patientQuery.getFamilyName());
        names.setGivenName(patientQuery.getGivenName());
        patient.setNames(new TreeSet<PersonName>(Collections.singleton(names)));
    }

    private void setBirthdate(OpenEmpiPatientResult patientQuery, Patient patient) {
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

    private void setAddresses(OpenEmpiPatientResult patientQuery, Patient patient) {
        Set<PersonAddress> addresses = new TreeSet<PersonAddress>();
        PersonAddress address = new PersonAddress();
        address.setAddress1(patientQuery.getAddress1());
        addresses.add(address);
        patient.setAddresses(addresses);
    }

    private void setIdentifiers(OpenEmpiPatientResult patientQuery, Patient patient) {
        setMpiPatientIdentifiers(patientQuery, patient);
        setMpiPersonIdentifier(patientQuery, patient);
    }

    private void setMpiPatientIdentifiers(OpenEmpiPatientResult patientQuery, Patient patient) {
        for (PersonIdentifier identifier : patientQuery.getPersonIdentifiers()) {
            String mpiIdentifierTypeName = identifier.getIdentifierDomain().getIdentifierDomainName();
            Integer mpiIdentifierTypeId = identifier.getIdentifierDomain().getIdentifierDomainId();

            String localIdentifierTypeUuid = identifierMapper.getMappedLocalIdentifierTypeUuid(mpiIdentifierTypeId.toString());
            String identifierValue = identifier.getIdentifier();
            PatientIdentifier patientIdentifier = createIdentifier(mpiIdentifierTypeName, localIdentifierTypeUuid, identifierValue);

            patient.addIdentifier(patientIdentifier);
        }
    }

    private void setMpiPersonIdentifier(OpenEmpiPatientResult patientQuery, Patient patient) {
        String personIdentifierTypeUuid = mpiProperties.getMpiPersonIdentifierTypeUuid();
        PatientIdentifier identifier = createIdentifier("Person identifier", personIdentifierTypeUuid, String.valueOf(patientQuery.getPersonId()));
        patient.getIdentifiers().add(identifier);
    }

    private PatientIdentifier createIdentifier(String identifierName, String identifierTypeUuid, String identifierValue) {
        log.info("Create identifier for imported Mpi patient. Identifier name: " + identifierName + ". Identifier UUID: "
                + identifierTypeUuid + ". Identifier value: " + identifierValue);
        PatientIdentifier identifier = identifierBuilder.createIdentifier(identifierTypeUuid, identifierValue, null);
        if (registrationCoreProperties.getOpenMrsIdentifierUuid().equals(identifierTypeUuid)) {
            identifier.setPreferred(true);
        }
        return identifier;
    }
}
