package org.openmrs.module.registrationcore.api.mpi.openempi;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.MpiCredentials;
import org.openmrs.module.registrationcore.api.mpi.MpiSimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.*;

@Service("registrationcore.OpenEmpiExactPatientSearchAlgorithm")
public class OpenEmpiExactPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    private MpiCredentials credentials;

    private Client client = Client.create(new DefaultClientConfig());

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        this.setCredentials(new MpiCredentials("admin", "admin"));
        if (credentials.getToken() == null) {
            processAuthentication();
        }
        return findPatients(patient, cutoff, maxResults);
    }

    private void processAuthentication() {
        WebResource service = client.resource(
                UriBuilder.fromUri(OpenEmpiVariables.getAuthenticationUrl()).build());

        String token = service.put(String.class, credentials);
        credentials.setToken(token);
    }

    private List<PatientAndMatchQuality> findPatients(Patient patient, Double cutoff, Integer maxResults) {
        OpenEmpiPatientQuery patientQuery = getQuery(patient);

        WebResource.Builder service = getRestBuilder();

        List<OpenEmpiPatientQuery> mpiPatients = getMatchedPatients(patientQuery, service, maxResults);

        return convertMatchedPatients(mpiPatients, cutoff);
    }

    private OpenEmpiPatientQuery getQuery(Patient patient) {
        return PatientQueryMapper.convert(patient);
    }

    private WebResource.Builder getRestBuilder() {
        return client.resource(UriBuilder.fromUri(OpenEmpiVariables.getFindPatientsUrl()).build())
                .header(OpenEmpiVariables.TOKEN_HEADER_KEY, credentials.getToken());
    }

    private List<OpenEmpiPatientQuery> getMatchedPatients(OpenEmpiPatientQuery patientQuery,
                                                          WebResource.Builder service, int maxResults) {
        return service.post(new GenericType<List<OpenEmpiPatientQuery>>() {}, patientQuery);
    }

    private List<PatientAndMatchQuality> convertMatchedPatients(List<OpenEmpiPatientQuery> mpiPatients,
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

    public void setCredentials(MpiCredentials credentials) {
        this.credentials = credentials;
    }
}
