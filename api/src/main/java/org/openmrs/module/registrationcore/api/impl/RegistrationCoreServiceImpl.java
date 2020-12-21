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

import static org.openmrs.module.registrationcore.RegistrationCoreConstants.LOCAL_FINGERPRINT_NAME;
import static org.openmrs.module.registrationcore.RegistrationCoreConstants.NATIONAL_FINGERPRINT_NAME;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.*;
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
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricMatch;
import org.openmrs.module.registrationcore.api.biometrics.model.BiometricSubject;
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.errorhandling.ErrorHandlingService;
import org.openmrs.module.registrationcore.api.errorhandling.FetchingMpiPatientParameters;
import org.openmrs.module.registrationcore.api.errorhandling.PdqErrorHandlingService;
import org.openmrs.module.registrationcore.api.mpi.common.MpiException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatientFilter;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProvider;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.PatientNameSearch;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.openmrs.module.registrationcore.api.xdssender.XdsCcdImporter;
import org.openmrs.module.santedb.mpiclient.api.MpiClientService;
import org.openmrs.module.santedb.mpiclient.exception.MpiClientException;
import org.openmrs.module.santedb.mpiclient.model.MpiPatient;
import org.openmrs.module.santedb.mpiclient.model.MpiPatientExport;
import org.openmrs.serialization.SerializationException;
import org.openmrs.validator.PatientIdentifierValidator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
	@Qualifier("registrationcore.xdsCcdImporter")
	private XdsCcdImporter xdsCcdImporter;

	@Autowired
	@Qualifier("registrationcore.identifierBuilder")
	private IdentifierBuilder identifierBuilder;

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

	@Override
	public Patient registerPatient(Patient patient, List<Relationship> relationships, String identifierString,
								   Location identifierLocation, BiometricData biometricData) {
		RegistrationData data = new RegistrationData();
		data.setPatient(patient);
		data.setRelationships(relationships);
		data.setIdentifier(identifierString);
		data.setIdentifierLocation(identifierLocation);
		if (biometricData != null) {
			data.addBiometricData(biometricData);
		}

		return registerPatient(data);
	}

	/**
	 *  @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(RegistrationData)
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


		PatientIdentifier pId = validateOrGenerateIdentifier(identifierString, identifierLocation);
		patient.addIdentifier(pId);

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
		for (BiometricData biometricData : registrationData.getBiometrics()) {
			saveBiometricsForPatient(patient, biometricData);
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

//		Check if the MPI (CR) is configured and search for/extract similar patients from it.
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
	public Patient findMpiPatient(String identifier, String identifierTypeUuid) {
		if (!registrationCoreProperties.isMpiEnabled()) {
			throw new MpiException("Should not perform 'findMpiPatient' when MPI is disabled");
		}
		MpiProvider mpiProvider = registrationCoreProperties.getMpiProvider();
		Patient patient = null;
		try {
			patient = mpiProvider.fetchMpiPatient(identifier, identifierTypeUuid);
		} catch (RuntimeException e) {
			servePdqExceptionAndThrowAgain(e,
					prepareParameters(identifier, identifierTypeUuid),
					PdqErrorHandlingService.FIND_MPI_PATIENT_DESTINATION);
		}
		return patient;
	}
	@Override
	public MpiPatient fetchMpiPatientWithObservations(String identifier, String identifierTypeUuid) {
		if (!registrationCoreProperties.isMpiEnabled()) {
			throw new MpiException("Should not perform 'fetchMpiPatientWithObservations' when MPI is disabled");
		}
		MpiProvider mpiProvider = registrationCoreProperties.getMpiProvider();
		MpiPatient mpiPatient = null;
		try {
			mpiPatient = mpiProvider.fetchMpiPatientWithObservations(identifier, identifierTypeUuid);
		} catch (RuntimeException e) {
			servePdqExceptionAndThrowAgain(e,
					prepareParameters(identifier, identifierTypeUuid),
					PdqErrorHandlingService.FIND_MPI_PATIENT_DESTINATION);
		}
		return mpiPatient;
	}

	@Override
	public String importMpiPatient(String personId) {
		PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByName(
				RegistrationCoreConstants.MPI_IDENTIFIER_TYPE_ECID_NAME);

		return importMpiPatient(personId, patientIdentifierType.getUuid());
	}

	@Override
	public String importMpiPatient(String patientIdentifier, String patientIdentifierTypeUuid) {
		MpiPatient foundPatient = fetchMpiPatientWithObservations(patientIdentifier, patientIdentifierTypeUuid);

		String savedPatientUuid = null;
		try {
			if (foundPatient == null) {
				throw new MpiException(String.format(
						"Error during importing Patient from MPI. "
								+ "Patient ID: %s of identifier type: %s has not been found in MPI",
						patientIdentifier, patientIdentifierTypeUuid));
			}else{
				Patient savedPatient = persistImportedMpiPatient(foundPatient.toPatient());
				Location defaultLocation = Context.getLocationService().getDefaultLocation();
//				TODO save the extra obs
//				Create registration encounter
				EncounterType registrationEncounterType = Context.getEncounterService()
						.getEncounterTypeByUuid(registrationCoreProperties.getRegistrationEncounterTypeUuid());
				EncounterRole registrationEncounterRole = registrationCoreProperties.getRegistrationEncounterRole();
				// create the encounter
				Encounter registrationEncounter = new Encounter();
				registrationEncounter.setPatient(savedPatient);
				registrationEncounter.setEncounterType(registrationEncounterType);
				registrationEncounter.setLocation(defaultLocation);
				registrationEncounter.setEncounterDatetime(new Date());
				Context.getEncounterService().saveEncounter(registrationEncounter);
//
				List<Obs> patientObservations = foundPatient.getPatientObservations();
				Iterator<Obs> iterator = patientObservations.iterator();
				while(iterator.hasNext()){
					Obs next = iterator.next();
					next.setPerson(savedPatient);
					if(next.getObsDatetime() == null){
						next.setObsDatetime(new Date());
					}
					Iterator<Obs> groupIterator = next.getGroupMembers().iterator();
					while(groupIterator.hasNext()){
						Obs memberObs = groupIterator.next();
						memberObs.setPerson(savedPatient);
						memberObs.setObsGroup(next);
					}
					registrationEncounter.addObs(next);
				}
				Context.getEncounterService().saveEncounter(registrationEncounter);

				exportPatient(savedPatient);
				savedPatientUuid = savedPatient.getUuid();
			}
		} catch (RuntimeException e) {
//			servePdqExceptionAndThrowAgain(e,
//					prepareParameters(patientIdentifier, patientIdentifierTypeUuid),
//					PdqErrorHandlingService.PERSIST_MPI_PATIENT_DESTINATION);
			log.error(ExceptionUtils.getFullStackTrace(e));
		}
		return savedPatientUuid;
	}

	private void exportPatient(Patient savedPatient) {
		MpiClientService mpiClientService = Context.getService(MpiClientService.class);
		MpiPatientExport patientExport = new MpiPatientExport(savedPatient,null,null,null,null);
		try {
			mpiClientService.exportPatient(patientExport);
		} catch (MpiClientException e) {
			log.error(ExceptionUtils.getFullStackTrace(e));
		}
	}

	private void servePdqExceptionAndThrowAgain(RuntimeException e, String message,
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

	private String prepareParameters(String identifier, String identifierTypeUuid) {
		FetchingMpiPatientParameters parameters = new FetchingMpiPatientParameters(identifier,
				identifierTypeUuid);
		try {
			return new ObjectMapper().writeValueAsString(parameters);
		} catch (IOException e) {
			throw new RuntimeException("Cannot prepare parameters for OutgoingMessageException", e);
		}
	}

	@Override
	public BiometricEngine getBiometricEngine() {
		return registrationCoreProperties.getBiometricEngine();
	}

	@Override
	public BiometricData saveBiometricsForPatient(Patient patient, BiometricData biometricData) {
		BiometricSubject subject = biometricData.getSubject();
		if (subject == null) {
			log.debug("There are no biometrics to save for patient");
		}
		else {
			if (!isBiometricEngineEnabled()) {
				throw new IllegalStateException("Unable to save biometrics, as no biometrics engine is enabled");
			}
			BiometricEngine biometricEngine = getBiometricEngine();
			log.debug("Using biometric engine: " + biometricEngine.getClass().getSimpleName());

			PatientIdentifierType idType = biometricData.getIdentifierType();
			if (idType != null) {
				log.debug("Saving biometrics as a patient identifier of type: " + idType.getName());
				//Currently, lookup checks if identifier exists only in local fingerprint server, but this method will be used also by national ids
				//BiometricSubject existingSubject = (subject.getSubjectId() == null ? null : biometricEngine.lookup(subject.getSubjectId()));
				//if (existingSubject == null) {
				//	throw new IllegalArgumentException("The subject doesn't exist in m2Sys. Did you call m2Sys enroll method?") ;
				//}

				// If patient does not already have an identifier that references this subject, add one
				boolean identifierExists = false;
				for (PatientIdentifier identifier : patient.getPatientIdentifiers(idType)) {
					if (identifier.getIdentifier().equals(subject.getSubjectId())) {
						identifierExists = true;
					}
				}
				if (identifierExists) {
					log.debug("Identifier already exists for patient");
				} else {
					PatientIdentifier identifier = identifierBuilder.createIdentifier(idType.getUuid(), subject.getSubjectId(), null);
					patient.addIdentifier(identifier);
					patientService.savePatientIdentifier(identifier);
					log.debug("New patient identifier saved for patient: " + identifier);
				}
			} else {
				// TODO: In the future we could add additional support for different storage options - eg. as person attributes
				throw new IllegalArgumentException("Invalid biometric configuration.  No patient identifier type specified");
			}
		}
		return biometricData;
	}

	@Override
	public List<PatientAndMatchQuality> findByBiometricMatch(BiometricSubject subject) {
		List<BiometricMatch> matches = getBiometricEngine().search(subject);
		Set<PatientAndMatchQuality> result = new HashSet<PatientAndMatchQuality>();

		PatientIdentifierType biometricId = patientService.getPatientIdentifierTypeByUuid(
				getGlobalProperty(RegistrationCoreConstants.GP_BIOMETRICS_PERSON_IDENTIFIER_TYPE_UUID));

		PatientIdentifierType nationalBiometricId = patientService.getPatientIdentifierTypeByUuid(
				getGlobalProperty(RegistrationCoreConstants.GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID));

		for (BiometricMatch match : matches) {
			boolean possessNationalFpId = false;
			String subjectId = match.getSubjectId();

			List<Patient> patients = patientService.getPatients(null, subjectId,
					Collections.singletonList(biometricId),true,
					0, Integer.MAX_VALUE);

			if (CollectionUtils.isEmpty(patients)) {
				log.warn("No patient found wth biometric id: " + subjectId);
			} else {
				if (patients.size() > 1) {
					log.warn("Found " + patients.size() + " patients with the same biometric id: " + subjectId);
				}
				for (Patient patient : patients) {
					dao.getSessionFactory().getCurrentSession().refresh(patient);

					possessNationalFpId = checkIfPossessNationalFpId(patient) || possessNationalFpId;
					result.add(new PatientAndMatchQuality(patient, match.getMatchScore(),
							Collections.singletonList(LOCAL_FINGERPRINT_NAME)));
				}
			}

			if (result.isEmpty() && registrationCoreProperties.isMpiEnabled() && !possessNationalFpId) {
				Patient mpiPatient = findMpiPatient(subjectId, nationalBiometricId.getUuid());
				if (mpiPatient != null) {
					result.add(new PatientAndMatchQuality(mpiPatient,
							match.getMatchScore(),
							Collections.singletonList(NATIONAL_FINGERPRINT_NAME)));
				}
			}
		}

		return new ArrayList<PatientAndMatchQuality>(result);
	}

	@Override
	public Patient findByPatientIdentifier(String patientIdentifier, String patientIdentifierTypeUuid) {
		PatientIdentifierType idType = patientService.getPatientIdentifierTypeByUuid(patientIdentifierTypeUuid);
		if (idType == null) {
			throw new APIException(String.format("PatientIdentifierType is missing: UUID %s",
					patientIdentifierTypeUuid));
		} else {
			//Currently method getPatients() does not take into consideration the identifierTypes,
			//so it needs to be filtered anyway
			List<Patient> patients = new ArrayList<Patient>();
			for (Patient p : patientService.getPatients(null, patientIdentifier,
					Collections.singletonList(idType), true)) {
				for (PatientIdentifier pi : p.getIdentifiers()) {
					if (pi.getIdentifierType().equals(idType)) {
						patients.add(p);
						break;
					}
				}
			}

			if (CollectionUtils.isEmpty(patients)) {
				return null;
			} else {
				Patient patient = patients.get(0);
				dao.getSessionFactory().getCurrentSession().refresh(patient);
				return patient;
			}
		}
	}

	@Override
	public Integer importCcd(Patient patient) {
		Integer ccdId = null;
		try {
			ccdId = xdsCcdImporter.downloadAndSaveCcd(patient).getId();
		} catch (Exception e) {
			log.error(e);
		}
		return ccdId;
	}

	private boolean checkIfPossessNationalFpId(Patient patient) {
		PatientIdentifierType nationalBiometricId = patientService.getPatientIdentifierTypeByUuid(
				getGlobalProperty(RegistrationCoreConstants.GP_BIOMETRICS_NATIONAL_PERSON_IDENTIFIER_TYPE_UUID));

		return patient.getPatientIdentifier(nationalBiometricId) != null;
	}

	private Patient persistImportedMpiPatient(Patient mpiPatient) {
		String openMrsIdTypeUuid = adminService.getGlobalProperty(RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_UUID);
		String codePcTypeUuid = adminService.getGlobalProperty(RegistrationCoreConstants.GP_CODE_PC_IDENTIFIER_UUID);
		String codeStTypeUuid = adminService.getGlobalProperty(RegistrationCoreConstants.GP_CODE_ST_IDENTIFIER_UUID);

		PatientIdentifierType openMrsIdType = patientService.getPatientIdentifierTypeByUuid(openMrsIdTypeUuid);
		PatientIdentifierType codePcType = patientService.getPatientIdentifierTypeByUuid(codePcTypeUuid);
		PatientIdentifierType codeStType = patientService.getPatientIdentifierTypeByUuid(codeStTypeUuid);

		if (mpiPatient.getPatientIdentifier(openMrsIdType) == null) {
			PatientIdentifier localId = validateOrGenerateIdentifier(null, null);
			mpiPatient.addIdentifier(localId);
		}else{
//			A patient with similar openmrs Id - possibly from a different instance of OpenMRS
//			Remove the openmrs id, Code ST and Code PC identifiers and create patient
			mpiPatient.removeIdentifier(mpiPatient.getPatientIdentifier(openMrsIdType));
			mpiPatient.removeIdentifier(mpiPatient.getPatientIdentifier(codePcType));
			mpiPatient.removeIdentifier(mpiPatient.getPatientIdentifier(codeStType));
			PatientIdentifier localId = validateOrGenerateIdentifier(null, null);
			mpiPatient.addIdentifier(localId);
		}

		Patient patient = patientService.savePatient(mpiPatient);
		return patient;
	}

	private boolean isBiometricEngineEnabled() {
		return registrationCoreProperties.isBiometricsEngineEnabled();
	}

	private IdentifierSourceService getIssAndUpdateIdSource() {
		IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
		if (idSource == null) {
			String idSourceId = adminService.getGlobalProperty(RegistrationCoreConstants.GP_OPENMRS_IDENTIFIER_SOURCE_ID);
			if (StringUtils.isBlank(idSourceId)) {
				throw new APIException("Please set the id of the identifier source to use to generate patient identifiers");
			}
			try {
				idSource = iss.getIdentifierSource(Integer.valueOf(idSourceId));
				if (idSource == null) {
					throw new APIException("cannot find identifier source with id:" + idSourceId);
				}
			}
			catch (NumberFormatException e) {
				throw new APIException("Identifier source id should be a number");
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

	private String getGlobalProperty(String propertyName) {
		String propertyValue = adminService.getGlobalProperty(propertyName);
		if (StringUtils.isBlank(propertyValue)) {
			throw new APIException(String.format("Property value for '%s' is not set", propertyName));
		}
		return propertyValue;
	}
}