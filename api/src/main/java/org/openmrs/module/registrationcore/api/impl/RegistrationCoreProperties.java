package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.module.registrationcore.api.MpiErrorHandlingService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
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

    public boolean isBiometricsEngineEnabled() {
        return getBiometricEngine() != null;
    }

    /**
     * @return the id of the Biometric Engine that is configured
     */
    public BiometricEngine getBiometricEngine() {
        String propertyName = RegistrationCoreConstants.GP_BIOMETRICS_IMPLEMENTATION;
        String engineId = administrationService.getGlobalProperty(propertyName);
        BiometricEngine engine = null;
        if (StringUtils.isNotBlank(engineId)) {
            log.debug("Looking up biometrics engine component: " + engineId);
            engine = Context.getRegisteredComponent(engineId, BiometricEngine.class);
            if (engine == null) {
                throw new IllegalStateException(propertyName + " must point to a bean implementing BiometricEngine. " + engineId + " is not valid.");
            }
        }
        return engine;
    }

    public MpiErrorHandlingService getMpiErrorHandlingService() {
        String propertyName = RegistrationCoreConstants.GP_ERROR_HANDLER_IMPLEMENTATION;
        String handlerId = Context.getAdministrationService().getGlobalProperty(propertyName);
        MpiErrorHandlingService handler = null;
        if (StringUtils.isNotBlank(handlerId)) {
            log.debug("Looking up biometrics engine component: " + handlerId);
            handler = Context.getRegisteredComponent(handlerId, MpiErrorHandlingService.class);
            if (handler == null) {
                throw new IllegalStateException(propertyName + " must point to a bean implementing BiometricEngine. " + handlerId + " is not valid.");
            }
        }
        return handler;
    }

    /**
     * Get the PDQ endpoint
     * @return
     */
    public String getPdqEndpoint() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_PDQ_ENDPOINT, "localhost");
    }

    /**
     * Get the PDQ port
     * @return
     */
    public Integer getPdqPort() {
        return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_PDQ_PORT, "8989"));
    }

    public String getPixEndpoint() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_PIX_ENDPOINT, "localhost");
    }

    public Integer getPixPort() {
        return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_PIX_PORT, "3600"));
    }

    public String getMpiUsername() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_ACCESS_USERNAME, "admin");
    }

    public String getMpiPassword() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_ACCESS_PASSWORD, "admin");
    }

    public String getShrPatientRoot() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_PATIENT_ROOT, "1.2.3.4.5.9");
    }

    public String getReceivingApplication() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_RECEIVING_APPLICATION, "CR");
    }

    public String getReceivingFacility() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_RECEIVING_FACILITY, "Clinic");
    }

    public String getSendingApplication() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_SENDING_APPLICATION, "OPENMRS");
    }

    public String getSendingFacility() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_SENDING_FACILITY, "LOCATION");
    }

    public String getUniversalIdType() {
        return Context.getAdministrationService().getGlobalProperty(
                RegistrationCoreConstants.GP_MPI_UNI_ID_TYPE, "PI");
    }
}
