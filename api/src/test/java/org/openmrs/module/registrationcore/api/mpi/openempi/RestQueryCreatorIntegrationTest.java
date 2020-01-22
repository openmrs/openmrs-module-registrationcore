package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

//perform actual querying to remote server.
//Use this tests for testing real querying to MPI server.
public class RestQueryCreatorIntegrationTest {
    private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";
    private static final String SERVER_URL = "188.166.56.70:8080/openempi-admin";
    private static final String TOKEN = "DCBE246033E134DE2CA58163C7F5A1E6";
    private static final Integer OPENEMPI_GLOBAL_DOMAIN_ID = 60;

    private XmlMarshaller xmlMarshaller = new XmlMarshaller();
    @Mock
    private MpiProperties properties;
    private RestUrlBuilder urlBuilder = new RestUrlBuilder();
    @InjectMocks
    private RestQueryExecutor queryCreator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(properties.getServerUrl()).thenReturn(SERVER_URL);
        ReflectionTestUtils.setField(urlBuilder, "properties", properties);
        ReflectionTestUtils.setField(queryCreator, "urlBuilder", urlBuilder);
    }

    // @Test
    public void testPerformExport() throws Exception {
        OpenEmpiPatientResult person = getPerson();
        removeGlobalIdentifier(person);

        queryCreator.exportPatient(TOKEN, person);
    }

    // @Test
    public void testPerformUpdate() throws Exception {
        OpenEmpiPatientResult person = getPerson();

        person.setGivenName("Daniel");
        person.setFamilyName("Ocean");

        queryCreator.updatePatient(TOKEN, person);
    }

    private OpenEmpiPatientResult getPerson() throws Exception {
        return xmlMarshaller.getQuery(PATIENT_WITH_OPENMRS_ID);
    }

    private void removeGlobalIdentifier(OpenEmpiPatientResult person) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentifiers()) {
            if (personIdentifier.getIdentifierDomain().getIdentifierDomainId().equals(OPENEMPI_GLOBAL_DOMAIN_ID)) {
                person.getPersonIdentifiers().remove(personIdentifier);
            }
        }
    }
}