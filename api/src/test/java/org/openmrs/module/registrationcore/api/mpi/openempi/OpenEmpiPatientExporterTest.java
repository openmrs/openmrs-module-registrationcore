package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.*;
import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier.*;

@Ignore
public class OpenEmpiPatientExporterTest {

    private static final String TOKEN = "token";
    private static final int MPI_GLOBAL_DOMAIN_ID = 13;
    private static final int PERSON_ID = 13;
    @InjectMocks private OpenEmpiPatientExporter patientExport = new OpenEmpiPatientExporter();
    @Mock private OpenEmpiPatientQueryBuilder queryBuilder;
    @Mock private MpiProperties mpiProperties;
    @Mock private MpiAuthenticator authenticator;
    @Mock private RestQueryExecutor queryCreator;
    @Mock private Patient patient;
    @Mock private OpenEmpiPatientResult query;
    @Mock private OpenEmpiPatientResult queryResult;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockAuthentication();
        mockBuilder();
        mockMpiProperties();
        mockQueryIdentifiers();
        mockMpiPersonId();
    }

    @Test
    public void testExportPatient() throws Exception {
        patientExport.exportPatient(patient);

        ArgumentCaptor<OpenEmpiPatientResult> queryCaptor = ArgumentCaptor.forClass(OpenEmpiPatientResult.class);
        verify(queryCreator).exportPatient(eq(TOKEN), queryCaptor.capture());

        //assert that GlobalDomainIdentifier was removed before exportPatient:
        assertEquals(queryCaptor.getValue().getPersonIdentifiers().size(), 0);
    }

    private void mockMpiPersonId() {
        when(queryCreator.exportPatient(TOKEN, query)).thenReturn(queryResult);
        when(queryResult.getPersonId()).thenReturn(PERSON_ID);
    }

    private void mockAuthentication() {
        when(authenticator.getToken()).thenReturn(TOKEN);
    }

    private void mockBuilder() {
        when(queryBuilder.build(patient)).thenReturn(query);
    }

    private void mockMpiProperties() {
        when(mpiProperties.getGlobalIdentifierDomainId()).thenReturn(MPI_GLOBAL_DOMAIN_ID);
    }

    private void mockQueryIdentifiers() {
        LinkedList<PersonIdentifier> identifiers = new LinkedList<PersonIdentifier>();

        PersonIdentifier identifier = new PersonIdentifier();
        IdentifierDomain identifierDomain = new IdentifierDomain();
        identifierDomain.setIdentifierDomainId(MPI_GLOBAL_DOMAIN_ID);
        identifier.setIdentifierDomain(identifierDomain);
        identifiers.add(identifier);

        when(query.getPersonIdentifiers()).thenReturn(identifiers);
    }
}