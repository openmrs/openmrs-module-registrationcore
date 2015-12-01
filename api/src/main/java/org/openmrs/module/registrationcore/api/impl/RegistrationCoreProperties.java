package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RegistrationCoreProperties extends ModuleProperties implements ApplicationContextAware {
    //TODO move getting properties related to Registration Core in this class.

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Integer getOpenMrsIdentifierSourceId() {
        String propertyName = RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID;
        return getIntegerProperty(propertyName);
    }

    public boolean isMpiEnabled() {
        return getMpiProvider() != null;
    }

    public MpiProvider getMpiProvider() {
        String propertyName = RegistrationCoreConstants.GP_MPI_IMPLEMENTATION;
        Object bean;
        try {
            String beanId = getProperty(propertyName);
            bean = applicationContext.getBean(beanId);
        } catch (APIException e) {
            return null;
        }
        if (!(bean instanceof MpiProvider))
            throw new IllegalArgumentException(propertyName
                    + " must point to bean implementing MpiProvider");

        return (MpiProvider) bean;
    }
}
