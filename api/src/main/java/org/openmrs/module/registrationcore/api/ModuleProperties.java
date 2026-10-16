/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
	
	protected Integer getIntegerProperty(String propertyName) {
		String propertyValue = getProperty(propertyName);
		try {
			return Integer.valueOf(propertyValue);
		}
		catch (NumberFormatException e) {
			throw new APIException("Incorrect property value. " + propertyValue + " cannot be cast to Integer");
		}
	}
	
	protected String getProperty(String propertyName) {
		String propertyValue = administrationService.getGlobalProperty(propertyName);
		if (StringUtils.isBlank(propertyValue))
			throw new APIException("Property value for '" + propertyName + "' is not set");
		return propertyValue;
	}
}
