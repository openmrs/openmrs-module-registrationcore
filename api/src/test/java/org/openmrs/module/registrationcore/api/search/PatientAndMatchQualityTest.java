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
import org.openmrs.Patient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PatientAndMatchQualityTest {
	
	private static final FieldMatchResult NAME_MATCH = new FieldMatchResult(FieldMatchResult.NAME, FieldMatchStatus.MATCH,
	        4d);
	
	private static final FieldMatchResult PHONE_DIFFERENT = new FieldMatchResult(FieldMatchResult.PHONE,
	        FieldMatchStatus.DIFFERENT, 0d);
	
	private static final FieldMatchResult ADDRESS_MISSING = new FieldMatchResult(FieldMatchResult.ADDRESS,
	        FieldMatchStatus.MISSING, 0d);
	
	@Test
	public void threeArgConstructorShouldDefaultToEmptyNonNullFieldResults() {
		PatientAndMatchQuality quality = new PatientAndMatchQuality(new Patient(), 3d, Arrays.asList("gender"));
		
		assertNotNull(quality.getFieldResults());
		assertTrue(quality.getFieldResults().isEmpty());
		assertNull(quality.getFieldResult(FieldMatchResult.NAME));
	}
	
	@Test
	public void threeArgConstructorShouldKeepExistingGetters() {
		Patient patient = new Patient();
		List<String> matchedFields = Arrays.asList("gender", "birthdate");
		
		PatientAndMatchQuality quality = new PatientAndMatchQuality(patient, 3d, matchedFields);
		
		assertSame(patient, quality.getPatient());
		assertEquals(3d, quality.getScore(), 0d);
		assertSame(matchedFields, quality.getMatchedFields());
	}
	
	@Test
	public void fourArgConstructorShouldTreatNullFieldResultsAsEmpty() {
		PatientAndMatchQuality quality = new PatientAndMatchQuality(new Patient(), 3d, new ArrayList<String>(), null);
		
		assertNotNull(quality.getFieldResults());
		assertTrue(quality.getFieldResults().isEmpty());
	}
	
	@Test
	public void fourArgConstructorShouldKeepFieldResultsInOrder() {
		Patient patient = new Patient();
		List<String> matchedFields = Arrays.asList("names.givenName");
		
		PatientAndMatchQuality quality = new PatientAndMatchQuality(patient, 4d, matchedFields,
		        Arrays.asList(PHONE_DIFFERENT, NAME_MATCH, ADDRESS_MISSING));
		
		assertEquals(Arrays.asList(PHONE_DIFFERENT, NAME_MATCH, ADDRESS_MISSING), quality.getFieldResults());
		assertSame(patient, quality.getPatient());
		assertEquals(4d, quality.getScore(), 0d);
		assertSame(matchedFields, quality.getMatchedFields());
	}
	
	@Test
	public void shouldLookUpFieldResultByKey() {
		PatientAndMatchQuality quality = new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH, PHONE_DIFFERENT));
		
		assertSame(PHONE_DIFFERENT, quality.getFieldResult(FieldMatchResult.PHONE));
		assertSame(NAME_MATCH, quality.getFieldResult(FieldMatchResult.NAME));
		assertNull(quality.getFieldResult(FieldMatchResult.ADDRESS));
		assertNull(quality.getFieldResult(null));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void fieldResultsShouldBeUnmodifiable() {
		PatientAndMatchQuality quality = new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH));
		
		quality.getFieldResults().add(PHONE_DIFFERENT);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void defaultFieldResultsShouldBeUnmodifiable() {
		new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>()).getFieldResults().add(PHONE_DIFFERENT);
	}
	
	@Test
	public void shouldNotBeAffectedByLaterChangesToTheSuppliedList() {
		List<FieldMatchResult> supplied = new ArrayList<FieldMatchResult>();
		supplied.add(NAME_MATCH);
		PatientAndMatchQuality quality = new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>(), supplied);
		
		supplied.add(PHONE_DIFFERENT);
		
		assertEquals(Collections.singletonList(NAME_MATCH), quality.getFieldResults());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectNullFieldResultElement() {
		new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>(), Arrays.asList(NAME_MATCH, null));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectDuplicateFieldKeys() {
		FieldMatchResult secondName = new FieldMatchResult(FieldMatchResult.NAME, FieldMatchStatus.SIMILAR, 0.5d);
		
		new PatientAndMatchQuality(new Patient(), 4d, new ArrayList<String>(), Arrays.asList(NAME_MATCH, secondName));
	}
	
	@Test
	public void shouldSortByScoreDescending() {
		PatientAndMatchQuality low = new PatientAndMatchQuality(new Patient(), 2d, new ArrayList<String>());
		PatientAndMatchQuality mid = new PatientAndMatchQuality(new Patient(), 5d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH));
		PatientAndMatchQuality high = new PatientAndMatchQuality(new Patient(), 12d, new ArrayList<String>(),
		        Arrays.asList(PHONE_DIFFERENT));
		List<PatientAndMatchQuality> matches = new ArrayList<PatientAndMatchQuality>(Arrays.asList(low, high, mid));
		
		Collections.sort(matches);
		
		assertSame(high, matches.get(0));
		assertSame(mid, matches.get(1));
		assertSame(low, matches.get(2));
	}
	
	@Test
	public void compareToShouldIgnoreFieldResults() {
		PatientAndMatchQuality withFields = new PatientAndMatchQuality(new Patient(), 5d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH, PHONE_DIFFERENT));
		PatientAndMatchQuality withoutFields = new PatientAndMatchQuality(new Patient(), 5d, new ArrayList<String>());
		
		assertEquals(0, withFields.compareTo(withoutFields));
		assertEquals(0, withoutFields.compareTo(withFields));
	}
	
	@Test
	public void equalityShouldDependOnPatientOnly() {
		Patient patient = new Patient();
		PatientAndMatchQuality plain = new PatientAndMatchQuality(patient, 1d, new ArrayList<String>());
		PatientAndMatchQuality withFields = new PatientAndMatchQuality(patient, 9d, Arrays.asList("gender"),
		        Arrays.asList(NAME_MATCH));
		
		assertEquals(plain, withFields);
		assertEquals(plain.hashCode(), withFields.hashCode());
	}
	
	@Test
	public void differentPatientsShouldNotBeEqualEvenWithIdenticalResults() {
		PatientAndMatchQuality a = new PatientAndMatchQuality(new Patient(), 5d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH));
		PatientAndMatchQuality b = new PatientAndMatchQuality(new Patient(), 5d, new ArrayList<String>(),
		        Arrays.asList(NAME_MATCH));
		
		assertFalse(a.equals(b));
	}
}
