package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class RestQueryCreator {

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

    public OpenEmpiPatientQuery getPatientById(String token, String id) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        String url = urlBuilder.createGetPatientUrl(id);
        ResponseEntity<OpenEmpiPatientQuery> person = restTemplate.exchange(url,
                HttpMethod.GET, entity, OpenEmpiPatientQuery.class);

        return person.getBody();
    }

    public List<OpenEmpiPatientQuery> findPatients(String token, OpenEmpiPatientQuery query) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(query, headers);

        String url = urlBuilder.createFindPreciseSimilarPatientUrl();
        ResponseEntity<OpenEmpiPeopleWrapper> people = restTemplate.exchange(url,
                HttpMethod.POST, entity, OpenEmpiPeopleWrapper.class);

        return unwrapResult(people.getBody());
    }

    public void exportPatient(String token, OpenEmpiPatientQuery patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);
        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(patientQuery, headers);

        String url = urlBuilder.createExportPatientUrl();
        ResponseEntity<OpenEmpiPatientQuery> personResponse = restTemplate.exchange(url,
                HttpMethod.PUT, entity, OpenEmpiPatientQuery.class);

        if (personResponse.getBody() == null)
            throw new APIException("Fail while Patient export to Mpi server. " +
                    "Check if patient with same identifiers do not exist on MPI server.");
    }

    public void updatePatient(String token, OpenEmpiPatientQuery patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(patientQuery, headers);

        String url = urlBuilder.createUpdatePatientUrl();
        ResponseEntity<OpenEmpiPatientQuery> personResponse = restTemplate.exchange(url,
                HttpMethod.PUT, entity, OpenEmpiPatientQuery.class);

        //TODO check it:
        if (personResponse.getBody() == null)
            throw new APIException("Fail while Patient export to Mpi server. " +
                    "Check if patient with same identifiers do not exist on MPI server.");
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    private List<OpenEmpiPatientQuery> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people.getPeople() == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }
}