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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
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
import org.openmrs.module.registrationcore.RegistrationData;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.biometrics.BiometricEngine;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricData;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.FetchingMpiPatientParameters;
import org.openmrs.module.registrationcore.api.errorhandling.PdqErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFilter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.PatientNameSearch;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.validator.PatientIdentifierValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.IOException;

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

	@Autowired
	@Qualifier("registrationcore.mpiProperties")
	private MpiProperties mpiProperties;

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
     * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(Patient, List, String, Location)
     */
    @Override
    public Patient registerPatient(Patient patient, List<Relationship> relationships, String identifierString, Location identifierLocation) {
        RegistrationData data = new RegistrationData();
        data.setPatient(patient);
        data.setRelationships(relationships);
        data.setIdentifier(identifierString);
        data.setIdentifierLocation(identifierLocation);
        return registerPatient(data);
    }

	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(RegistrationData)
	 */
	@Override
	public Patient registerPatient(RegistrationData registrationData) {
		if (log.isInfoEnabled()) {
            log.info("Registering new patient..");
        }
        Patient patient = registrationData.getPatient();
		String identifierString = registrationData.getIdentifier();
		Location identifierLocation = registrationData.getIdentifierLocation();

		if (patient == null) {
            throw new APIException("Patient cannot be null");
        }

		PatientIdentifier pId =validateOrGenerateIdentifier(identifierString, identifierLocation);

		patient.addIdentifier(pId);
        pId.setPreferred(true);

		//TODO fix this when creating a patient from a person is possible
		boolean wasAPerson = patient.getPersonId() != null;

		patient = patientService.savePatient(patient);

		List<Relationship> relationships = registrationData.getRelationships();
		ArrayList<String> relationshipUuids = new ArrayList<String>();
        for (Relationship relationship : relationships) {
            if (relationship.getPersonA() == null) {
                relationship.setPersonA(patient);
            }
            else if (relationship.getPersonB() == null) {
                relationship.setPersonB(patient);
            }
            else {
                throw new APIException("Only one side of a relationship should be specified");
            }
            personService.saveRelationship(relationship);
            relationshipUuids.add(relationship.getUuid());
        }

        try {
			for (BiometricData biometricData : registrationData.getBiometrics()) {
				Context.getService(RegistrationCoreService.class).saveBiometricsForPatient(patient, biometricData);
			}
		}
		catch (Exception e) {
        	throw new APIException("registrationapp.biometrics.errorContactingBiometricsServer", e);
		}

        DateFormat df = new SimpleDateFormat(RegistrationCoreConstants.DATE_FORMAT_STRING);

		EventMessage eventMessage = new EventMessage();
		eventMessage.put(RegistrationCoreConstants.KEY_PATIENT_UUID, patient.getUuid());
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_UUID, patient.getCreator().getUuid());
		eventMessage.put(RegistrationCoreConstants.KEY_REGISTERER_ID, patient.getCreator().getId());
		eventMessage.put(RegistrationCoreConstants.KEY_DATE_REGISTERED, df.format(patient.getDateCreated()));
		eventMessage.put(RegistrationCoreConstants.KEY_WAS_A_PERSON, wasAPerson);
		if (!relationshipUuids.isEmpty()) {
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

	/**
	 * Query to MPI server to find a single patient with Identifier of IdentifierType with IdentifierTypeUuid provided.
	 *
	 * @param identifier person identifier of patient to be found
	 * @param identifierTypeUuid person identifier type of patient which will be found
	 * @return found mpiPatient
	 * @should fail if more than one patient exits in the MPI for that specific identifier and identifier type
	 */
	@Override
	public MpiPatient findMpiPatient(String identifier, String identifierTypeUuid) {
		if (!registrationCoreProperties.isMpiEnabled()) {
			throw new MpiException("Should not perform 'findMpiPatient' when MPI is disabled");
		}
		MpiProvider mpiProvider = registrationCoreProperties.getMpiProvider();
		MpiPatient mpiPatient = null;
		try {
			mpiPatient = mpiProvider.fetchMpiPatient(identifier, identifierTypeUuid);
		} catch (APIException e) {
			servePdqExceptionAndThrowAgain(e,
					prepareParameters(identifier, identifierTypeUuid),
					PdqErrorHandlingService.FIND_MPI_PATIENT_DESTINATION);
		}
		return mpiPatient;
	}

	/**
	 * Imports a patient from the MPI based on the MPI identifier string provided.
	 *
	 * @param personId person identifier of patient which should be imported
	 * @return Patient object that has been persisted locally
	 * @should fail if more than one patient exits in the MPI for that specific identifier and identifier type
	 */
	@Override
	public Patient importMpiPatient(String mpiPatientIdentifier) {
		return importMpiPatient(mpiPatientIdentifier, mpiProperties.getMpiPersonIdentifierTypeUuid());
	}

	/**
	 * Imports a patient from the MPI based on an identifier for a specific identifier type.
	 *
	 * @param patientIdentifier person identifier of patient which should be imported
	 * @param patientIdentifierTypeUuid identifier type uuid of given patientId
	 * @return Patient object that has been persisted locally
	 * @should fail if more than one patient exits in the MPI for that specific identifier and identifier type
	 */
	@Override
	public Patient importMpiPatient(String patientIdentifier, String patientIdentifierTypeUuid) {
		MpiPatient foundPatient = findMpiPatient(patientIdentifier, patientIdentifierTypeUuid);
		Patient savedPatient = null;
		try {
			if (foundPatient == null) {
				throw new MpiException(String.format(
						"Error during importing Patient from MPI. "
								+ "Patient ID: %s of identifier type: %s has not been found in MPI",
						patientIdentifier, patientIdentifierTypeUuid));
			}
			savedPatient = persistImportedMpiPatient(foundPatient);
		} catch (APIException e) {
			servePdqExceptionAndThrowAgain(e,
					prepareParameters(patientIdentifier, patientIdentifierTypeUuid),
					PdqErrorHandlingService.PERSIST_MPI_PATIENT_DESTINATION);
		}
		return savedPatient;
	}

	/**
	 * Serves the PDQ exception if a PDQ error handling service has been specified in the global properties.
	 * Then, throws MPI exception again.
	 *
	 * @param e APIException triggered
	 * @param message message to be resent by the error handling service
	 * @param destination the destination the message was supposed to be sent to
	 */
	private void servePdqExceptionAndThrowAgain(APIException e, String message,
			String destination) {
		ErrorHandlingService errorHandler = registrationCoreProperties.getPdqErrorHandlingService();
		if (errorHandler == null) {
			throw new MpiException(message + " with not configured PDQ error handler", e);
		} else {
			errorHandler.handle(message, destination,true,
					ExceptionUtils.getFullStackTrace(e));
			throw new MpiException(message, e);
		}
	}

	/**
	 * Prepares the string of parameters required by the retrying service in the error handler
	 *
	 * @param identifier identifier for patient that is to be fetched by the retry service from the MPI
	 * @param identifierTypeUuid uuid of the identifier type for the identifier provided
	 * @return parameters in the form of a string
	 */
	private String prepareParameters(String identifier, String identifierTypeUuid) {
		FetchingMpiPatientParameters parameters = new FetchingMpiPatientParameters(identifier,
				identifierTypeUuid);
		try {
			return new ObjectMapper().writeValueAsString(parameters);
		} catch (IOException e) {
			throw new APIException("Cannot prepare parameters for OutgoingMessageException", e);
		}
	}

	/**
	 * Save the MPI patient imported from the MPI to the local DB and return the patient.
	 *
	 * @param mpiPatient MPI patient fetched from MPI
	 * @return local patient object of saved MPI patient
	 */
	private Patient persistImportedMpiPatient(MpiPatient mpiPatient) {
		PatientIdentifierType openMrsIdType = patientService.getPatientIdentifierTypeByUuid(registrationCoreProperties.getOpenMrsIdentifierUuid());
		if (mpiPatient.getPatientIdentifier(openMrsIdType) == null) {
			PatientIdentifier localId = validateOrGenerateIdentifier(null,null);
			mpiPatient.addIdentifier(localId);
		}
		Patient patient = patientService.savePatient(mpiPatient.convertToPatient());
		return patient;
	}

	private IdentifierSourceService getIssAndUpdateIdSource() {
		IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
		if (idSource == null) {
			Integer idSourceId = registrationCoreProperties.getOpenMrsIdentifierSourceId();
			idSource = iss.getIdentifierSource(idSourceId);
			if (idSource == null) {
				throw new APIException("cannot find identifier source with id:" + idSourceId);
			}
		}
		return iss;
	}

	private Location getIdentifierLocation(Location identifierLocation) {
		if (identifierLocation == null) {
			identifierLocation = locationService.getDefaultLocation();
			if (identifierLocation == null) {
				throw new APIException("Failed to resolve location to associate to patient identifiers");
			}
		}

		// see if there is a primary identifier location further up the chain, use that instead if there is
		Location identifierAssignmentLocationAssociatedWith = getIdentifierAssignmentLocationAssociatedWith(identifierLocation);
		if (identifierAssignmentLocationAssociatedWith != null) {
			identifierLocation = identifierAssignmentLocationAssociatedWith;
		}

		return identifierLocation;
	}

	private PatientIdentifier validateOrGenerateIdentifier(String identifierString, Location identifierLocation) {
		IdentifierSourceService iss = getIssAndUpdateIdSource();
		identifierLocation = getIdentifierLocation(identifierLocation);

		// generate identifier if necessary, otherwise validate
		if (StringUtils.isBlank(identifierString)) {
			identifierString = iss.generateIdentifier(idSource, null);
		} else {
			PatientIdentifierValidator.validateIdentifier(identifierString, idSource.getIdentifierType());
		}

		PatientIdentifier pId = new PatientIdentifier(identifierString, idSource.getIdentifierType(),
				identifierLocation);
		pId.setPreferred(true);

		return pId;
	}

    @Override
    public BiometricEngine getBiometricEngine() {
	    return registrationCoreProperties.getBiometricEngine();
    }

    @Override
    public BiometricData saveBiometricsForPatient(Patient patient, BiometricData biometricData) {
	    BiometricSubject subject = biometricData.getSubject();

	    if (subject == null || subject.getFingerprints().isEmpty()) {
	        log.debug("There are no biometrics to save for patient");
        }
        else {
            log.debug("Saving biometrics for patient.  Found " + subject.getFingerprints().size() + " fingerprints.");

            BiometricEngine biometricEngine = getBiometricEngine();
            if (biometricEngine == null) {
                throw new IllegalStateException("Unable to save biometrics, as no biometrics engine is enabled");
            }
            log.debug("Using biometric engine: " + biometricEngine.getClass().getSimpleName());

            PatientIdentifierType idType = biometricData.getIdentifierType();
            if (idType != null) {
                log.debug("Saving biometrics as a patient identifier of type: " + idType.getName());

                BiometricSubject existingSubject = (subject.getSubjectId() == null ? null : biometricEngine.lookup(subject.getSubjectId()));
                if (existingSubject == null) {
                    subject = biometricEngine.enroll(subject);
                    log.debug("Enrolled new biometric subject: " + subject.getSubjectId());
                }
                else {
                    subject = biometricEngine.update(subject);
                    log.debug("Updated existing biometric subject: " + subject.getSubjectId());
                }

                // If patient does not already have an identifier that references this subject, add one
                boolean identifierExists = false;
                for (PatientIdentifier identifier : patient.getPatientIdentifiers(idType)) {
                    if (identifier.getIdentifier().equals(subject.getSubjectId())) {
                        identifierExists = true;
                    }
                }
                if (identifierExists) {
                    log.debug("Identifier already exists for patient");
                }
                else {
                    PatientIdentifier identifier = new PatientIdentifier(subject.getSubjectId(), idType, null);
                    patient.addIdentifier(identifier);
                    patientService.savePatientIdentifier(identifier);
                    log.debug("New patient identifier saved for patient: " + identifier);
                }
            }
            else {
                // TODO: In the future we could add additional support for different storage options - eg. as person attributes
                throw new IllegalArgumentException("Invalid biometric configuration.  No patient identifier type specified");
            }
        }

        return biometricData;
    }
}
