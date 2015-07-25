package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiAuthenticator;
import org.openmrs.module.registrationcore.api.mpi.common.MpiCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OpenEmpiAuthenticator implements MpiAuthenticator {

    @Autowired
    @Qualifier("registrationcore.restQueryCreator")
    private RestQueryCreator queryCreator;
    @Autowired
    @Qualifier("adminService")
    private AdministrationService adminService;
    private String token;

    @Override
    public boolean isAuthenticated() {
        return token != null;
    }

    @Override
    public void performAuthentication() {
        try {
            token = queryCreator.processAuthentication(getMpiCredentials());
        } catch (RuntimeException e) {
            throw new APIException("Exception while processing authentication to MPI server.");
        }
    }

    @Override
    public String getToken() {
        if (token == null) {
            performAuthentication();
        }
        return token;
    }

    private MpiCredentials getMpiCredentials() {
        String username = adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME);
        String password = adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new APIException("Incorrect credentials are set for MPI server." +
                    "(Username: \"" + username + "\", Password: \"" + password + "\")");
        }

        return new MpiCredentials(username, password);
    }
}
