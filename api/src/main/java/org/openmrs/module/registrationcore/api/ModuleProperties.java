package org.openmrs.module.registrationcore.api;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ModuleProperties {

    private static final Map<String, String> PROPERTIES = new LinkedHashMap<String, String>();

    @Autowired
    @Qualifier("adminService")
    protected AdministrationService administrationService;

    protected Integer getIntegerProperty(String propertyName) {
        String propertyValue = getProperty(propertyName);
        try {
            return Integer.valueOf(propertyValue);
        } catch (NumberFormatException e) {
            throw new APIException("Incorrect property value. " + propertyValue + " cannot be cast to Integer");
        }
    }

    protected String getProperty(String propertyName) {
        if (PROPERTIES.get(propertyName) == null) {
            initializeProperty(propertyName);
        }
        return PROPERTIES.get(propertyName);
    }

    private void initializeProperty(String propertyName) {
        String propertyValue = readProperty(propertyName);
        PROPERTIES.put(propertyName, propertyValue);
    }

    private String readProperty(String propertyName) {
        String propertyValue = administrationService.getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue))
            throw new APIException("Property value for '" + propertyName + "' is not set");
        return propertyValue;
    }
}
