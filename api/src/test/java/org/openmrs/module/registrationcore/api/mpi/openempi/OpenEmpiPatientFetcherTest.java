package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreSensitiveTestBase;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiPatientResult.PersonIdentifier;

public class OpenEmpiPatientFetcherTest extends RegistrationCoreSensitiveTestBase {

    private static final String PATIENT_WITH_OPENMRS_ID = "patient_with_openmrs_id.xml";
    private static final String PATIENT_WITHOUT_OPENMRS_ID = "patient_without_openmrs_id.xml";
    private static final String GP_MPI_USERNAME = "mpi_username";
    private static final String GP_MPI_PASSWORD = "mpi_password";

    //identifiers mapping:
    private static final String MPI_IDENTIFIER_TYPE_OPENEMRS_NAME = "OpenMRS";
    private static final String MPI_IDENTIFIER_TYPE_ID_OPENMRS = "13";
    private static final String LOCAL_IDENTIFIER_TYPE_ID_OPENMRS = "3";

    private static final String MPI_IDENTIFIER_TYPE_OPENEMPI_NAME = "OpenEMPI";
    private static final String MPI_IDENTIFIER_TYPE_ID_OPENEMPI = "18";
    private static final String LOCAL_IDENTIFIER_TYPE_ID_OPENEMPI = "4";

    private static final String MPI_IDENTIFIER_TYPE_ECID_NAME = "ECID";
    private static final String MPI_IDENTIFIER_TYPE_ID_ECID = "60";
    private static final String LOCAL_IDENTIFIER_TYPE_ID_ECID = "5";

    private static final String MPI_PERSON_ID = "2";
    private static final String TOKEN_VALUE = "token_value";
    private static final String PERSON_IDENTIFIER_TYPE_ID = "6";

    private RegistrationCoreService service;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService adminService;

    @Autowired
    @Qualifier("patientService")
    private PatientService patientService;

    @Autowired
    @Qualifier("locationService")
    private LocationService locationService;

    @Autowired
    private ApplicationContext context;

    private XmlMarshaller marshaller = new XmlMarshaller();
    private RestTemplate mockRestTemplate = mock(RestTemplate.class);

    @Before
    public void setUp() throws Exception {
        service = Context.getService(RegistrationCoreService.class);

        executeDataSets();

        saveGlobalProperties();

        mockResTemplate();
    }

    private void executeDataSets() throws Exception {
        //executeDataSet("idgen_dataset.xml");
        executeDataSet("identifiers_dataset.xml");
    }

    private void saveGlobalProperties() {
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_MPI_IMPLEMENTATION, "registrationcore.mpi.implementation.OpenEMPI"));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID, "1"));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME, GP_MPI_USERNAME));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD, GP_MPI_PASSWORD));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_MPI_URL, "server.url"));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_MPI_PERSON_IDENTIFIER_ID, PERSON_IDENTIFIER_TYPE_ID));

        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP + MPI_IDENTIFIER_TYPE_OPENEMRS_NAME, LOCAL_IDENTIFIER_TYPE_ID_OPENMRS + ":" + MPI_IDENTIFIER_TYPE_ID_OPENMRS));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP + MPI_IDENTIFIER_TYPE_OPENEMPI_NAME, LOCAL_IDENTIFIER_TYPE_ID_OPENEMPI + ":" + MPI_IDENTIFIER_TYPE_ID_OPENEMPI));
        adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP + MPI_IDENTIFIER_TYPE_ECID_NAME, LOCAL_IDENTIFIER_TYPE_ID_ECID + ":" + MPI_IDENTIFIER_TYPE_ID_ECID));
    }

    private void mockResTemplate() {
        RestQueryExecutor queryExecutor = (RestQueryExecutor) context.getBean("registrationcore.restQueryExecutor");
        ReflectionTestUtils.setField(queryExecutor, "restTemplate", mockRestTemplate);
    }

    @Test
    public void testPerformCorrectImportForPatientWithoutOpenMrsIdentifier() throws Exception {
        mockMpiAuthentication();
        OpenEmpiPatientResult mpiPatient = marshaller.getQuery(PATIENT_WITHOUT_OPENMRS_ID);
        mockMpiResponse(mpiPatient);

        String uuid = service.importMpiPatient(MPI_PERSON_ID);

        verifyRemoteMpiServerQuerying();
        Patient savedPatient = patientService.getPatientByUuid(uuid);
        assertPatientEquals(mpiPatient, savedPatient, 4);
    }

    @SuppressWarnings("unchecked")
    private void mockMpiAuthentication() {
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity(TOKEN_VALUE, HttpStatus.OK));
    }

    @SuppressWarnings("unchecked")
    private void mockMpiResponse(Object response) {
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OpenEmpiPatientResult.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.OK));
    }

    private void verifyRemoteMpiServerQuerying() {
        verify(mockRestTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(OpenEmpiPatientResult.class));
    }

    private void assertPatientEquals(OpenEmpiPatientResult mpiPatient, Patient savedPatient, int idCount) {
        assertNotNull(savedPatient.getPatientId());
        assertNotNull(savedPatient.getPersonId());
        assertEquals(savedPatient.getIdentifiers().size(), idCount);
        assertNotNull(savedPatient.getPatientIdentifier());
        assertEquals(savedPatient.getPatientIdentifier().getLocation(), locationService.getDefaultLocation());
        assertEqualsPatients(mpiPatient, savedPatient);
    }

    private void assertEqualsPatients(OpenEmpiPatientResult mpiPatient, Patient savedPatient) {
        assertEquals(mpiPatient.getGivenName(), savedPatient.getGivenName());
        assertEquals(mpiPatient.getFamilyName(), savedPatient.getFamilyName());
        assertEquals(mpiPatient.getGender().getGenderCode(), savedPatient.getGender());
        for (PersonIdentifier personIdentifier : mpiPatient.getPersonIdentifiers()) {
            assertContainsIdentifier(personIdentifier, savedPatient);
        }
    }

    private void assertContainsIdentifier(PersonIdentifier personIdentifier, Patient savedPatient) {
        String identifierName = personIdentifier.getIdentifierDomain().getIdentifierDomainName();
        for (PatientIdentifier patientIdentifier : savedPatient.getIdentifiers()) {
            if (patientIdentifier.getIdentifierType().getName().contains(identifierName)) {
                assertEquals(personIdentifier.getIdentifier(), patientIdentifier.getIdentifier());
                return;
            }
        }
        throw new RuntimeException("Patient identifiers doesn't contains identifier: " + identifierName);
    }

    @Test
    public void testPerformCorrectImportForPatientWithOpenMrsIdentifier() throws Exception {
        mockMpiAuthentication();
        OpenEmpiPatientResult mpiPatient = marshaller.getQuery(PATIENT_WITH_OPENMRS_ID);
        mockMpiResponse(mpiPatient);

        String uuid = service.importMpiPatient(MPI_PERSON_ID);

        verifyRemoteMpiServerQuerying();
        Patient savedPatient = patientService.getPatientByUuid(uuid);
        assertPatientEquals(mpiPatient, savedPatient, 3);
    }
}