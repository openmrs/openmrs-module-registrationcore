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
package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Relationship;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.event.Event;
import org.openmrs.event.EventMessage;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.openmrs.module.registrationcore.api.mpi.openempi.MatchedPatientFilter;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.PatientNameSearch;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * It is a default implementation of {@link RegistrationCoreService}.
 */
@Transactional
public class RegistrationCoreServiceImpl extends BaseOpenmrsService implements RegistrationCoreService, ApplicationContextAware {

	protected final Log log = LogFactory.getLog(this.getClass());

	private ApplicationContext applicationContext;

	private RegistrationCoreDAO dao;

	private PatientService patientService;

	private PersonService personService;

	private AdministrationService adminService;

	private IdentifierBuilder identifierBuilder;

	private MatchedPatientFilter matchedPatientFilter;

	private RegistrationCoreProperties coreProperties;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setDao(RegistrationCoreDAO dao) {
		this.dao = dao;
	}

	/**
	 * @return the dao
	 */
	public RegistrationCoreDAO getDao() {
		return dao;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setAdminService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	public void setIdentifierBuilder(IdentifierBuilder identifierBuilder) {
		this.identifierBuilder = identifierBuilder;
	}

	public void setMatchedPatientFilter(MatchedPatientFilter matchedPatientFilter) {
		this.matchedPatientFilter = matchedPatientFilter;
	}

	public void setCoreProperties(RegistrationCoreProperties coreProperties) {
		this.coreProperties = coreProperties;
	}

	/**
     * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(org.openmrs.Patient,
     *      java.util.List, String, Location)
     */
    @Override
    public Patient registerPatient(Patient patient, List<Relationship> relationships, Location identifierLocation) {
        return registerPatient(patient, relationships, null, identifierLocation);
    }

	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(org.openmrs.Patient,
	 *      java.util.List, String, Location)
	 */
	@Override
	public Patient registerPatient(Patient patient, List<Relationship> relationships, String identifierString, Location identifierLocation) {
		if (log.isInfoEnabled())
			log.info("Registering new patient..");
		if (patient == null)
			throw new APIException("Patient cannot be null");

		Integer openMrsIdentifierId = coreProperties.getIdentifierSourceId();
		PatientIdentifier patientIdentifier;
		if (StringUtils.isBlank(identifierString)) {
			patientIdentifier = identifierBuilder.generateIdentifier(openMrsIdentifierId, identifierLocation);
		} else {
			patientIdentifier = identifierBuilder.createIdentifier(openMrsIdentifierId, identifierString, identifierLocation);
		}
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);

		//TODO fix this when creating a patient from a person is possible
		boolean wasAPerson = patient.getPersonId() != null;

		patient = patientService.savePatient(patient);

		if (relationships != null) {
			for (Relationship relationship : relationships) {
				if (relationship.getPersonA() == null) {
					relationship.setPersonA(patient);
				} else if (relationship.getPersonB() == null) {
					relationship.setPersonB(patient);
				} else {
					throw new APIException("Only one side of a relationship should be specified");
				}

				personService.saveRelationship(relationship);
			}
		}

		EventMessage eventMessage = new EventMessage();
		eventMessage.put(RegistrationCoreConstants.KEY_PATIENT_UUID, patient.getUuid());

		//TODO incorrect set of "UUID" each time it generates new RANDOM value, instead of set uuid of creator.
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_UUID, patient.getCreator().getUuid());
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_ID, String.valueOf(patient.getCreator().getId()));
		eventMessage.put(RegistrationCoreConstants.KEY_DATE_REGISTERED, new SimpleDateFormat(
		        RegistrationCoreConstants.DATE_FORMAT_STRING).format(patient.getDateCreated()));
		eventMessage.put(RegistrationCoreConstants.KEY_WAS_A_PERSON, wasAPerson);
		ArrayList<String> relationshipUuids = new ArrayList<String>();
		if (relationships != null) {
			for (Relationship r : relationships) {
				relationshipUuids.add(r.getUuid());
			}
			eventMessage.put(RegistrationCoreConstants.KEY_RELATIONSHIP_UUIDS, relationshipUuids);
		}

		Event.fireEvent(RegistrationCoreConstants.PATIENT_REGISTRATION_EVENT_TOPIC_NAME, eventMessage);

		return patient;
	}

	private SimilarPatientSearchAlgorithm getFastSimilarPatientSearchAlgorithm() {
		String gp = adminService.getGlobalProperty(RegistrationCoreConstants.GP_FAST_SIMILAR_PATIENT_SEARCH_ALGORITHM,
		    "registrationcore.BasicSimilarPatientSearchAlgorithm");

		Object bean = applicationContext.getBean(gp);
		if (bean instanceof SimilarPatientSearchAlgorithm) {
			return (SimilarPatientSearchAlgorithm) bean;
		} else {
			throw new IllegalArgumentException(RegistrationCoreConstants.GP_FAST_SIMILAR_PATIENT_SEARCH_ALGORITHM
			        + " must point to " + "a bean implementing SimilarPatientSearchAlgorithm");
		}
	}

	private SimilarPatientSearchAlgorithm getPreciseSimilarPatientSearchAlgorithm() {
		String gp = adminService.getGlobalProperty(RegistrationCoreConstants.GP_PRECISE_SIMILAR_PATIENT_SEARCH_ALGORITHM,
		    "registrationcore.BasicExactPatientSearchAlgorithm");

		Object bean = applicationContext.getBean(gp);
		if (bean instanceof SimilarPatientSearchAlgorithm) {
			return (SimilarPatientSearchAlgorithm) bean;
		} else {
			throw new IllegalArgumentException(RegistrationCoreConstants.GP_PRECISE_SIMILAR_PATIENT_SEARCH_ALGORITHM
			        + " must point to " + "a bean implementing SimilarPatientSearchAlgorithm");
		}
	}

	private PatientNameSearch getPatientNameSearch() {
		String gp = adminService.getGlobalProperty(RegistrationCoreConstants.GP_PATIENT_NAME_SEARCH,
		    "registrationcore.BasicPatientNameSearch");

		Object bean = applicationContext.getBean(gp);
		if (bean instanceof PatientNameSearch) {
			return (PatientNameSearch) bean;
		} else {
			throw new IllegalArgumentException(RegistrationCoreConstants.GP_PATIENT_NAME_SEARCH + " must point to "
			        + "a bean implementing PatientNameSearch");
		}
	}

	@Override
	public List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                            Double cutoff, Integer maxResults) {
		List<PatientAndMatchQuality> matches = new LinkedList<PatientAndMatchQuality>();

		List<PatientAndMatchQuality> localMatches = getFastSimilarPatientSearchAlgorithm()
				.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
		matches.addAll(localMatches);

		if (coreProperties.isMpiEnabled()) {
			List<PatientAndMatchQuality> mpiMatches = coreProperties.getMpiProvider()
					.findProbablySimilarPatients(patient, otherDataPoints, cutoff, maxResults);
			matches.addAll(mpiMatches);

			matchedPatientFilter.filter(matches);
		}

		return matches;
	}

	@Override
	public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                               Double cutoff, Integer maxResults) {
		List<PatientAndMatchQuality> matches = new LinkedList<PatientAndMatchQuality>();

		List<PatientAndMatchQuality> localMatches = getPreciseSimilarPatientSearchAlgorithm()
				.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
		matches.addAll(localMatches);

		if (coreProperties.isMpiEnabled()) {
			List<PatientAndMatchQuality> mpiMatches = coreProperties.getMpiProvider()
					.findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
			matches.addAll(mpiMatches);

			matchedPatientFilter.filter(matches);
		}

		return matches;
	}

	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#findSimilarGivenNames(String)
	 */
	@Override
	public List<String> findSimilarGivenNames(String searchPhrase) {
		return getPatientNameSearch().findSimilarGivenNames(searchPhrase);
	}

	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#findSimilarFamilyNames(String)
	 */
	@Override
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		return getPatientNameSearch().findSimilarFamilyNames(searchPhrase);
	}

	@Override
	public String importMpiPatient(String personId) {
		if (coreProperties.isMpiEnabled()) {
			MpiProvider mpiProvider = coreProperties.getMpiProvider();
			Patient importedPatient = mpiProvider.importMpiPatient(personId);
			Patient patient = patientService.savePatient(importedPatient);
			return patient.getUuid();
		} else {
			//should not pass here since "importPatient" performs only when MpiProvider is not null
			throw new APIException("Should not perform 'importMpiPatient' when MPI is disabled");
		}
	}
}