package org.openmrs.module.registrationcore.api.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.namephonetics.NamePhoneticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * An implementation of {@link SimilarPatientSearchAlgorithm}.
 * <p>
 * It searches using {@link BasicSimilarPatientSearchAlgorithm} apart from the names where it uses
 * the namephonetics algorithms.
 * <p>
 */
@Service("registrationcore.NamePhoneticsPatientSearchAlgorithm")
public class NamePhoneticsPatientSearchAlgorithm extends BasicSimilarPatientSearchAlgorithm {
	
	@Autowired
	public NamePhoneticsPatientSearchAlgorithm(@Qualifier("sessionFactory") SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.BasicSimilarPatientSearchAlgorithm#addNameCriteria(org.hibernate.Criteria,
	 *      org.openmrs.Patient)
	 */
	protected Criteria addNameCriteria(Criteria criteria, Patient patient) {
		return criteria;
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.BasicSimilarPatientSearchAlgorithm#filterPatients(java.util.List,
	 *      org.openmrs.Patient)
	 */
	protected List<Patient> filterPatients(List<Patient> patients, Patient patient) {
		
		NamePhoneticsService service = Context.getService(NamePhoneticsService.class);
		List<Patient> namephoneticsPatients = null;
		
		//for each name, get a list of patients that match using namephonetics
		if (patient.getNames() != null && !patient.getNames().isEmpty()) {
			for (PersonName name : patient.getNames()) {
				List<Patient> pts = service.findPatient(name.getGivenName(), name.getMiddleName(), name.getFamilyName(),
				    name.getFamilyName2());
				
				if (namephoneticsPatients == null) {
					namephoneticsPatients = new ArrayList<Patient>();
					namephoneticsPatients.addAll(pts);
				} else {
					for (Patient pt : pts) {
						if (!namephoneticsPatients.contains(pt)) {
							namephoneticsPatients.add(pt);
						}
					}
				}
			}
		}
		
		//Filter the namephonetics matched patients by those that match the other non name
		//properties. (gender, birthdate, person attributes, person addresses) 
		if (namephoneticsPatients != null) {
			
			for (int index = 0; index < namephoneticsPatients.size(); index++) {
				Patient pt = namephoneticsPatients.get(index);
				
				//If a namephonetic matched patient did not match on the other non name
				//properties, just remove it.
				if (!patients.contains(pt)) {
					namephoneticsPatients.remove(pt);
					index--;
				}
			}
			
			//So now namephoneticsPatients has only those patients that matched both with
			//namephonetics algorithms and the rest of the other non name properties.
			return namephoneticsPatients;
		}
		
		//patient has no names to use for filtering. But is this possible anyway? :)
		return patients;
	}

	/**
	 * Returns a score higher than 0 if value and matches strings are not empty.
	 *
	 * @param value
	 * @param matches
	 * @return 0 if value or matches strings are empty else return 1
	 */
	@Override
	public double countStartWithScoreForField(String value, String matches) {
		if (!StringUtils.isBlank(value) && !StringUtils.isBlank(matches)) {
			return 1;
		}
		return 0;
	}
}
