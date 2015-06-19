package org.openmrs.module.registrationcore.api.mpi;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;

@Service("registrationcore.OpenEmpiPatientSearchAlgorithm")
public class OpenEmpiPatientSearchAlgorithm implements MpiSimilarPatientSearchAlgorithm {

    private MpiCredentials credentials;

    private String SERVER_URL = "http://178.62.213.106:8080";
    private String AUTHENTICATION_URL = "/openempi-admin/openempi-ws-rest/security-resource/authenticate";

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient,
                                                            Map<String, Object> otherDataPoints,
                                                            Double cutoff, Integer maxResults) {
        if (credentials.getToken() == null) {
            processAuthentication();
        }
        return null;
    }


    private void processAuthentication() {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(UriBuilder.fromUri(SERVER_URL + AUTHENTICATION_URL).build());

        String token = service.put(String.class, credentials);
        credentials.setToken(token);
    }

    public void setCredentials(MpiCredentials credentials) {
        this.credentials = credentials;
    }
}
