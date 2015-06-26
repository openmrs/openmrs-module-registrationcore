package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.MpiCredentials;
import org.openmrs.module.registrationcore.api.mpi.MpiSimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import java.util.*;

@Service("registrationcore.OpenEmpiExactPatientSearchAlgorithm")
public class OpenEmpiExactPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    String token = "F58C465C3045B63D7542F2D3BE9BA9DF";
    private MpiCredentials credentials;

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        //TODO mocking. Should be removed.
        credentials = new MpiCredentials("admin", "admin");

        List<PatientAndMatchQuality> result;
        try {
            if (!isAuthenticated()) {
                processAuthentication();
            }
            List<OpenEmpiPatientQuery> mpiSimilarPatients = getMpiSimilarPatients(patient);
            subtractMpiPatients(mpiSimilarPatients, maxResults);
            result = convertMpiPatients(mpiSimilarPatients, cutoff);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            result = Collections.emptyList();
        }
        return result;
    }

    private List<PatientAndMatchQuality> convertMpiPatients(List<OpenEmpiPatientQuery> mpiPatients,
                                                            Double cutoff) {
        List<String> matchedFields = Arrays.asList("personName", "gender", "birthdate");
        List<PatientAndMatchQuality> result = new LinkedList<PatientAndMatchQuality>();
        for (OpenEmpiPatientQuery mpiPatient : mpiPatients) {
            Patient convertedPatient = PatientQueryMapper.convert(mpiPatient);
            PatientAndMatchQuality resultPatient =
                    new PatientAndMatchQuality(convertedPatient, cutoff, matchedFields);
            result.add(resultPatient);
        }
        return result;
    }

    private boolean isAuthenticated() {
        return credentials.getToken() != null;
    }

    private void subtractMpiPatients(List<OpenEmpiPatientQuery> mpiSimilarPatients, Integer maxResults) {
        if (mpiSimilarPatients.size() > maxResults) {
            mpiSimilarPatients = mpiSimilarPatients.subList(0, maxResults);
        }
    }

    private void processAuthentication() throws AuthenticationException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> token = restTemplate
                .exchange(OpenEmpiVariables.getAuthenticationUrl(),
                        HttpMethod.PUT, new HttpEntity<MpiCredentials>(credentials), String.class);
        String tokenValue = token.getBody();
        if (tokenValue != null) {
            credentials.setToken(tokenValue);
        } else {
            throw new AuthenticationException("Can't perform authentication");
        }
    }

    private List<OpenEmpiPatientQuery> getMpiSimilarPatients(Patient patient) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<OpenEmpiPatientQuery> request = getHttpEntityRequest(restTemplate, patient);

        OpenEmpiPeopleWrapper people = restTemplate.postForObject(OpenEmpiVariables.getFindPatientsUrl(),
                request, OpenEmpiPeopleWrapper.class);

        return people.getPeople();
    }

    private HttpEntity<OpenEmpiPatientQuery> getHttpEntityRequest(RestTemplate restTemplate, Patient patient) {
        MultiValueMap<String, String> headers = getAuthenticationHeader(restTemplate);
        OpenEmpiPatientQuery query = getPatientSimilarQuery(patient);

        return new HttpEntity<OpenEmpiPatientQuery>(query, headers);
    }

    private MultiValueMap<String, String> getAuthenticationHeader(RestTemplate restTemplate) {
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add(OpenEmpiVariables.TOKEN_HEADER_KEY, credentials.getToken());

        return headers;
    }

    private OpenEmpiPatientQuery getPatientSimilarQuery(Patient patient) {
        return PatientQueryMapper.convert(patient);
    }
}
