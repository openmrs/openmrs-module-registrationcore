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
import org.openmrs.module.registrationcore.api.mpi.common.MpiFacade;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.PatientNameSearch;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

	private IdentifierGenerator identifierGenerator;

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

	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
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

		Integer openMrsIdentifierId = identifierGenerator.getOpenMrsIdentifierSourceId();
		PatientIdentifier patientIdentifier;
		if (StringUtils.isBlank(identifierString)) {
			patientIdentifier = identifierGenerator.generateIdentifier(openMrsIdentifierId, identifierLocation);
		} else {
			patientIdentifier = identifierGenerator.createIdentifier(openMrsIdentifierId, identifierString, identifierLocation);
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
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_UUID, patient.getCreator().getUuid());
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

		Event.fireEvent(RegistrationCoreConstants.TOPIC_NAME, eventMessage);

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

	private MpiFacade getMpiFacade() {
        String gp = adminService.getGlobalProperty(RegistrationCoreConstants.GP_MPI_FACADE,
                "registrationcore.mpiFacade");

        Object bean = applicationContext.getBean(gp);

        if (bean instanceof MpiFacade) {
            return (MpiFacade) bean;
        } else {
            throw new IllegalArgumentException(RegistrationCoreConstants.GP_MPI_FACADE
                    + " must point to " + "a bean implementing MpiFacade");
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
		return getFastSimilarPatientSearchAlgorithm().findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}

	@Override
	public List<PatientAndMatchQuality> findProbablisticSimilarPatientsOnMpi(Patient patient, Map<String, Object> otherDataPoints,
																			 Double cutoff, Integer maxResults) {
		return getMpiFacade().findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}

	@Override
	public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                               Double cutoff, Integer maxResults) {
		return getPreciseSimilarPatientSearchAlgorithm().findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}

	@Override
	public List<PatientAndMatchQuality> findPreciseSimilarPatientsOnMpi(Patient patient, Map<String, Object> otherDataPoints,
                                                                        Double cutoff, Integer maxResults) {
		return getMpiFacade().findPreciseSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
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
		MpiFacade mpiFacade = getMpiFacade();
		Patient importedPatient = mpiFacade.importMpiPatient(personId);
		Patient patient = patientService.savePatient(importedPatient);
		return patient.getUuid();
	}
}
