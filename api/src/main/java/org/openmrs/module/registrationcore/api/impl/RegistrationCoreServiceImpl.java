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
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Relationship;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.event.Event;
import org.openmrs.event.EventMessage;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFilter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.PatientNameSearch;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.openmrs.validator.PatientIdentifierValidator;
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
public class RegistrationCoreServiceImpl extends BaseOpenmrsService implements RegistrationCoreService, GlobalPropertyListener, ApplicationContextAware {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private ApplicationContext applicationContext;
	
	private RegistrationCoreDAO dao;
	
	private PatientService patientService;
	
	private PersonService personService;
	
	private LocationService locationService;
	
	private AdministrationService adminService;

	private static IdentifierSource idSource;

	private MpiPatientFilter mpiPatientFilter;

	private RegistrationCoreProperties registrationCoreProperties;

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

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setAdminService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	public void setMpiPatientFilter(MpiPatientFilter mpiPatientFilter) {
		this.mpiPatientFilter = mpiPatientFilter;
	}

	public void setRegistrationCoreProperties(RegistrationCoreProperties coreProperties) {
		this.registrationCoreProperties = coreProperties;
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
		
		IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
		if (idSource == null) {
			String idSourceId = adminService.getGlobalProperty(RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID);
			if (StringUtils.isBlank(idSourceId))
				throw new APIException("Please set the id of the identifier source to use to generate patient identifiers");
			
			try {
				idSource = iss.getIdentifierSource(Integer.valueOf(idSourceId));
				if (idSource == null)
					throw new APIException("cannot find identifier source with id:" + idSourceId);
			}
			catch (NumberFormatException e) {
				throw new APIException("Identifier source id should be a number");
			}
		}
		
		if (identifierLocation == null) {
			identifierLocation = locationService.getDefaultLocation();
			if (identifierLocation == null)
				throw new APIException("Failed to resolve location to associate to patient identifiers");
		}

        // see if there is a primary identifier location further up the chain, use that instead if there is
        Location identifierAssignmentLocationAssociatedWith = getIdentifierAssignmentLocationAssociatedWith(identifierLocation);
        if (identifierAssignmentLocationAssociatedWith != null) {
            identifierLocation = identifierAssignmentLocationAssociatedWith;
        }

        // generate identifier if necessary, otherwise validate
        if (StringUtils.isBlank(identifierString)) {
            identifierString = iss.generateIdentifier(idSource, null);
        }
        else {
            PatientIdentifierValidator.validateIdentifier(identifierString, idSource.getIdentifierType());
        }
		PatientIdentifier pId = new PatientIdentifier(identifierString, idSource.getIdentifierType(), identifierLocation);
		patient.addIdentifier(pId);
        pId.setPreferred(true);

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
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_ID, patient.getCreator().getId());
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

    private Location getIdentifierAssignmentLocationAssociatedWith(Location location) {
        if (location != null) {
            if (location.hasTag(RegistrationCoreConstants.LOCATION_TAG_IDENTIFIER_ASSIGNMENT_LOCATION)) {
                return location;
            } else {
                return getIdentifierAssignmentLocationAssociatedWith(location.getParentLocation());
            }
        }
        return null;
    }
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty gp) {
		idSource = null;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String gpName) {
		idSource = null;
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
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
    @Transactional(readOnly = true)
	public boolean supportsPropertyName(String gpName) {
		return RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID.equals(gpName);
	}
	
	@Override
    @Transactional(readOnly = true)
	public List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                            Double cutoff, Integer maxResults) {
		List<PatientAndMatchQuality> matches = new LinkedList<PatientAndMatchQuality>();

		List<PatientAndMatchQuality> localMatches = getFastSimilarPatientSearchAlgorithm()
				.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
		matches.addAll(localMatches);

		if (registrationCoreProperties.isMpiEnabled()) {
			List<PatientAndMatchQuality> mpiMatches = registrationCoreProperties.getMpiProvider()
					.findSimilarMatches(patient, otherDataPoints, cutoff, maxResults);
			matches.addAll(mpiMatches);

			mpiPatientFilter.filter(matches);
		}

		return matches;
	}
	
	@Override
    @Transactional(readOnly = true)
	public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                               Double cutoff, Integer maxResults) {
		List<PatientAndMatchQuality> matches = new LinkedList<PatientAndMatchQuality>();

		List<PatientAndMatchQuality> localMatches = getPreciseSimilarPatientSearchAlgorithm()
				.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
		matches.addAll(localMatches);

		if (registrationCoreProperties.isMpiEnabled()) {
			List<PatientAndMatchQuality> mpiMatches = registrationCoreProperties.getMpiProvider()
					.findExactMatches(patient, otherDataPoints, cutoff, maxResults);
			matches.addAll(mpiMatches);

			mpiPatientFilter.filter(matches);
		}

		return matches;
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#findSimilarGivenNames(String)
	 */
	@Override
    @Transactional(readOnly = true)
	public List<String> findSimilarGivenNames(String searchPhrase) {
		return getPatientNameSearch().findSimilarGivenNames(searchPhrase);
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#findSimilarFamilyNames(String)
	 */
	@Override
    @Transactional(readOnly = true)
	public List<String> findSimilarFamilyNames(String searchPhrase) {
		return getPatientNameSearch().findSimilarFamilyNames(searchPhrase);
	}

	@Override
	public String importMpiPatient(String personId) {
		if (registrationCoreProperties.isMpiEnabled()) {
			MpiProvider mpiProvider = registrationCoreProperties.getMpiProvider();
			Patient importedPatient = mpiProvider.fetchMpiPatient(personId);
			Patient patient = patientService.savePatient(importedPatient);
			return patient.getUuid();
		} else {
			//should not pass here since "importPatient" performs only when MpiProvider is not null
			throw new APIException("Should not perform 'fetchMpiPatient' when MPI is disabled");
		}
	}

    @Override
    public BiometricEngine getBiometricEngine() {
	    return registrationCoreProperties.getBiometricEngine();
    }
}