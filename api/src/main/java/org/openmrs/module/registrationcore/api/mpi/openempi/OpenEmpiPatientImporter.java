package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.registrationcore.api.impl.IdentifierBuilder;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiPatientImporter implements MpiPatientImporter {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator restQueryCreator;


    @Autowired
    @Qualifier("registrationcore.identifierBuilder")
    private IdentifierBuilder identifierBuilder;

    @Autowired
    @Qualifier("registrationcore.patientBuilder")
    private PatientBuilder patientBuilder;

    @Autowired
    @Qualifier("registrationcore.mpiAuthenticator")
    private MpiAuthenticator authenticator;

    @Override
    public Patient importMpiPatient(String patientId) {
        OpenEmpiPatientQuery mpiPatient = restQueryCreator.getPatientById(authenticator.getToken(), patientId);

        return createPatient(mpiPatient);
    }

    private Patient createPatient(OpenEmpiPatientQuery mpiPatient) {
        patientBuilder.setPatient(new Patient());
        Patient patient = patientBuilder.buildPatient(mpiPatient);

        if (!containsOpenMrsIdentifier(patient))
            addOpenMrsIdentifier(patient);
        return patient;
    }

    private boolean containsOpenMrsIdentifier(Patient patientQuery) {
        for (PatientIdentifier identifier : patientQuery.getIdentifiers()) {
            if (identifier.isPreferred())
                return true;
        }
        return false;
    }

    private void addOpenMrsIdentifier(Patient patient) {
        log.info("Generate OpenMRS identifier for imported Mpi patient.");
        Integer openMrsIdentifierId = identifierBuilder.getOpenMrsIdentifierSourceId();
        PatientIdentifier identifier = identifierBuilder.generateIdentifier(openMrsIdentifierId, null);
        identifier.setPreferred(true);
        patient.addIdentifier(identifier);
    }
}
