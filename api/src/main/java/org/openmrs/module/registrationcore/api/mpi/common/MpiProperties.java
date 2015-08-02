package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;

public class MpiProperties extends ModuleProperties {

    public String getServerUrl() {
        String propertyName = RegistrationCoreConstants.GP_MPI_URL;
        return getProperty(propertyName);
    }

    public Integer getGlobalIdentifierDomainId() {
        String propertyName = RegistrationCoreConstants.GP_MPI_GLOBAL_IDENTIFIER_DOMAIN_ID;
        return getIntegerProperty(propertyName);
    }

    public MpiCredentials getMpiCredentials() {
        String usernamePropertyName = RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME;
        String username = getProperty(usernamePropertyName);

        String passwordPropertyName = RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD;
        String password = getProperty(passwordPropertyName);

        return new MpiCredentials(username, password);
    }
}
