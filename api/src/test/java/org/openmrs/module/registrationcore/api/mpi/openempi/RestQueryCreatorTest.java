package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiVariables.*;

public class RestQueryCreatorTest {

    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String SERVER_URL = "server.url";
    private static final String TOKEN_VALUE = "tokenValue";
    private static final String PATIENT_ID = "13";
    private static final String PATIENT_QUERY_GIVENNAME = "Michael";
    private static final String PATIENT_QUERY_FAMILYNAME = "Louis";

    @InjectMocks private RestQueryCreator restQueryCreator = new RestQueryCreator();
    @Mock private AdministrationService adminService;
    @Mock private RestTemplate restTemplate;
    @Mock private OpenEmpiPatientQuery patientQuery;
    @Mock private OpenEmpiPeopleWrapper peopleWrapper;
    @Mock private List<OpenEmpiPatientQuery> patientQueryList;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockRestTemplate();
        mockServerUrlProperty();
    }

    @Test
    public void testProcessAuthentication() throws Exception {
        mockServerResponse(TOKEN_VALUE, String.class);

        String actualToken = restQueryCreator.processAuthentication(createCredentials());

        ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        //verify correct request creation:
        verify(restTemplate).exchange(eq(createAuthenticationUrl()),
                eq(HttpMethod.PUT), argumentCaptor.capture(), eq(String.class));
        //verify request body was set correctly
        assertEquals(argumentCaptor.getValue().getBody(), createCredentials());

        assertEquals(actualToken, TOKEN_VALUE);
    }

    @Test(expected = APIException.class)
    public void testProcessAuthenticationThrowExceptionOnInvalidToken() throws Exception {
        mockServerResponse(null, String.class);

        restQueryCreator.processAuthentication(createCredentials());
    }

    @Test
    public void testGetPatientById() throws Exception {
        mockServerResponse(patientQuery, OpenEmpiPatientQuery.class);

        OpenEmpiPatientQuery actualPatient = restQueryCreator.getPatientById(TOKEN_VALUE, PATIENT_ID);

        ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        //verify correct request creation:
        verify(restTemplate).exchange(eq(createGetPatientUrl(PATIENT_ID)),
                eq(HttpMethod.GET), argumentCaptor.capture(), eq(OpenEmpiPatientQuery.class));
        assertNull(argumentCaptor.getValue().getBody());
        //verify that header with token was sett:
        assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));
        assertEquals(patientQuery, actualPatient);
    }

    @Test
    public void testFindPatients() throws Exception {
        mockPeopleWrapper(peopleWrapper, patientQueryList);
        mockServerResponse(peopleWrapper, OpenEmpiPeopleWrapper.class);
        OpenEmpiPatientQuery query = createPatientQuery();

        List<OpenEmpiPatientQuery> patients = restQueryCreator.findPatients(TOKEN_VALUE, query);

        ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        //verify correct request creation:
        verify(restTemplate).exchange(eq(createFindPreciseSimilarPatient()),
                eq(HttpMethod.POST), argumentCaptor.capture(), eq(OpenEmpiPeopleWrapper.class));
        //verify request body was set correctly:
        assertEquals(argumentCaptor.getValue().getBody(), query);
        //verify that header with token was sett:
        assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));
        assertEquals(patients, patientQueryList);
    }

    @Test
    public void testFindPatientsReturnEmptyListOnNullPatient() throws Exception {
        mockPeopleWrapper(peopleWrapper, null);
        mockServerResponse(peopleWrapper, OpenEmpiPeopleWrapper.class);
        OpenEmpiPatientQuery query = createPatientQuery();

        List<OpenEmpiPatientQuery> patients = restQueryCreator.findPatients(TOKEN_VALUE, query);

        assertTrue(patients.size() == 0);
    }

    @Test
    public void testExportPatient() throws Exception {
        mockServerResponse(patientQuery, OpenEmpiPatientQuery.class);
        OpenEmpiPatientQuery query = createPatientQuery();

        restQueryCreator.exportPatient(TOKEN_VALUE, query);

        ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        //verify correct request creation:
        verify(restTemplate).exchange(eq(createExportPatient()),
                eq(HttpMethod.PUT), argumentCaptor.capture(), eq(OpenEmpiPatientQuery.class));
        //verify request body was set correctly:
        assertEquals(argumentCaptor.getValue().getBody(), query);
        //verify that header with token was sett:
        assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));

    }

    private OpenEmpiPatientQuery createPatientQuery() {
        OpenEmpiPatientQuery query = new OpenEmpiPatientQuery();
        query.setGivenName(PATIENT_QUERY_GIVENNAME);
        query.setFamilyName(PATIENT_QUERY_FAMILYNAME);
        return query;
    }

    private void mockServerResponse(Object body, Class<?> value) {
        ResponseEntity response = mock(ResponseEntity.class);
        when(response.getBody()).thenReturn(body);
        when(restTemplate.exchange(anyString(),
                any(HttpMethod.class), any(HttpEntity.class), eq(value)))
                .thenReturn(response);
    }

    private MpiCredentials createCredentials() {
        return new MpiCredentials(USERNAME, PASSWORD);
    }

    private String createAuthenticationUrl() {
        return getServerUrl() + REST_URL + AUTHENTICATION_URL;
    }

    private String createGetPatientUrl(String id) {
        String url = getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PATIENT_BY_ID_URL;
        url += "?personId=" + id;
        return url;
    }

    private String createFindPreciseSimilarPatient() {
        return getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_EXACT_PATIENTS_URL;
    }

    private String createExportPatient() {
        return getServerUrl() + REST_URL + PERSON_MANAGER_URL + IMPORT_PERSON_URL;
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    private String getServerUrl() {
        return "http://" + SERVER_URL;
    }

    private void mockRestTemplate() {
        ReflectionTestUtils.setField(restQueryCreator, "restTemplate", restTemplate);
    }

    private void mockServerUrlProperty() {
        when(adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_URL)).thenReturn(SERVER_URL);
    }

    private void mockPeopleWrapper(OpenEmpiPeopleWrapper peopleWrapper, List<OpenEmpiPatientQuery> patientQueryList) {
        when(peopleWrapper.getPeople()).thenReturn(patientQueryList);
    }
}