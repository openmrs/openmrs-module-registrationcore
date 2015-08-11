package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiVariables.*;

public class RestQueryCreator {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties properties;

    public String processAuthentication(MpiCredentials credentials) throws APIException {
        ResponseEntity<String> token = restTemplate.exchange(createAuthenticationUrl(),
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

    public void exportPatient(String token, OpenEmpiPatientQuery patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(patientQuery, headers);

        ResponseEntity<OpenEmpiPatientQuery> personResponse = restTemplate.exchange(createExportPatient(),
                HttpMethod.PUT, entity, OpenEmpiPatientQuery.class);

        if (personResponse.getBody() == null)
            throw new APIException("Fail while Patient export to Mpi server. " +
                    "Check if patient with same identifiers do not exist on MPI server.");
    }

    public void updatePatient(String token, OpenEmpiPatientQuery patientQuery) {
        HttpHeaders headers = getAuthenticationHeader(token);

        HttpEntity<OpenEmpiPatientQuery> entity = new HttpEntity<OpenEmpiPatientQuery>(patientQuery, headers);

        ResponseEntity<OpenEmpiPatientQuery> personResponse = restTemplate.exchange(createUpdatePatient(),
                HttpMethod.PUT, entity, OpenEmpiPatientQuery.class);

        if (personResponse.getBody() == null)
            throw new APIException("Fail while Patient export to Mpi server. " +
                    "Check if patient with same identifiers do not exist on MPI server.");
    }

    private HttpHeaders getAuthenticationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, token);
        return headers;
    }

    //TODO move URL creation in separate class.
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

    private String createUpdatePatient() {
        return getServerUrl() + REST_URL + PERSON_MANAGER_URL + UPDATE_PERSON_URL;
    }

    private String getServerUrl() {
        return "http://" + properties.getServerUrl();
    }

    private List<OpenEmpiPatientQuery> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people.getPeople() == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }
}