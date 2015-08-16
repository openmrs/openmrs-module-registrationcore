package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.openmrs.module.registrationcore.api.mpi.openempi.OpenEmpiVariables.*;

public class RestUrlBuilder {

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties properties;

    public String createAuthenticationUrl() {
        return getServerUrl() + REST_URL + AUTHENTICATION_URL;
    }

    public String createGetPatientUrl(String id) {
        String url = getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_PATIENT_BY_ID_URL;
        url += "?personId=" + id;
        return url;
    }

    public String createFindPreciseSimilarPatientsUrl() {
        return getServerUrl() + REST_URL + PERSON_QUERY_URL + FIND_EXACT_PATIENTS_URL;
    }

    public String createExportPatientUrl() {
        return getServerUrl() + REST_URL + PERSON_MANAGER_URL + IMPORT_PERSON_URL;
    }

    public String createUpdatePatientUrl() {
        return getServerUrl() + REST_URL + PERSON_MANAGER_URL + UPDATE_PERSON_URL;
    }

    private String getServerUrl() {
        return "http://" + properties.getServerUrl();
    }
}
