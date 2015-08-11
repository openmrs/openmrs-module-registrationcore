package org.openmrs.module.registrationcore.api.mpi.openempi;

public class OpenEmpiVariables {

    static final String TOKEN_HEADER_KEY = "OPENEMPI_SESSION_KEY";

    static final String REST_URL = "/openempi-ws-rest/";

    static final String PERSON_QUERY_URL = "person-query-resource/";

    static final String PERSON_MANAGER_URL = "person-manager-resource/";

    static final String AUTHENTICATION_URL = "security-resource/authenticate";

    static final String FIND_EXACT_PATIENTS_URL = "findPersonsByAttributes";

    //TODO should be changed for Probablistic match.
    static final String FIND_PROBABLISTIC_PATIENTS_URL = "findPersonsByAttributes";

    static final String FIND_PATIENT_BY_ID_URL = "loadPerson";

    static final String IMPORT_PERSON_URL = "importPerson";

    static final String UPDATE_PERSON_URL = "updatePerson";
}
