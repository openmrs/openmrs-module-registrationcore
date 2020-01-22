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
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * It's a default implementation of {@link SimilarPatientSearchAlgorithm}.
 * <p>
 * It searches by gender, birthdate within +/- two weeks, family, given and middle name, country and city.
 * <p>
 * Names are matched if they start with the given values.
 */
@Service("registrationcore.BasicSimilarPatientSearchAlgorithm")
public class BasicSimilarPatientSearchAlgorithm implements SimilarPatientSearchAlgorithm {

    private final DbSessionFactory sessionFactory;

    public final long TWO_WEEKS = 3600000L * 24 * 7 * 2;

    public long birthdateRangePeriod = TWO_WEEKS;

    @Autowired
    public BasicSimilarPatientSearchAlgorithm(@Qualifier("dbSessionFactory") DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * For adding criterion based on patient names.
     * 
     * @param criteria
     *            the criteria to add to the various name criterion.
     * @param patient
     *            the patient whose names to use for the criteria.
     * @return the criteria that has name based criterion added.
     */
    protected Criteria addNameCriteria(Criteria criteria, Patient patient) {
        if (patient.getNames() != null && !patient.getNames().isEmpty()) {
            criteria.createAlias("names", "names");
            for (PersonName name : patient.getNames()) {
                criteria.add(Restrictions.eq("names.voided", false));

                Disjunction or = Restrictions.disjunction();
                addLikeIfNotBlankRestriction(or, "names.givenName", name.getGivenName(), MatchMode.START);
                addLikeIfNotBlankRestriction(or, "names.middleName", name.getMiddleName(), MatchMode.START);
                addLikeIfNotBlankRestriction(or, "names.familyName", name.getFamilyName(), MatchMode.START);
                criteria.add(or);
            }
        }

        return criteria;
    }

    /**
     * For further filtering a list of patients based on properties for a given patient.
     * 
     * @param patients
     *            the patients to filter.
     * @param patient
     *            the patient whose properties to use for filtering.
     * @return the filtered list of patients.
     */
    protected List<Patient> filterPatients(List<Patient> patients, Patient patient) {
        return patients;
    }

    /**
     * @should find by exact country and exact city
     * @should find by exact gender
     * @should find by partial given and partial family name
     * @should find by birthday within two weeks
     * @should find by phonetic name match
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
            Double cutoff, Integer maxResults) {

        // used to track whether we've added any criteria besides voided=false
        boolean hasCriteria = false;

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);

        criteria.add(Restrictions.eq("voided", false));

        if (!StringUtils.isBlank(patient.getGender())) {
            hasCriteria = true;
            criteria.add(Restrictions.eq("gender", patient.getGender()));
        }

        if (patient.getBirthdate() != null) {
            hasCriteria = true;
            Date birthdate = patient.getBirthdate();
            addDateWithinPeriodRestriction(criteria, birthdate, birthdateRangePeriod);
        }

        if (patient.getNames() != null && !patient.getNames().isEmpty()) {
            hasCriteria = true;
            addNameCriteria(criteria, patient);
        }

        if (patient.getAttributes() != null && !patient.getAttributes().isEmpty()) {
            hasCriteria = true;
            criteria.createAlias("attributes", "attributes");

            for (PersonAttribute attribute : patient.getAttributes()) {
                criteria.add(Restrictions.eq("attributes.voided", false));

                Conjunction and = Restrictions.conjunction();
                and.add(Restrictions.eq("attributes.attributeType", attribute.getAttributeType()));
                addLikeIfNotBlankRestriction(and, "attributes.value", attribute.getValue(), MatchMode.START);

                criteria.add(and);
            }
        }

        if (patient.getAddresses() != null && !patient.getAddresses().isEmpty()) {
            hasCriteria = true;
            criteria.createAlias("addresses", "addresses");
            for (PersonAddress address : patient.getAddresses()) {
                criteria.add(Restrictions.eq("addresses.voided", false));

                Conjunction and = Restrictions.conjunction();
                addLikeIfNotBlankRestriction(and, "addresses.country", address.getCountry(), MatchMode.EXACT);
                addLikeIfNotBlankRestriction(and, "addresses.stateProvince", address.getStateProvince(),
                        MatchMode.EXACT);
                addLikeIfNotBlankRestriction(and, "addresses.countyDistrict", address.getCountyDistrict(),
                        MatchMode.EXACT);
                addLikeIfNotBlankRestriction(and, "addresses.cityVillage", address.getCityVillage(), MatchMode.EXACT);
                addLikeIfNotBlankRestriction(and, "addresses.postalCode", address.getPostalCode(), MatchMode.EXACT);
                addLikeIfNotBlankRestriction(and, "addresses.address1", address.getAddress1(), MatchMode.START);
                addLikeIfNotBlankRestriction(and, "addresses.address2", address.getAddress2(), MatchMode.START);

                criteria.add(and);
            }
        }

        // if we haven't added any criteria, don't search, we never want to return the entire patient list
        if (!hasCriteria) {
            return new ArrayList<PatientAndMatchQuality>();
        }

        @SuppressWarnings("unchecked")
        List<Patient> patients = criteria.list();

        patients = filterPatients(patients, patient);

        List<PatientAndMatchQuality> matches = new ArrayList<PatientAndMatchQuality>();

        for (Patient match : patients) {
            List<String> matchedFields = new ArrayList<String>();

            double score = 0;

            if (patient.getBirthdate() != null && match.getBirthdate() != null) {
                long birthdateDistance = Math.abs(patient.getBirthdate().getTime() - match.getBirthdate().getTime());
                if (birthdateDistance < birthdateRangePeriod) {
                    matchedFields.add("birthdate");
                }
                double birthdateScore = 1.0 / Math.log((1.0 + birthdateDistance));
                score += birthdateScore;
            }

            for (PersonName patientName : patient.getNames()) {
                for (PersonName matchName : match.getNames()) {
                    double familyNameScore = countStartWithScoreForField(matchName.getFamilyName(),
                            patientName.getFamilyName());
                    if (familyNameScore != 0) {
                        score += familyNameScore;
                        matchedFields.add("names.familyName");
                    }

                    double givenNameScore = countStartWithScoreForField(matchName.getGivenName(),
                            patientName.getGivenName());
                    if (givenNameScore != 0) {
                        matchedFields.add("names.givenName");
                        score += givenNameScore;
                    }

                    double middleNameScore = countStartWithScoreForField(matchName.getMiddleName(),
                            patientName.getMiddleName());
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

    /**
     * Returns a score higher than 0 if value starts with the matches string.
     * 
     * @param value
     * @param matches
     * @return 0 if not matched and 1 if matched
     */
    public double countStartWithScoreForField(String value, String matches) {
        if (!StringUtils.isBlank(value) && !StringUtils.isBlank(matches)) {
            if (value.startsWith(matches)) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Adds a like restriction with the given match mode.
     * 
     * @param junction
     * @param property
     * @param value
     * @param matchMode
     */
    public void addLikeIfNotBlankRestriction(Junction junction, String property, String value, MatchMode matchMode) {
        if (!StringUtils.isBlank(value)) {
            junction.add(Restrictions.like(property, value, matchMode));
        }
    }

    /**
     * Adds a restriction on date to be within +/- the given period in ms.
     * 
     * @param criteria
     * @param date
     * @param period
     */
    public void addDateWithinPeriodRestriction(Criteria criteria, Date date, long period) {
        Date low = new Date(date.getTime() - period);
        Date high = new Date(date.getTime() + period);
        criteria.add(Restrictions.between("birthdate", low, high));
    }
}
