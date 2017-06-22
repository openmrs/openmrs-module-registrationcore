package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.module.registrationcore.api.biometrics.BiometricsEngine;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RegistrationCoreProperties extends ModuleProperties implements ApplicationContextAware {
    //TODO move getting properties related to Registration Core in this class.

    protected final Log log = LogFactory.getLog(this.getClass());

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

    /**
     * @return the id of the Biometrics Engine that is configured
     */
    public BiometricsEngine getBiometricsEngine() {
        String propertyName = RegistrationCoreConstants.GP_BIOMETRICS_IMPLEMENTATION;
        String engineId = administrationService.getGlobalProperty(propertyName);
        BiometricsEngine engine = null;
        if (StringUtils.isNotBlank(engineId)) {
            log.debug("Looking up biometrics engine component: " + engineId);
            engine = Context.getRegisteredComponent(engineId, BiometricsEngine.class);
            if (engine == null) {
                throw new IllegalStateException(propertyName + " must point to a bean implementing BiometricsEngine. " + engineId + " is not valid.");
            }
        }
        return engine;
    }
}
