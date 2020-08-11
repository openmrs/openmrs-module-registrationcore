package org.openmrs.module.registrationcore.api.impl;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.ModuleProperties;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.errorhandling.PdqErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.PixErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RegistrationCoreProperties extends ModuleProperties implements ApplicationContextAware {
	
	//TODO move getting properties related to Registration Core in this class.
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * @deprecated as of 1.9.0, replaced by {@link #getSourceIdentifier()}
	 */
	@Deprecated
	public Integer getOpenMrsIdentifierSourceId() {
		String propertyName = RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID;
		return getIntegerProperty(propertyName);
	}
	
	/**
	 * @return identifier source uuid or id
	 */
	public String getSourceIdentifier() {
		String propertyName = RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID;
		return getProperty(propertyName);
	}
	
	public String getOpenMrsIdentifierUuid() {
		String propertyName = RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_UUID;
		return getProperty(propertyName);
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
		}
		catch (APIException e) {
			return null;
		}
		if (!(bean instanceof MpiProvider))
			throw new IllegalArgumentException(propertyName + " must point to bean implementing MpiProvider");
		
		return (MpiProvider) bean;
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
				throw new IllegalStateException(propertyName + " must point to a bean implementing BiometricEngine. "
				        + engineId + " is not valid.");
			}
		}
		return engine;
	}
	
	public PixErrorHandlingService getPixErrorHandlingService() {
		String propertyName = RegistrationCoreConstants.GP_MPI_PIX_ERROR_HANDLER_IMPLEMENTATION;
		PixErrorHandlingService handler = null;
		if (isPropertySet(propertyName)) {
			handler = Context.getRegisteredComponent(getProperty(propertyName), PixErrorHandlingService.class);
		}
		return handler;
	}
	
	public PdqErrorHandlingService getPdqErrorHandlingService() {
		String propertyName = RegistrationCoreConstants.GP_MPI_PDQ_ERROR_HANDLER_IMPLEMENTATION;
		PdqErrorHandlingService handler = null;
		if (isPropertySet(propertyName)) {
			handler = Context.getRegisteredComponent(getProperty(propertyName), PdqErrorHandlingService.class);
		}
		return handler;
	}
	
	/**
	 * Get the PDQ endpoint
	 * 
	 * @return
	 */
	public String getPdqEndpoint() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PDQ_ENDPOINT);
	}
	
	/**
	 * Get the PDQ port
	 * 
	 * @return
	 */
	public Integer getPdqPort() {
		return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
		    RegistrationCoreConstants.GP_MPI_PDQ_PORT));
	}
	
	public String getPixEndpoint() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_PIX_ENDPOINT);
	}
	
	public Integer getPixPort() {
		return Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
		    RegistrationCoreConstants.GP_MPI_PIX_PORT));
	}
	
	public String getMpiUsername() {
		Properties props = OpenmrsUtil.getRuntimeProperties(null);
		String username = props.getProperty(RegistrationCoreConstants.RTP_MPI_ACCESS_USERNAME);
		if (StringUtils.isBlank(username)) {
			log.error("MPI Username is not defined in .OpenMRS/openmrs-runtime.properties file. Unable to authenticate.");
		}
		return username;
	}
	
	public String getMpiPassword() {
		Properties props = OpenmrsUtil.getRuntimeProperties(null);
		String password = props.getProperty(RegistrationCoreConstants.RTP_MPI_ACCESS_PASSWORD);
		if (StringUtils.isBlank(password)) {
			log.error("MPI Username is not defined in .OpenMRS/openmrs-runtime.properties file. Unable to authenticate.");
		}
		return password;
	}
	
	public String getReceivingApplication() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_RECEIVING_APPLICATION,
		    "Demo MPI");
	}
	
	public String getReceivingFacility() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_RECEIVING_FACILITY,
		    "Demo Receiving Facility");
	}
	
	public String getSendingApplication() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_SENDING_APPLICATION,
		    "OpenMRS");
	}
	
	public String getSendingFacility() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_SENDING_FACILITY,
		    "Demo Sending Facility");
	}
	
	public String getUniversalIdType() {
		return Context.getAdministrationService().getGlobalProperty(RegistrationCoreConstants.GP_MPI_UNI_ID_TYPE, "PI");
	}
	
	private boolean isPropertySet(String globalProperty) {
		return StringUtils.isNotBlank(Context.getAdministrationService().getGlobalProperty(globalProperty));
	}
	
	public Object getBeanFromName(String propertyName) {
		Object bean;
		try {
			String beanId = Context.getAdministrationService().getGlobalProperty(propertyName);
			bean = applicationContext.getBean(beanId);
		}
		catch (APIException e) {
			throw new APIException(e);
		}
		return bean;
	}
}
