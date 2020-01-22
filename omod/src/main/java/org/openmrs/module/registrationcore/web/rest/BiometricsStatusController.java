/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
        } catch (Exception e) {
            status = new BiometricEngineStatus();
            status.setEnabled(false);
            status.setStatusMessage(e.getMessage());
        }
        return new SimpleObject().add("results", status);
    }
}
