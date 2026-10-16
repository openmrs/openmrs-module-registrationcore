/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.web.rest;

import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseUriSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class BiometricsStatusController {
	
	@Autowired
	BaseUriSetup baseUriSetup;
	
	@Autowired
	RegistrationCoreProperties registrationCoreProperties;
	
	@RequestMapping(value = "/rest/v1/registrationcore/biometrics/enginestatus", method = RequestMethod.GET)
	@ResponseBody
	public SimpleObject get(HttpServletRequest request, HttpServletResponse response) throws ResponseException {
		baseUriSetup.setup(request);
		BiometricEngineStatus status;
		try {
			BiometricEngine engine = registrationCoreProperties.getBiometricEngine();
			if (engine != null) {
				status = engine.getStatus();
			} else {
				status = new BiometricEngineStatus();
				status.setEnabled(false);
				status.setStatusMessage("registrationcore.biometrics.noEngineConfigured");
			}
		}
		catch (Exception e) {
			status = new BiometricEngineStatus();
			status.setEnabled(false);
			status.setStatusMessage(e.getMessage());
		}
		return new SimpleObject().add("results", status);
	}
}
