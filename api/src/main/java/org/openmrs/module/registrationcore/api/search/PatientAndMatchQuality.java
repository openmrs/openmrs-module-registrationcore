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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Objects;
import java.util.Set;
import org.openmrs.Patient;

/**
 * The results for {@link SimilarPatientSearchAlgorithm}.
 */
public class PatientAndMatchQuality implements Comparable<PatientAndMatchQuality> {
	
	private final Patient patient;
	
	private final Double score;
	
	private final List<String> matchedFields;
	
	private final List<FieldMatchResult> fieldResults;
	
	public PatientAndMatchQuality(Patient patient, Double score, List<String> matchedFields) {
		this(patient, score, matchedFields, null);
	}
	
	/**
	 * @param fieldResults optional per-field comparison results; null is treated as empty. Must not
	 *            contain null elements or two results with the same field key
	 * @throws IllegalArgumentException if fieldResults contains a null element or a duplicate field key
	 */
	public PatientAndMatchQuality(Patient patient, Double score, List<String> matchedFields,
	    List<FieldMatchResult> fieldResults) {
		this.patient = patient;
		this.score = score;
		this.matchedFields = matchedFields;
		this.fieldResults = copyFieldResults(fieldResults);
	}
	
	private static List<FieldMatchResult> copyFieldResults(List<FieldMatchResult> fieldResults) {
		if (fieldResults == null) {
			return Collections.emptyList();
		}
		Set<String> keys = new HashSet<>();
		for (FieldMatchResult result : fieldResults) {
			if (result == null) {
				throw new IllegalArgumentException("fieldResults must not contain null elements");
			}
			if (!keys.add(result.getField())) {
				throw new IllegalArgumentException(
				        "fieldResults contains more than one result for field " + result.getField());
			}
		}
		return Collections.unmodifiableList(new ArrayList<>(fieldResults));
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
	 * Per-field comparison results, for algorithms that report them (see {@link FieldMatchResult}).
	 * Algorithms that don't populate this yield an empty list; consumers should then fall back to
	 * showing no per-field detail.
	 * 
	 * @return an unmodifiable list in the order the algorithm reported it; never null
	 */
	public List<FieldMatchResult> getFieldResults() {
		return fieldResults;
	}
	
	/**
	 * @param field a field key, e.g. {@link FieldMatchResult#NAME}
	 * @return the result for that field, or null if the algorithm didn't report it
	 */
	public FieldMatchResult getFieldResult(String field) {
		for (FieldMatchResult result : fieldResults) {
			if (result.getField().equals(field)) {
				return result;
			}
		}
		return null;
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
