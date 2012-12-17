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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.registrationcore.api.db.RegistrationCoreDAO;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.openmrs.module.registrationcore.api.search.SimilarPatientSearchAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link RegistrationCoreService}.
 */
@Transactional
public class RegistrationCoreServiceImpl extends BaseOpenmrsService implements RegistrationCoreService, GlobalPropertyListener {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private RegistrationCoreDAO dao;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Autowired
	@Qualifier("personService")
	private PersonService personService;
	
	@Autowired
	@Qualifier("locationService")
	private LocationService locationService;
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	private static IdentifierSource idSource;
	
	/**
	 * @param dao the dao to set
	 */
	private SimilarPatientSearchAlgorithm fastSimilarPatientSearchAlgorithm;
	
	private SimilarPatientSearchAlgorithm preciseSimilarPatientSearchAlgorithm;
	
	public void setDao(RegistrationCoreDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public RegistrationCoreDAO getDao() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.module.registrationcore.api.RegistrationCoreService#registerPatient(org.openmrs.Patient,
	 *      java.util.List, Location)
	 */
	@Override
	public Patient registerPatient(Patient patient, List<Relationship> relationships, Location identifierLocation) {
		if (log.isInfoEnabled())
			log.info("Registering new patient..");
		if (patient == null)
			throw new APIException("Patient cannot be null");
		
		IdentifierSourceService iss = Context.getService(IdentifierSourceService.class);
		if (idSource == null) {
			String idSourceId = adminService.getGlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID);
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
		String identifierString = iss.generateIdentifier(idSource, null);
		PatientIdentifier pId = new PatientIdentifier(identifierString, idSource.getIdentifierType(), identifierLocation);
		patient.addIdentifier(pId);
		
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
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String gpName) {
		return RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID.equals(gpName);
	}
	
	@Override
    public void setFastSimilarPatientSearchAlgorithm(SimilarPatientSearchAlgorithm fastSimilarPatientSearchAlgorithm) {
		this.fastSimilarPatientSearchAlgorithm = fastSimilarPatientSearchAlgorithm;
	}
	
	@Override
    public void setPreciseSimilarPatientSearchAlgorithm(SimilarPatientSearchAlgorithm preciseSimilarPatientSearchAlgorithm) {
		this.preciseSimilarPatientSearchAlgorithm = preciseSimilarPatientSearchAlgorithm;
	}
	
	@Override
	public List<PatientAndMatchQuality> findFastSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                            Double cutoff, Integer maxResults) {
		return fastSimilarPatientSearchAlgorithm.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}
	
	@Override
	public List<PatientAndMatchQuality> findPreciseSimilarPatients(Patient patient, Map<String, Object> otherDataPoints,
	                                                               Double cutoff, Integer maxResults) {
		return preciseSimilarPatientSearchAlgorithm.findSimilarPatients(patient, otherDataPoints, cutoff, maxResults);
	}
}
