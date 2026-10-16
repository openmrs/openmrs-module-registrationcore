/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.registrationcore.api.impl.PatientActionListener;

import java.util.List;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class RegistrationCoreActivator implements ModuleActivator, DaemonTokenAware {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Registration Core Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Registration Core Module refreshed");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Registration Core Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		log.info("Registration Core Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Registration Core Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Registration Core Module stopped");
	}
	
	/**
	 * @see DaemonTokenAware#setDaemonToken(DaemonToken)
	 */
	@Override
	public void setDaemonToken(DaemonToken daemonToken) {
		log.info("Set daemon token to Registration Core Module");
		List<PatientActionListener> registeredComponents = Context.getRegisteredComponents(PatientActionListener.class);
		for (PatientActionListener patientActionListener : registeredComponents) {
			patientActionListener.setDaemonToken(daemonToken);
		}
	}
}
