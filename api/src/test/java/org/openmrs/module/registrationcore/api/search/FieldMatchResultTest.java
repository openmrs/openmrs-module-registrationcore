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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldMatchResultTest {
	
	@Test
	public void shouldExposeValues() {
		FieldMatchResult result = new FieldMatchResult(FieldMatchResult.PHONE, FieldMatchStatus.MATCH, 20d);
		
		assertEquals("phone", result.getField());
		assertEquals(FieldMatchStatus.MATCH, result.getStatus());
		assertEquals(20d, result.getScore(), 0d);
	}
	
	@Test
	public void shouldAllowNegativeAndZeroScores() {
		assertEquals(-8d, new FieldMatchResult("gender", FieldMatchStatus.DIFFERENT, -8d).getScore(), 0d);
		assertEquals(0d, new FieldMatchResult("phone", FieldMatchStatus.MISSING, 0d).getScore(), 0d);
	}
	
	@Test
	public void shouldDefineStableWellKnownKeys() {
		assertEquals("name", FieldMatchResult.NAME);
		assertEquals("gender", FieldMatchResult.GENDER);
		assertEquals("birthdate", FieldMatchResult.BIRTHDATE);
		assertEquals("mothersFirstName", FieldMatchResult.MOTHERS_FIRST_NAME);
		assertEquals("address", FieldMatchResult.ADDRESS);
		assertEquals("phone", FieldMatchResult.PHONE);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullField() {
		new FieldMatchResult(null, FieldMatchStatus.MATCH, 1d);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectBlankField() {
		new FieldMatchResult("  ", FieldMatchStatus.MATCH, 1d);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullStatus() {
		new FieldMatchResult("name", null, 1d);
	}
	
	@Test
	public void shouldBeEqualWhenFieldStatusAndScoreAreEqual() {
		FieldMatchResult a = new FieldMatchResult("name", FieldMatchStatus.SIMILAR, 0.5d);
		FieldMatchResult b = new FieldMatchResult("name", FieldMatchStatus.SIMILAR, 0.5d);
		
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenAnyPartDiffers() {
		FieldMatchResult base = new FieldMatchResult("name", FieldMatchStatus.SIMILAR, 0.5d);
		
		assertFalse(base.equals(new FieldMatchResult("gender", FieldMatchStatus.SIMILAR, 0.5d)));
		assertFalse(base.equals(new FieldMatchResult("name", FieldMatchStatus.MATCH, 0.5d)));
		assertFalse(base.equals(new FieldMatchResult("name", FieldMatchStatus.SIMILAR, 2d)));
		assertFalse(base.equals(null));
		assertFalse(base.equals("name"));
	}
	
	@Test
	public void shouldTreatNaNScoresAsEqual() {
		FieldMatchResult a = new FieldMatchResult("name", FieldMatchStatus.MATCH, Double.NaN);
		FieldMatchResult b = new FieldMatchResult("name", FieldMatchStatus.MATCH, Double.NaN);
		
		assertTrue(a.equals(b));
		assertEquals(a.hashCode(), b.hashCode());
	}
}
