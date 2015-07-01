package org.openmrs.module.registrationcore.api.mpi.openempi;

public class OpenEmpiVariables {

    static final String TOKEN_HEADER_KEY = "OPENEMPI_SESSION_KEY";

    //TODO move to property
    private static final String SERVER_URL = "http://188.166.56.70:8080/";

    private static final String REST_URL = "openempi-admin/openempi-ws-rest/";

    private static final String AUTHENTICATION_URL = "security-resource/authenticate";

    private static final String FIND_EXACT_PATIENTS_URL = "person-query-resource/findPersonsByAttributes";

    //TODO should be changed for Probablistic match.
    private static final String FIND_PROBABLISTIC_PATIENTS_URL = "person-query-resource/findPersonsByAttributes";

    static String getAuthenticationUrl() {
        return SERVER_URL + REST_URL + AUTHENTICATION_URL;
    }

    static String getExactPatientsUrl() {
        return SERVER_URL + REST_URL + FIND_EXACT_PATIENTS_URL;
    }

    static String getProbablisticPatientsUrl() {
        return SERVER_URL + REST_URL + FIND_PROBABLISTIC_PATIENTS_URL;
    }
}