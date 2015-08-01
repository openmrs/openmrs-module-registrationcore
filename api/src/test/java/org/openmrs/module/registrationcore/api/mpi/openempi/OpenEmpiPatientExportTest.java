package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
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

public class OpenEmpiPatientExportTest {

    private static final String TOKEN = "token";
    private static final int MPI_GLOBAL_DOMAIN_ID = 13;
    @InjectMocks private OpenEmpiPatientExport patientExport = new OpenEmpiPatientExport();
    @Mock private OpenEmpiPatientQueryBuilder queryBuilder;
    @Mock private MpiProperties mpiProperties;
    @Mock private MpiAuthenticator authenticator;
    @Mock private RestQueryCreator queryCreator;
    @Mock private Patient patient;
    @Mock private OpenEmpiPatientQuery query;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockAuthentication();
        mockBuilder();
        mockMpiProperties();
        mockQueryIdentifiers();
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

    @Test
    public void testExportPatient() throws Exception {
        patientExport.export(patient);

        ArgumentCaptor<OpenEmpiPatientQuery> queryCaptor = ArgumentCaptor.forClass(OpenEmpiPatientQuery.class);
        verify(queryCreator).exportPatient(eq(TOKEN), queryCaptor.capture());

        //assert that GlobalDomainIdentifier was removed before export:
        assertEquals(queryCaptor.getValue().getPersonIdentifiers().size(), 0);
    }
}