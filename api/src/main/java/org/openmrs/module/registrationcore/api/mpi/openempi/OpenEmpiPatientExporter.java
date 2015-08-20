package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiPatientExporter implements MpiPatientExporter {

    @Autowired
    @Qualifier("registrationcore.openEmpiPatientQueryBuilder")
    private OpenEmpiPatientQueryBuilder queryBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Autowired
    @Qualifier("registrationcore.restQueryExecutor")
    private RestQueryExecutor queryExecutor;

    @Autowired
    @Qualifier("registrationcore.identifierBuilder")
    private IdentifierBuilder identifierBuilder;

    @Autowired
    @Qualifier("patientService")
    private PatientService patientService;

    @Override
    public void exportPatient(Patient patient) {
        OpenEmpiPatientQuery patientQuery = queryBuilder.build(patient);
        removeOpenEmpiGlobalIdentifier(patientQuery);

        OpenEmpiPatientQuery exportedMpiPatient = queryExecutor.exportPatient(authenticator.getToken(), patientQuery);

        updatePatientPersonIdentifier(patient, exportedMpiPatient.getPersonId());
    }

    private void removeOpenEmpiGlobalIdentifier(OpenEmpiPatientQuery patientQuery) {
        Integer mpiGlobalIdentifierId = mpiProperties.getGlobalIdentifierDomainId();
        for (PersonIdentifier personIdentifier : patientQuery.getPersonIdentifiers()) {
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(mpiGlobalIdentifierId)) {
                patientQuery.getPersonIdentifiers().remove(personIdentifier);
            }
        }
    }

    private void updatePatientPersonIdentifier(Patient patient, Integer personId) {
        grandPrivileges();
        try {
            setPersonIdentifier(patient, personId);
            updatePatient(patient);
        } finally {
            resetPrivileges();
        }
    }

    private void setPersonIdentifier(Patient patient, Integer personId) {
        PatientIdentifier personIdentifier = identifierBuilder.createIdentifier(mpiProperties.getMpiPersonIdentifierId(), String.valueOf(personId), null);
        patient.getIdentifiers().add(personIdentifier);
    }

    private void updatePatient(Patient patient) {
        patientService.savePatient(patient);
    }

    private void grandPrivileges() {
        Context.addProxyPrivilege("Get Identifier Types");
        Context.addProxyPrivilege("Get Patients");
        Context.addProxyPrivilege("Get Locations");
        Context.addProxyPrivilege("Add Patients");
        Context.addProxyPrivilege("Edit Patients");
    }

    private void resetPrivileges() {
        Context.removeProxyPrivilege("Get Identifier Types");
        Context.removeProxyPrivilege("Get Patients");
        Context.removeProxyPrivilege("Get Locations");
        Context.removeProxyPrivilege("Add Patients");
        Context.removeProxyPrivilege("Edit Patients");
    }
}
