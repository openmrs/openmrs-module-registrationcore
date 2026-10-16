/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.registrationcore.api.search;

import java.util.List;

import java.util.Objects;
import org.openmrs.Patient;

/**
 * The results for {@link SimilarPatientSearchAlgorithm}.
 */
public class PatientAndMatchQuality implements Comparable<PatientAndMatchQuality> {
	
	private final Patient patient;
	
	private final Double score;
	
	private final List<String> matchedFields;
	
	public PatientAndMatchQuality(Patient patient, Double score, List<String> matchedFields) {
		this.patient = patient;
		this.score = score;
		this.matchedFields = matchedFields;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public Double getScore() {
		return score;
	}
	
	public List<String> getMatchedFields() {
		return matchedFields;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PatientAndMatchQuality o) {
		return o.score.compareTo(score);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PatientAndMatchQuality that = (PatientAndMatchQuality) o;
		return Objects.equals(patient, that.patient);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(patient);
	}
}
