package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class RestQueryCreator {

    public String processAuthentication(String username, String password) throws RuntimeException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> token = restTemplate
                .exchange(OpenEmpiVariables.getAuthenticationUrl(),
                        HttpMethod.PUT, new HttpEntity<MpiCredentials>(new MpiCredentials(username, password)), String.class);
        String tokenValue = token.getBody();
        if (tokenValue != null) {
            return tokenValue;
        } else {
            throw new RuntimeException("Can't perform authentication");
        }
    }

    public OpenEmpiPatientQuery getPatientById(String token, String id) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String findPatientByIdUrl = createGetPatientUrl(id);


        ResponseEntity<OpenEmpiPatientQuery> person = restTemplate.exchange(findPatientByIdUrl, HttpMethod.GET,
                entity, OpenEmpiPatientQuery.class);

        return person.getBody();
    }

    public List<OpenEmpiPatientQuery> findPatients(String token, OpenEmpiPatientQuery query) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(query, headers);

        ResponseEntity<OpenEmpiPeopleWrapper> people = restTemplate.exchange(OpenEmpiVariables.getExactPatientsUrl(), HttpMethod.POST, entity, OpenEmpiPeopleWrapper.class);

        return unwrapResult(people.getBody());
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    private String createGetPatientUrl(String id) {
        String url = OpenEmpiVariables.getFindPatientByIdUrl();
        url += "?personId=";
        url += id;
        return url;
    }

    private List<OpenEmpiPatientQuery> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people.getPeople() == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }
}