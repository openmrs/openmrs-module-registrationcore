package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

public class RestQueryCreator {

    public List<OpenEmpiPatientQuery> getPatients(MpiCredentials credentials, OpenEmpiPatientQuery query) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<OpenEmpiPatientQuery> request = getHttpEntityRequest(restTemplate, query, credentials);

        OpenEmpiPeopleWrapper people = performPost(restTemplate, request);

        return unwrapResult(people);
    }

    private HttpEntity<OpenEmpiPatientQuery> getHttpEntityRequest(RestTemplate restTemplate,
                                                                  OpenEmpiPatientQuery query,
                                                                  MpiCredentials credentials) {
        MultiValueMap<String, String> headers = getAuthenticationHeader(restTemplate, credentials);

        return new HttpEntity<OpenEmpiPatientQuery>(query, headers);
    }

    private MultiValueMap<String, String> getAuthenticationHeader(RestTemplate restTemplate,
                                                                  MpiCredentials credentials) {
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, credentials.getToken());

        return headers;
    }

    private OpenEmpiPeopleWrapper performPost(RestTemplate restTemplate, HttpEntity<OpenEmpiPatientQuery> request) {
        return restTemplate.postForObject(OpenEmpiVariables.getExactPatientsUrl(),
                request, OpenEmpiPeopleWrapper.class);
    }

    private List<OpenEmpiPatientQuery> unwrapResult(OpenEmpiPeopleWrapper people) {
        if (people == null) {
            return Collections.emptyList();
        } else {
            return people.getPeople();
        }
    }

}
