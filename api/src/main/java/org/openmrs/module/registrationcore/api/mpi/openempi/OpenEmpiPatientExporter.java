package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientExporter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Iterator;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier;

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

    @Override
    public String exportPatient(Patient patient) {
        OpenEmpiPatientResult patientQuery = queryBuilder.build(patient);
        removeOpenEmpiGlobalIdentifier(patientQuery);

        OpenEmpiPatientResult mpiPerson = queryExecutor.exportPatient(authenticator.getToken(), patientQuery);
        return String.valueOf(mpiPerson.getPersonId());
    }

    // patient can't be exported to MPI server if it contains identifier which is Global in MPI.
    // The reason is that MPI server should manually generate Global identifier.
    private void removeOpenEmpiGlobalIdentifier(OpenEmpiPatientResult patientQuery) {
        Integer mpiGlobalIdentifierId = mpiProperties.getGlobalIdentifierDomainId();

        Iterator<PersonIdentifier> i = patientQuery.getPersonIdentifiers().iterator();

        while (i.hasNext()) {
            PersonIdentifier personIdentifier = i.next();
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(mpiGlobalIdentifierId)) {
                i.remove();
            }
        }
    }
}
