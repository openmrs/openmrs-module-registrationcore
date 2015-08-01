package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;

import static org.mockito.Mockito.when;

//perform actual querying to remote server.
public class RestQueryCreatorIntegrationTest {
    private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";
    private static final String SERVER_URL = "188.166.56.70:8080/openempi-admin";
    private static final String TOKEN = "DCBE246033E134DE2CA58163C7F5A1E6";
    private static final Integer OPENEMPI_GLOBAL_DOMAIN_ID = 60;

    private XmlMarshaller xmlMarshaller = new XmlMarshaller();
    @Mock private MpiProperties properties;
    @InjectMocks private RestQueryCreator queryCreator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockProperties();
    }

    private void mockProperties() {
        when(properties.getServerUrl()).thenReturn(SERVER_URL);
    }

//    @Test
    //Use this test for testing real querying to MPI server.
    public void testPerformExport() throws Exception {
        OpenEmpiPatientQuery person = createPerson();

        queryCreator.exportPatient(TOKEN, person);
    }

    private OpenEmpiPatientQuery createPerson() throws Exception {
        OpenEmpiPatientQuery person = getPerson();
        removeGlobalIdentifier(person);
        return person;
    }

    private OpenEmpiPatientQuery getPerson() throws Exception {
        return xmlMarshaller.getQuery(PATIENT_WITH_OPENMRS_ID);
    }

    private void removeGlobalIdentifier(OpenEmpiPatientQuery person) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentifiers()) {
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(OPENEMPI_GLOBAL_DOMAIN_ID)) {
                person.getPersonIdentifiers().remove(personIdentifier);
            }
        }
    }
}