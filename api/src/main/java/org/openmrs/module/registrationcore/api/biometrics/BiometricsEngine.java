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

import org.openmrs.module.registrationcore.api.biometrics.model.EngineStatus;
import org.openmrs.module.registrationcore.api.biometrics.model.Match;
import org.openmrs.module.registrationcore.api.biometrics.model.Subject;

import java.util.List;

/**
 * Provides primary API for interacting with the biometric matching engine
 */
public interface BiometricsEngine {

    EngineStatus getStatus();

    /**
     * Enrolls biometrics for the passed subject
     */
    Subject enroll(Subject subject);

    /**
     * Change an existing subjectId to a new value
     */
    Subject updateSubjectId(String oldId, String newId);

    /**
     * @return a List of PatientAndMatchQuality for the given subject in order to find matching patients
     */
    List<Match> search(Subject subject);

    /**
     * @return the subject saved for the given subjectId
     */
    Subject lookup(String subjectId);

    /**
     * Deletes the saved subject for the given subjectId
     */
    void delete(String subjectId);
}