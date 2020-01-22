/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.biometrics.model;

import org.openmrs.PatientIdentifierType;

import java.io.Serializable;

/**
 * Encapsulates both a BiometricSubject and the mechanism by which these Biometrics are referenced on the patient
 */
public class BiometricData implements Serializable {

    private BiometricSubject subject;
    private PatientIdentifierType identifierType;

    public BiometricData() {
    }

    public BiometricData(BiometricSubject subject, PatientIdentifierType identifierType) {
        this.subject = subject;
        this.identifierType = identifierType;
    }

    public BiometricSubject getSubject() {
        return subject;
    }

    public void setSubject(BiometricSubject subject) {
        this.subject = subject;
    }

    public PatientIdentifierType getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(PatientIdentifierType identifierType) {
        this.identifierType = identifierType;
    }
}
