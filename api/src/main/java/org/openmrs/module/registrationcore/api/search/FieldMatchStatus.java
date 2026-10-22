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

/**
 * How one field of the searched-for patient compares to the same field of a match. The search
 * algorithm decides which status applies (including any thresholds); consumers should only read the
 * status.
 * 
 * @see FieldMatchResult
 */
public enum FieldMatchStatus {
	
	/**
	 * The values agree.
	 */
	MATCH,
	
	/**
	 * The values are close but not identical (e.g. a phonetic-only name match, a birthdate within the
	 * algorithm's tolerance, or only some parts of an address agreeing).
	 */
	SIMILAR,
	
	/**
	 * The field is recorded on both sides and the values disagree.
	 */
	DIFFERENT,
	
	/**
	 * The field is not recorded on one or both sides, so no comparison was possible.
	 */
	MISSING
}
