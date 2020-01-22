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

import java.io.Serializable;

/**
 * Represents a possible biometric match as a result of searching a biometric database for matches on a given subject
 * The matchScore is a numeric that indicates the strength of the match
 */
public class BiometricMatch implements Serializable, Comparable<BiometricMatch> {

    private String subjectId;
    private Double matchScore;

    public BiometricMatch() {
    }

    public BiometricMatch(String subjectId, Double matchScore) {
        this.subjectId = subjectId;
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        return "Biometric Match [subject: " + subjectId + ", score: " + matchScore + "]";
    }

    @Override
    public int compareTo(BiometricMatch that) {
        int ret = (int) (that.getMatchScore() - this.getMatchScore()); // A higher match score should come first
        if (ret == 0) {
            ret = this.getSubjectId().compareTo(that.getSubjectId());
        }
        return ret;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }
}
