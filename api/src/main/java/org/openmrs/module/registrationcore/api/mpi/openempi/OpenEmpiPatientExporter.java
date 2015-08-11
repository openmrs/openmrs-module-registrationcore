package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
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
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator queryCreator;

    @Override
    public void exportPatient(Patient patient) {
        OpenEmpiPatientQuery patientQuery = queryBuilder.build(patient);
        removeOpenEmpiGlobalIdentifier(patientQuery);

        queryCreator.exportPatient(authenticator.getToken(), patientQuery);
    }

    private void removeOpenEmpiGlobalIdentifier(OpenEmpiPatientQuery patientQuery) {
        Integer mpiGlobalIdentifierId = mpiProperties.getGlobalIdentifierDomainId();
        for (PersonIdentifier personIdentifier : patientQuery.getPersonIdentifiers()) {
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(mpiGlobalIdentifierId)) {
                patientQuery.getPersonIdentifiers().remove(personIdentifier);
            }
        }
    }
}
