package org.openmrs.module.registrationcore.api.mpi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * This class contains properties related to MPI.
 */
public class MpiProperties extends ModuleProperties {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

    public String getMpiPersonIdentifierTypeUuid() {
        String propertyName = RegistrationCoreConstants.GP_MPI_PERSON_IDENTIFIER_TYPE_UUID;
        return getProperty(propertyName);
    }

    public MpiCredentials getMpiCredentials() {
        Properties props = OpenmrsUtil.getRuntimeProperties(null);
        String username = props.getProperty(RegistrationCoreConstants.RTP_MPI_ACCESS_USERNAME);
        String password = props.getProperty(RegistrationCoreConstants.RTP_MPI_ACCESS_PASSWORD);
        if (StringUtils.isBlank(username)) {
            log.error(
                    "MPI Username is not defined in .OpenMRS/openmrs-runtime.properties file. Unable to authenticate.");
        }
        if (StringUtils.isBlank(password)) {
            log.error(
                    "MPI Username is not defined in .OpenMRS/openmrs-runtime.properties file. Unable to authenticate.");
        }
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
            // If exception is thrown if property is missed, so by default return false;
            return false;
        }
    }

    public List<String> getPixIdentifierTypeUuidList() {
        return splitIntoList(RegistrationCoreConstants.GP_MPI_PIX_IDENTTIFIER_TYPE_UUID_LIST);
    }

    private List<String> splitIntoList(String property) {
        String uuidList = getProperty(property);
        List<String> list = new ArrayList<String>();
        for (String uuid : uuidList.split(",")) {
            list.add(uuid.trim());
        }
        return list;
    }
}
