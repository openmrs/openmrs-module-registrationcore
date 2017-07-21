package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains properties related to MPI.
 */
public class MpiProperties extends ModuleProperties {

    /**
     * @return MPI server base url
     */
    public String getServerUrl() {
        String propertyName = RegistrationCoreConstants.GP_MPI_URL;
        return getProperty(propertyName);
    }

    public Integer getGlobalIdentifierDomainId() {
        String propertyName = RegistrationCoreConstants.GP_MPI_GLOBAL_IDENTIFIER_DOMAIN_ID;
        return getIntegerProperty(propertyName);
    }

    public Integer getMpiPersonIdentifierTypeId() {
        String propertyName = RegistrationCoreConstants.GP_MPI_PERSON_IDENTIFIER_ID;
        return getIntegerProperty(propertyName);
    }

    public MpiCredentials getMpiCredentials() {
        String usernamePropertyName = RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME;
        String username = getProperty(usernamePropertyName);

        String passwordPropertyName = RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD;
        String password = getProperty(passwordPropertyName);

        return new MpiCredentials(username, password);
    }

    public List<String> getLocalMpiIdentifierTypeMap() {
        String propertyPrefix = RegistrationCoreConstants.GP_LOCAL_MPI_IDENTIFIER_TYPE_MAP;

        List<String> result = new LinkedList<String>();
        for (GlobalProperty property : administrationService.getGlobalPropertiesByPrefix(propertyPrefix)) {
            result.add(property.getPropertyValue());
        }
        return result;
    }

    public boolean isProbabilisticMatchingEnabled() {
        String propertyName = RegistrationCoreConstants.GP_PROBABLY_MATCH_ENABLED;
        try {
            String isEnabled = getProperty(propertyName);
            return Boolean.getBoolean(isEnabled);
        } catch (APIException e) {
            //If exception is thrown if property is missed, so by default return false;
            return false;
        }
    }

    public List<String> getPixIdentifierUuidList() {
        String uuidListPropertyName = RegistrationCoreConstants.GP_MPI_PIX_IDENTTIFIER_UUID_LIST;
        String uuidList = getProperty(uuidListPropertyName);
        List<String> list = new ArrayList<String>();
        for (String uuid : uuidList.split(",")) {
            list.add(uuid.trim());
        }
        return list;
    }

    public List<String> getPdqIdentifierUuidList() {
        String uuidListPropertyName = RegistrationCoreConstants.GP_MPI_PDQ_IDENTTIFIER_UUID_LIST;
        String uuidList = getProperty(uuidListPropertyName);
        List<String> list = new ArrayList<String>();
        for (String uuid : uuidList.split(",")) {
            list.add(uuid.trim());
        }
        return list;
    }
}
