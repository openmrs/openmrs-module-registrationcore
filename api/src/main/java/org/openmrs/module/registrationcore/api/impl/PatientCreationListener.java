package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientQuery;

import javax.jms.Message;

public class PatientCreationListener extends PatientActionListener {

    private MpiProperties mpiProperties;

    private IdentifierBuilder identifierBuilder;

    private UserService userService;

    public void setMpiProperties(MpiProperties mpiProperties) {
        this.mpiProperties = mpiProperties;
    }

    public void setIdentifierBuilder(IdentifierBuilder identifierBuilder) {
        this.identifierBuilder = identifierBuilder;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void init() {
        Event.subscribe(RegistrationCoreConstants.PATIENT_REGISTRATION_EVENT_TOPIC_NAME, this);
    }

    @Override
    public void performMpiAction(Message message) {
        Patient patient = extractPatient(message);

        OpenEmpiPatientQuery exportedPatientQuery = pushPatientToMpiServer(patient);

        updatePatient(patient, message, exportedPatientQuery.getPersonId());
    }

    private OpenEmpiPatientQuery pushPatientToMpiServer(Patient patient) {
        return coreProperties.getMpiProvider().exportPatient(patient);
    }

    private void updatePatient(Patient patient, Message message, Integer personId) {
        grandPrivileges();
        try {
            User creator = extractPatientCreator(message);
            addPersonIdentifier(patient, creator, personId);
            patientService.savePatient(patient);
        } finally {
            resetPrivileges();
        }
    }

    private User extractPatientCreator(Message message) {
        String creatorId = getMessagePropertyValue(message, RegistrationCoreConstants.KEY_REGISTERER_ID);
        return userService.getUser(Integer.parseInt(creatorId));
    }

    private void addPersonIdentifier(Patient patient, User creator, Integer personId) {
        PatientIdentifier personIdentifier = createPersonIdentifier(creator, personId);
        patient.getIdentifiers().add(personIdentifier);
    }

    private PatientIdentifier createPersonIdentifier(User creator, Integer personId) {
        PatientIdentifier personIdentifier = identifierBuilder
                .createIdentifier(mpiProperties.getMpiPersonIdentifierTypeId(), String.valueOf(personId), null);
        personIdentifier.setCreator(creator);
        return personIdentifier;
    }

    private void grandPrivileges() {
        Context.addProxyPrivilege("Get Users");
        Context.addProxyPrivilege("Get Identifier Types");
        Context.addProxyPrivilege("Get Locations");
        Context.addProxyPrivilege("Add Patient Identifiers");
        Context.addProxyPrivilege("Edit Patient Identifiers");
        Context.addProxyPrivilege("Get Patients");
        Context.addProxyPrivilege("Add Patients");
        Context.addProxyPrivilege("Edit Patients");
    }

    private void resetPrivileges() {
        Context.removeProxyPrivilege("Get Users");
        Context.removeProxyPrivilege("Get Identifier Types");
        Context.removeProxyPrivilege("Get Locations");
        Context.removeProxyPrivilege("Add Patient Identifiers");
        Context.removeProxyPrivilege("Edit Patient Identifiers");
        Context.removeProxyPrivilege("Get Patients");
        Context.removeProxyPrivilege("Add Patients");
        Context.removeProxyPrivilege("Edit Patients");
    }
}
