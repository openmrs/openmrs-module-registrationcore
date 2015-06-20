package org.openmrs.module.registrationcore.api.mpi.openempi;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.MpiCredentials;
import org.openmrs.module.registrationcore.api.mpi.MpiSimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;

@Service("registrationcore.OpenEmpiPatientSearchAlgorithm")
public class OpenEmpiPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    private MpiCredentials credentials;


    private Client client = Client.create(new DefaultClientConfig());

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        if (credentials.getToken() == null) {
            processAuthentication();
        }
        findPatients(patient);
        return null;
    }

    private void processAuthentication() {
        WebResource service = client.resource(
                UriBuilder.fromUri(OpenEmpiVariables.getAuthenticationUrl()).build());

        String token = service.put(String.class, credentials);
        credentials.setToken(token);
    }

    private void findPatients(Patient patient) {
        WebResource.Builder service = client.resource(
                UriBuilder.fromUri(OpenEmpiVariables.getFindPatientsUrl()).build())
                .header(OpenEmpiVariables.TOKEN_HEADER_KEY, credentials.getToken());

        OpenEmpiPatientQuery patientQuery = PatientToQueryMapper.convert(patient);

        String result = service.post(String.class, patientQuery);
        //TODO return matched persons.
    }

    public void setCredentials(MpiCredentials credentials) {
        this.credentials = credentials;
    }
}
