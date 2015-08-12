package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.module.registrationcore.api.mpi.common.MpiFacade;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RegistrationCoreProperties extends ModuleProperties implements ApplicationContextAware {
    //TODO move getting properties related to Registration Core in this class.

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Integer getIdentifierSourceId() {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID;
        return getIntegerProperty(propertyName);
    }

    public boolean isMpiEnabled() {
        return getMpiFacade() != null;
    }

    public MpiFacade getMpiFacade() {
        String propertyName = RegistrationCoreConstants.GP_MPI_FACADE;
        String beanId = null;

        try {
            beanId = getProperty(propertyName);
        } catch (APIException e) {
            return null;
        }

        Object bean = applicationContext.getBean(beanId);
        if (!(bean instanceof MpiFacade))
            throw new IllegalArgumentException(propertyName
                    + " must point to bean implementing MpiFacade");

        return (MpiFacade) bean;
    }
}
