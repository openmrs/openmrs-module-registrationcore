package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiVariables.*;

public class RestQueryCreator {

    private AdministrationService adminService;
    private RestTemplate restTemplate = new RestTemplate();
    private String serverUrl;

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public String processAuthentication(MpiCredentials credentials) throws RuntimeException {
        ResponseEntity<String> token = restTemplate.exchange(createAuthenticationUrl(),
                HttpMethod.PUT, new HttpEntity<MpiCredentials>(credentials), String.class);
        String tokenValue = token.getBody();
        if (tokenValue != null) {
            return tokenValue;
        } else {
            throw new RuntimeException("Can't perform authentication");
        }
    }

    public OpenEmpiPatientQuery getPatientById(String token, String id) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<OpenEmpiPatientQuery> person = restTemplate.exchange(createGetPatientUrl(id),
                HttpMethod.GET, entity, OpenEmpiPatientQuery.class);

        return person.getBody();
    }

    public List<OpenEmpiPatientQuery> findPatients(String token, OpenEmpiPatientQuery query) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(query, headers);

        ResponseEntity<OpenEmpiPeopleWrapper> people = restTemplate.exchange(createFindPreciseSimilarPatient(),
                HttpMethod.POST, entity, OpenEmpiPeopleWrapper.class);

        return unwrapResult(people.getBody());
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    private String createAuthenticationUrl() {
        return getServerUrl() + REST_URL + AUTHENTICATION_URL;
    }

    private String createGetPatientUrl(String id) {
        String url = getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PATIENT_BY_ID_URL;
        url += "?personId" + id;
        return url;
    }

    private String createFindPreciseSimilarPatient() {
        return getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_EXACT_PATIENTS_URL;
    }

    private String getServerUrl() {
        if (serverUrl == null) {
            serverUrl = adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_URL);
            validateServerUrl();
            serverUrl = "http://" + serverUrl;
        }
        return serverUrl;
    }

    private void validateServerUrl() {
        if (StringUtils.isBlank(serverUrl))
            throw new APIException("Mpi server credentials are not set. Please fill "
                    + RegistrationCoreConstants.GP_MPI_URL + " property");
    }

    private List<OpenEmpiPatientQuery> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people.getPeople() == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }
}