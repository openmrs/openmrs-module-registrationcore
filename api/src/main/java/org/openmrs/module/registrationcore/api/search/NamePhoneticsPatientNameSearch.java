package org.openmrs.module.registrationcore.api.search;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.namephonetics.NamePhoneticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A namephonetics implementation of {@link PatientNameSearch}.
 */
@Service("registrationcore.NamePhoneticsPatientNameSearch")
public class NamePhoneticsPatientNameSearch implements PatientNameSearch {
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarGivenNames(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarGivenNames(String searchPhrase) {
		NamePhoneticsService service = Context.getService(NamePhoneticsService.class);
		return service.findSimilarGivenNames(searchPhrase);
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.search.PatientNameSearch#findSimilarFamilyNames(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		NamePhoneticsService service = Context.getService(NamePhoneticsService.class);
		return service.findSimilarFamilyNames(searchPhrase);
	}
}
