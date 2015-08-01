package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;

public class OpenEmpiPatientExporter implements MpiPatientExporter {

    private OpenEmpiPatientQueryBuilder queryBuilder;

    private MpiProperties mpiProperties;

    private MpiAuthenticator authenticator;

    private RestQueryCreator queryCreator;

    public void setOpenEmpiPatientQueryBuilder(OpenEmpiPatientQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public void setMpiProperties(MpiProperties mpiProperties) {
        this.mpiProperties = mpiProperties;
    }

    public void setMpiAuthenticator(MpiAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setRestQueryCreator(RestQueryCreator queryCreator) {
        this.queryCreator = queryCreator;
    }

    @Override
    public void export(Patient patient) {
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
