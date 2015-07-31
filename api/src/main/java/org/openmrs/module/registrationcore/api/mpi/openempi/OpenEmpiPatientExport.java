package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.PatientExport;

public class OpenEmpiPatientExport implements PatientExport {

    private OpenEmpiPatientQueryBuilder queryBuilder;

    private PatientIdentifierMapper identifierMapper;

    private MpiAuthenticator authenticator;

    private RestQueryCreator queryCreator;

    public void setOpenEmpiPatientQueryBuilder(OpenEmpiPatientQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public void setPatientIdentifierMapper(PatientIdentifierMapper patientIdentifierMapper) {
        this.identifierMapper = patientIdentifierMapper;
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
        Integer mpiGlobalIdentifierId = identifierMapper.getMpiGlobalIdentifierDomainId();
        for (PersonIdentifier personIdentifier : patientQuery.getPersonIdentifiers()) {
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(mpiGlobalIdentifierId)) {
                patientQuery.getPersonIdentifiers().remove(personIdentifier);
            }
        }
    }
}
