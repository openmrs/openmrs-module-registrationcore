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
package org.openmrs.module.registrationcore.api.search;

import java.util.List;

import java.util.Objects;
import org.openmrs.Patient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;

/**
 * The results for {@link SimilarPatientSearchAlgorithm}.
 */
public class PatientAndMatchQuality implements Comparable<PatientAndMatchQuality> {

	private final Patient patient;

	private final Double score;

	private final List<String> matchedFields;

	private String sourceLocation;

	public PatientAndMatchQuality(Patient patient, Double score, List<String> matchedFields) {
		this.patient = patient;
		this.score = score;
		this.matchedFields = matchedFields;
	}

	public PatientAndMatchQuality(Patient patient, Double score, List<String> matchedFields, String sourceLocation) {
		this.patient = patient;
		this.score = score;
		this.matchedFields = matchedFields;
		this.sourceLocation = sourceLocation;
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

	public String getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(String sourceLocation) {
		this.sourceLocation = sourceLocation;
	}
}
