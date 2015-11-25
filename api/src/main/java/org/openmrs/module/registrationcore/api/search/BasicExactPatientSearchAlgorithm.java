package org.openmrs.module.registrationcore.api.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation of SimilarPatientSearchAlgorithm that searches for:
 * <ul>
 *     <li>Every name must exactly match (on each name component specified on the passed-in patient)</li>
 *     <li>Birthdate within 1 year</li>
 *     <li>Same gender</li>
 * </ul>
 * You would use this to perform one final check before actually creating a patient, after all data has been filled.
 */
@Service("registrationcore.BasicExactPatientSearchAlgorithm")
public class BasicExactPatientSearchAlgorithm implements SimilarPatientSearchAlgorithm {

    @Autowired
    @Qualifier("dbSessionFactory")
    private DbSessionFactory sessionFactory;

    @Override
    public List<PatientAndMatchQuality> findSimilarPatients(Patient patient, Map<String, Object> otherDataPoints, Double cutoff, Integer maxResults) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);criteria.add(Restrictions.eq("voided", false));

        if (patient.getGender() != null) {
            criteria.add(Restrictions.eq("gender", patient.getGender()));
        }

        Date birthdateForQuery = null;

        // TODO change this to only do a "between" match if patient or match birthdate is estimated, otherwise require an exact birthdate match
        // our query birthdate is the patient birthdate if it known
        if (patient.getBirthdate() != null) {
            birthdateForQuery = patient.getBirthdate();
        }
        // otherwise see if we have an estimated birthdate passed in via "birthdateYears" and "birthdateMonth" parameters (which is what the registration app uses)
        else if (otherDataPoints != null && otherDataPoints.containsKey("birthdateYears") && otherDataPoints.get("birthdateYears") != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -(Integer) otherDataPoints.get("birthdateYears"));
            birthdateForQuery = cal.getTime();
        }
        else if (otherDataPoints != null && otherDataPoints.containsKey("birthdateMonths") && otherDataPoints.get("birthdateMonths") != null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -(Integer) otherDataPoints.get("birthdateMonths"));
            birthdateForQuery = cal.getTime();
        }

        if (birthdateForQuery != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthdateForQuery);
            cal.add(Calendar.YEAR, -1);
            Date fromDate = cal.getTime();
            cal.add(Calendar.YEAR, 2);
            Date toDate = cal.getTime();
            criteria.add(Restrictions.between("birthdate", fromDate, toDate));
        }

        if (patient.getNames() != null) {
            addNameCriteria(criteria, patient);
        }

        List<PatientAndMatchQuality> matches = new ArrayList<PatientAndMatchQuality>();
        List<String> matchedFields = Arrays.asList("personName", "gender", "birthdate");
        for (Patient matchingPatient : ((List<Patient>) criteria.list())) {
            PatientAndMatchQuality match = new PatientAndMatchQuality(matchingPatient, cutoff, matchedFields);
            matches.add(match);
        }
        return matches;
    }

    private String[] nameFields = new String[] {
            "prefix", "givenName", "middleName", "familyNamePrefix",
            "familyName", "familyName2", "familyNameSuffix", "degree"
    };

    private void addNameCriteria(Criteria criteria, Patient patient) {
        criteria.createAlias("names", "names");
        for (PersonName name : patient.getNames()) {
            criteria.add(Restrictions.eq("names.voided", false));

            Conjunction and = Restrictions.conjunction();
            for (String nameField : nameFields) {
                try {
                    String value = (String) PropertyUtils.getProperty(name, nameField);
                    // if this name component is defined on the patient we are creating
                    if (StringUtils.isNotEmpty(value)) {
                        // then it must either match exactly, or be null, on an existing patient, to return a match
                        Disjunction or = Restrictions.disjunction();
                        or.add(Restrictions.isNull("names." + nameField));
                        or.add(Restrictions.eq("names." + nameField, value));
                        and.add(or);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Couldn't get name field " + nameField);
                }
            }
            criteria.add(and);
        }
    }

}
