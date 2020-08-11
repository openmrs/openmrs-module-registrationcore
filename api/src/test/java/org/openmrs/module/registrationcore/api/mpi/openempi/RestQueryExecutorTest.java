package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RestQueryExecutorTest {
	
	private static final String PASSWORD = "password";
	
	private static final String USERNAME = "username";
	
	private static final String TOKEN_VALUE = "tokenValue";
	
	private static final String PATIENT_ID = "13";
	
	private static final String PATIENT_QUERY_GIVENNAME = "Michael";
	
	private static final String PATIENT_QUERY_FAMILYNAME = "Louis";
	
	@InjectMocks
	private RestQueryExecutor restQueryExecutor = new RestQueryExecutor();
	
	@Mock
	private RestUrlBuilder urlBuilder;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private OpenEmpiPatientResult patientQuery;
	
	@Mock
	private OpenEmpiPeopleWrapper peopleWrapper;
	
	@Mock
	private List<OpenEmpiPatientResult> patientQueryList;
	
	private ResponseEntity response;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockRestTemplate();
	}
	
	@Test
	public void testProcessAuthentication() throws Exception {
		String authUrl = "authUrl";
		when(urlBuilder.createAuthenticationUrl()).thenReturn(authUrl);
		mockServerResponse(TOKEN_VALUE, String.class);
		
		String actualToken = restQueryExecutor.processAuthentication(createCredentials());
		
		ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		//verify correct request creation:
		verify(restTemplate).exchange(eq(authUrl), eq(HttpMethod.PUT), argumentCaptor.capture(), eq(String.class));
		//verify request body was set correctly
		assertEquals(argumentCaptor.getValue().getBody(), createCredentials());
		assertEquals(actualToken, TOKEN_VALUE);
	}
	
	@Test(expected = APIException.class)
	public void testProcessAuthenticationThrowExceptionOnInvalidToken() throws Exception {
		mockServerResponse(null, String.class);
		
		restQueryExecutor.processAuthentication(createCredentials());
	}
	
	@Test
	public void testGetPatientById() throws Exception {
		String patientUrl = "get_patient_url";
		when(urlBuilder.createGetPatientUrl(PATIENT_ID)).thenReturn(patientUrl);
		mockServerResponse(patientQuery, OpenEmpiPatientResult.class);
		
		OpenEmpiPatientResult actualPatient = restQueryExecutor.getPatientById(TOKEN_VALUE, PATIENT_ID);
		
		ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		//verify correct request creation:
		verify(restTemplate).exchange(eq(patientUrl), eq(HttpMethod.GET), argumentCaptor.capture(),
		    eq(OpenEmpiPatientResult.class));
		assertNull(argumentCaptor.getValue().getBody());
		//verify that header with token was sett:
		assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));
		assertEquals(patientQuery, actualPatient);
	}
	
	@Test
	public void testFindPatients() throws Exception {
		String findPrecise = "findPrecise";
		when(urlBuilder.createFindPreciseSimilarPatientsUrl()).thenReturn(findPrecise);
		mockPeopleWrapper(peopleWrapper, patientQueryList);
		mockServerResponse(peopleWrapper, OpenEmpiPeopleWrapper.class);
		OpenEmpiPatientResult query = createPatientQuery();
		
		List<OpenEmpiPatientResult> patients = restQueryExecutor.findPreciseSimilarPatients(TOKEN_VALUE, query);
		
		ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		//verify correct request creation:
		verify(restTemplate).exchange(eq(findPrecise), eq(HttpMethod.POST), argumentCaptor.capture(),
		    eq(OpenEmpiPeopleWrapper.class));
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
		OpenEmpiPatientResult query = createPatientQuery();
		
		List<OpenEmpiPatientResult> patients = restQueryExecutor.findPreciseSimilarPatients(TOKEN_VALUE, query);
		
		assertTrue(patients.size() == 0);
	}
	
	@Test
	public void testExportPatient() throws Exception {
		String exportUrl = "exportUrl";
		when(urlBuilder.createExportPatientUrl()).thenReturn(exportUrl);
		mockServerResponse(patientQuery, OpenEmpiPatientResult.class);
		OpenEmpiPatientResult query = createPatientQuery();
		
		restQueryExecutor.exportPatient(TOKEN_VALUE, query);
		
		ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		//verify correct request creation:
		verify(restTemplate).exchange(eq(exportUrl), eq(HttpMethod.PUT), argumentCaptor.capture(),
		    eq(OpenEmpiPatientResult.class));
		//verify request body was set correctly:
		assertEquals(argumentCaptor.getValue().getBody(), query);
		//verify that header with token was sett:
		assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));
	}
	
	@Test
	public void testUpdatePatient() throws Exception {
		String updateUrl = "updateUrl";
		when(urlBuilder.createUpdatePatientUrl()).thenReturn(updateUrl);
		mockServerResponse(patientQuery, String.class);
		when(response.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
		OpenEmpiPatientResult query = createPatientQuery();
		
		restQueryExecutor.updatePatient(TOKEN_VALUE, query);
		
		ArgumentCaptor<HttpEntity> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		//verify correct request creation:
		verify(restTemplate).exchange(eq(updateUrl), eq(HttpMethod.PUT), argumentCaptor.capture(), eq(String.class));
		//verify request body was set correctly:
		assertEquals(argumentCaptor.getValue().getBody(), query);
		//verify that header with token was sett:
		assertEquals(argumentCaptor.getValue().getHeaders(), getAuthenticationHeader(TOKEN_VALUE));
	}
	
	private OpenEmpiPatientResult createPatientQuery() {
		OpenEmpiPatientResult query = new OpenEmpiPatientResult();
		query.setGivenName(PATIENT_QUERY_GIVENNAME);
		query.setFamilyName(PATIENT_QUERY_FAMILYNAME);
		return query;
	}
	
	private void mockServerResponse(Object body, Class<?> value) {
		response = mock(ResponseEntity.class);
		when(response.getBody()).thenReturn(body);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(value))).thenReturn(
		    response);
	}
	
	private MpiCredentials createCredentials() {
		return new MpiCredentials(USERNAME, PASSWORD);
	}
	
	private HttpHeaders getAuthenticationHeader(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
		return headers;
	}
	
	private void mockRestTemplate() {
		ReflectionTestUtils.setField(restQueryExecutor, "restTemplate", restTemplate);
	}
	
	private void mockPeopleWrapper(OpenEmpiPeopleWrapper peopleWrapper, List<OpenEmpiPatientResult> patientQueryList) {
		when(peopleWrapper.getPeople()).thenReturn(patientQueryList);
	}
}
