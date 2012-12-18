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

import java.util.Date;
import java.util.List;

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
@Service("registrationcore.DefaultPatientSearch")
public class DefaultPatientSearch implements PatientSearch {
	
	private final SessionFactory sessionFactory;
	
	@Autowired
	public DefaultPatientSearch(@Qualifier("sessionFactory") SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientSearch#findSimilarPatients(org.openmrs.Patient,
	 *      java.lang.Integer)
	 * @should find by exact country and exact city
	 * @should find by exact gender
	 * @should find by partial given and partial family name
	 * @should find by birthday within two weeks
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> findSimilarPatients(Patient patient, Integer maxResults) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		criteria.add(Restrictions.eq("voided", false));
		
		if (!StringUtils.isBlank(patient.getGender())) {
			criteria.add(Restrictions.eq("gender", patient.getGender()));
		}
		
		if (patient.getBirthdate() != null) {
			long fourWeeks = 3600000L * 24 * 7 * 4;
			Date birthdate = patient.getBirthdate();
			addDateWithinPeriodRestriction(criteria, birthdate, fourWeeks);
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
		
		return patients;
	}
	
	private void addLikeIfNotBlankRestriction(Junction junction, String property, String value, MatchMode matchMode) {
		if (!StringUtils.isBlank(value)) {
			junction.add(Restrictions.like(property, value, matchMode));
		}
	}
	
	private void addDateWithinPeriodRestriction(Criteria criteria, Date birthdate, long period) {
		long twoDays = 3600000L * 24 * 2;
		long diff = (period + twoDays) / 2;
		Date low = new Date(birthdate.getTime() - diff);
		Date high = new Date(birthdate.getTime() + diff);
		criteria.add(Restrictions.between("birthdate", low, high));
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientSearch#findDuplicatePatients(org.openmrs.Patient,
	 *      java.lang.Integer)
	 *      
	 * @should find by exact country and exact city
	 * @should find by exact gender
	 * @should find by exact given and exact family name
	 * @should find by birthday within three days
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Patient> findDuplicatePatients(Patient patient, Integer maxResults) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
		
		criteria.add(Restrictions.eq("voided", false));
		
		if (!StringUtils.isBlank(patient.getGender())) {
			criteria.add(Restrictions.eq("gender", patient.getGender()));
		}
		
		if (patient.getBirthdate() != null) {
			long week = 3600000 * 24 * 7;
			Date birthdate = patient.getBirthdate();
			addDateWithinPeriodRestriction(criteria, birthdate, week);
		}
		
		if (patient.getNames() != null && !patient.getNames().isEmpty()) {
			criteria.createAlias("names", "names");
			for (PersonName name : patient.getNames()) {
				criteria.add(Restrictions.eq("names.voided", false));
				
				Disjunction or = Restrictions.disjunction();
				addLikeIfNotBlankRestriction(or, "names.middleName", name.getMiddleName(), MatchMode.START);
				criteria.add(or);
				
				Conjunction and = Restrictions.conjunction();
				addLikeIfNotBlankRestriction(and, "names.givenName", name.getGivenName(), MatchMode.EXACT);
				addLikeIfNotBlankRestriction(and, "names.familyName", name.getFamilyName(), MatchMode.EXACT);
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
		
		return patients;
	}
	
}
