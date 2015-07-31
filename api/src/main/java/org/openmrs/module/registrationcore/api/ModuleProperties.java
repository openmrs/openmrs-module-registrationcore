package org.openmrs.module.registrationcore.api;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class ModuleProperties {

    @Autowired
    @Qualifier("adminService")
    protected AdministrationService administrationService;

    protected String readProperty(String propertyName) {
        String propertyValue = administrationService.getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue))
            throw new APIException("Property value for '" + propertyName + "' is not set");
        return propertyValue;
    }
}
