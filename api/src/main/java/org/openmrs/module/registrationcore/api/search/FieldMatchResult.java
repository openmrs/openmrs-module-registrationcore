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

import java.util.Objects;

/**
 * The result of comparing a single field of the searched-for patient against a match, as reported
 * by a {@link SimilarPatientSearchAlgorithm} in {@link PatientAndMatchQuality#getFieldResults()}.
 * <p>
 * The field is identified by a stable string key. The well-known keys are defined as constants
 * here; an algorithm may report additional, site-specific keys.
 */
public class FieldMatchResult {
	
	public static final String NAME = "name";
	
	public static final String GENDER = "gender";
	
	public static final String BIRTHDATE = "birthdate";
	
	public static final String MOTHERS_FIRST_NAME = "mothersFirstName";
	
	public static final String ADDRESS = "address";
	
	public static final String PHONE = "phone";
	
	private final String field;
	
	private final FieldMatchStatus status;
	
	private final double score;
	
	/**
	 * @param field the stable key of the compared field, e.g. {@link #NAME}; must not be blank
	 * @param status how the field compares; must not be null
	 * @param score this field's contribution to the overall score, in the same units as
	 *            {@link PatientAndMatchQuality#getScore()}; may be negative. Informational only:
	 *            contributions are not guaranteed to sum to the overall score
	 */
	public FieldMatchResult(String field, FieldMatchStatus status, double score) {
		if (field == null || field.trim().length() == 0) {
			throw new IllegalArgumentException("field must not be blank");
		}
		if (status == null) {
			throw new IllegalArgumentException("status must not be null");
		}
		this.field = field;
		this.status = status;
		this.score = score;
	}
	
	public String getField() {
		return field;
	}
	
	public FieldMatchStatus getStatus() {
		return status;
	}
	
	public double getScore() {
		return score;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FieldMatchResult that = (FieldMatchResult) o;
		return field.equals(that.field) && status == that.status && Double.compare(score, that.score) == 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(field, status, score);
	}
	
	@Override
	public String toString() {
		return "FieldMatchResult{field=" + field + ", status=" + status + ", score=" + score + "}";
	}
}
