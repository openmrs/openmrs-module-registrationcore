package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class RestQueryExecutor {

    @Autowired
    @Qualifier("registrationcore.restUrlBuilder")
    private RestUrlBuilder urlBuilder;

    private RestTemplate restTemplate = new RestTemplate();

    public String processAuthentication(MpiCredentials credentials) throws APIException {
        String url = urlBuilder.createAuthenticationUrl();
        ResponseEntity<String> token = restTemplate.exchange(url,
                HttpMethod.PUT, new HttpEntity<MpiCredentials>(credentials), String.class);
        String tokenValue = token.getBody();

        if (tokenValue != null) {
            return tokenValue;
        } else {
            throw new APIException("Can't perform authentication to MPI server.");
        }
    }

    public OpenEmpiPatientResult getPatientById(String token, String id) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String url = urlBuilder.createGetPatientUrl(id);
        ResponseEntity<OpenEmpiPatientResult> person = restTemplate.exchange(url,
                HttpMethod.GET, entity, OpenEmpiPatientResult.class);

        return person.getBody();
    }

    public List<OpenEmpiPatientResult> findPreciseSimilarPatients(String token, OpenEmpiPatientResult query) {
        String url = urlBuilder.createFindPreciseSimilarPatientsUrl();
        return findPatients(token, url, query);
    }

    public List<OpenEmpiPatientResult> findProbablySimilarPatients(String token, OpenEmpiPatientResult query) {
        String url = urlBuilder.createFindProbablySimilarPatientsUrl();
        return findPatients(token, url, query);
    }

    public OpenEmpiPatientResult exportPatient(String token, OpenEmpiPatientResult patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<OpenEmpiPatientResult> entity = new HttpEntity<OpenEmpiPatientResult>(patientQuery, headers);

        String url = urlBuilder.createExportPatientUrl();
        ResponseEntity<OpenEmpiPatientResult> personResponse = restTemplate.exchange(url,
                HttpMethod.PUT, entity, OpenEmpiPatientResult.class);

        if (personResponse.getBody() == null)
            throw new APIException("Fail while Patient export to Mpi server. " +
                    "Check if patient with same identifiers do not exist on MPI server.");
        return personResponse.getBody();
    }

    public void updatePatient(String token, OpenEmpiPatientResult patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientResult> entity = new HttpEntity<OpenEmpiPatientResult>(patientQuery, headers);

        String url = urlBuilder.createUpdatePatientUrl();
        ResponseEntity<String> response = restTemplate.exchange(url,
                HttpMethod.PUT, entity, String.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT)
            throw new APIException("Fail while Patient update on Mpi server. " +
                    "Check if patient with same identifiers exist in  MPI server.");
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    private List<OpenEmpiPatientResult> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people.getPeople() == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }

    private List<OpenEmpiPatientResult> findPatients(String token, String url, OpenEmpiPatientResult query) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<OpenEmpiPatientResult> entity = new HttpEntity<OpenEmpiPatientResult>(query, headers);

        ResponseEntity<OpenEmpiPeopleWrapper> people = restTemplate.exchange(url,
                HttpMethod.POST, entity, OpenEmpiPeopleWrapper.class);

        return unwrapResult(people.getBody());
    }
}