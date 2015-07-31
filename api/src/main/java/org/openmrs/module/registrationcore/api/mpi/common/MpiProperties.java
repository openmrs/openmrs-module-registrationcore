package org.openmrs.module.registrationcore.api.mpi.common;

import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class MpiProperties extends ModuleProperties {

    private static final Map<String, String> MPI_PROPERTIES = new LinkedHashMap<String, String>();

    public String getServerUrl() {
        String propertyName = RegistrationCoreConstants.GP_MPI_URL;
        return getProperty(propertyName);
    }

//    private Integer getIntegerProperty(String propertyName) {
//        String propertyValue = getProperty(propertyName);
//        try {
//            return Integer.valueOf(propertyValue);
//        } catch (NumberFormatException e) {
//            throw new APIException("Incorrect property value. " + propertyValue + " cannot be cast to Integer");
//        }
//    }

    private String getProperty(String propertyName) {
        if (MPI_PROPERTIES.get(propertyName) == null) {
            initializeProperty(propertyName);
        }
        return MPI_PROPERTIES.get(propertyName);
    }

    private void initializeProperty(String propertyName) {
        String propertyValue = readProperty(propertyName);
        MPI_PROPERTIES.put(propertyName, propertyValue);
    }
}
