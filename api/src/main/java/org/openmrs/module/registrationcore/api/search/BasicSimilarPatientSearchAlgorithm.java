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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service("registrationcore.BasicSimilarPatientSearchAlgorithm")
public class BasicSimilarPatientSearchAlgorithm implements SimilarPatientSearchAlgorithm {
	
	private final SessionFactory sessionFactory;
	
	private final long TWO_WEEKS = 3600000L * 24 * 7 * 2;
	
	@Autowired
	public BasicSimilarPatientSearchAlgorithm(@Qualifier("sessionFactory") SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @should find by exact country and exact city
	 * @should find by exact gender
	 * @should find by partial given and partial family name
	 * @should find by birthday within two weeks
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                        Double cutoff, Integer maxResults) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		criteria.add(Restrictions.eq("voided", false));
		
		if (!StringUtils.isBlank(patient.getGender())) {
			criteria.add(Restrictions.eq("gender", patient.getGender()));
		}
		
		if (patient.getBirthdate() != null) {
			Date birthdate = patient.getBirthdate();
			addDateWithinPeriodRestriction(criteria, birthdate, TWO_WEEKS);
		}
		
		if (patient.getNames() != null && !patient.getNames().isEmpty()) {
			criteria.createAlias("names", "names");
			for (PersonName name : patient.getNames()) {
				criteria.add(Restrictions.eq("names.voided", false));
				
				Disjunction or = Restrictions.disjunction();
				addLikeIfNotBlankRestriction(or, "names.givenName", name.getGivenName(), MatchMode.START);
				addLikeIfNotBlankRestriction(or, "names.middleName", name.getMiddleName(), MatchMode.START);
				criteria.add(or);
				
				Conjunction and = Restrictions.conjunction();
				addLikeIfNotBlankRestriction(and, "names.familyName", name.getFamilyName(), MatchMode.START);
				criteria.add(and);
			}
		}
		
		if (patient.getAddresses() != null && !patient.getAddresses().isEmpty()) {
			criteria.createAlias("addresses", "addresses");
			for (PersonAddress address : patient.getAddresses()) {
				criteria.add(Restrictions.eq("addresses.voided", false));
				
				Conjunction and = Restrictions.conjunction();
				addLikeIfNotBlankRestriction(and, "addresses.country", address.getCountry(), MatchMode.EXACT);
				addLikeIfNotBlankRestriction(and, "addresses.cityVillage", address.getCityVillage(), MatchMode.EXACT);
				
				criteria.add(and);
			}
		}
		
		@SuppressWarnings("unchecked")
		List<Patient> patients = criteria.list();
		
		List<PatientAndMatchQuality> matches = new ArrayList<PatientAndMatchQuality>();
		
		for (Patient match : patients) {
			List<String> matchedFields = new ArrayList<String>();
			
			double score = 0;
			
			long birthdateDistance = Math.abs(patient.getBirthdate().getTime() - match.getBirthdate().getTime());
			if (birthdateDistance < TWO_WEEKS) {
				matchedFields.add("birthdate");
			}
			double birthdateScore = 1.0 / Math.log((1.0 + birthdateDistance));
			score += birthdateScore;
			
			for (PersonName patientName : patient.getNames()) {
				for (PersonName matchName : match.getNames()) {
					double familyNameScore = countStartWithScoreForField(matchName.getFamilyName(),
					    patientName.getFamilyName());
					if (familyNameScore != 0) {
						score += familyNameScore;
						matchedFields.add("names.familyName");
					}
					
					double givenNameScore = countStartWithScoreForField(matchName.getFamilyName(),
						patientName.getFamilyName());
					if (givenNameScore != 0) {
						matchedFields.add("names.givenName");
						score += givenNameScore;
					}
					
					double middleNameScore = countStartWithScoreForField(matchName.getFamilyName(),
						patientName.getFamilyName());
					if (middleNameScore != 0) {
						matchedFields.add("names.middleName");
						score += middleNameScore;
					}
				}
			}
			
			for (PersonAddress patientAddress : patient.getAddresses()) {
				for (PersonAddress matchAddress : match.getAddresses()) {
					if (!StringUtils.isBlank(matchAddress.getCountry())
					        && matchAddress.getCountry().equals(patientAddress.getCountry())) {
						matchedFields.add("addresses.country");
						score += 1;
					}
					
					if (!StringUtils.isBlank(matchAddress.getCityVillage())
					        && matchAddress.getCityVillage().equals(patientAddress.getCityVillage())) {
						matchedFields.add("addresses.cityVillage");
						score += 1;
					}
				}
			}
			
			if (cutoff == null) {
				matches.add(new PatientAndMatchQuality(match, score, matchedFields));
			} else {
				if (score >= cutoff) {
					matches.add(new PatientAndMatchQuality(match, score, matchedFields));
				}
			}
		}
		
		Collections.sort(matches);
		
		if (maxResults != null && matches.size() > maxResults) {
			return matches.subList(0, maxResults);
		} else {
			return matches;
		}
	}
	
	private double countStartWithScoreForField(String value, String matches) {
		if (!StringUtils.isBlank(value) && StringUtils.isBlank(matches)) {
			if (value.startsWith(matches)) {
				return 1;
			}
		}
		return 0;
	}
	
	private void addLikeIfNotBlankRestriction(Junction junction, String property, String value, MatchMode matchMode) {
		if (!StringUtils.isBlank(value)) {
			junction.add(Restrictions.like(property, value, matchMode));
		}
	}
	
	private void addDateWithinPeriodRestriction(Criteria criteria, Date birthdate, long period) {
		Date low = new Date(birthdate.getTime() - period);
		Date high = new Date(birthdate.getTime() + period);
		criteria.add(Restrictions.between("birthdate", low, high));
	}
}
