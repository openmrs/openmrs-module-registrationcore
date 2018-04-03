/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.biometrics;

import org.openmrs.module.registrationcore.api.biometrics.model.BiometricEngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;

import java.util.List;
import org.openmrs.module.registrationcore.api.biometrics.model.EnrollmentResult;

/**
 * Provides primary API for interacting with the biometric matching engine
 */
public interface BiometricEngine {

    BiometricEngineStatus getStatus();

    /**
     * Scans and enrolls biometrics
     */
    EnrollmentResult enroll();

    /**
     * Enrolls biometrics for the passed subject
     */
    EnrollmentResult enroll(BiometricSubject subject);

    /**
     * Updates the biometrics for a given subject, enrolling that subject if they do not already exist
     */
    BiometricSubject update(BiometricSubject subject);

    /**
     * Change an existing subjectId to a new value
     */
    BiometricSubject updateSubjectId(String oldId, String newId);

    /**
     * Scans fingerprints and returns matching patients.
     * @return a List of PatientAndMatchQuality in order to find matching patients
     */
    List<BiometricMatch> search();

    /**
     * @return the subject saved for the given subjectId
     */
    BiometricSubject lookup(String subjectId);

    /**
     * Deletes the saved subject for the given subjectId
     */
    void delete(String subjectId);
}