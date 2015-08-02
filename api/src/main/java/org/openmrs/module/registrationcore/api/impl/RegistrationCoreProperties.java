package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;

public class RegistrationCoreProperties extends ModuleProperties {
    //TODO move getting properties related to Registration Core in this class.

    public Integer getIdentifierSourceId() {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID;
        return getIntegerProperty(propertyName);
    }
}
