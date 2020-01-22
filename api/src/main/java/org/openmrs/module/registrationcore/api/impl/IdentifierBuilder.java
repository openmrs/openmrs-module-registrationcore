package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.validator.PatientIdentifierValidator;

/**
 * This service perform creating and generating identifiers.
 */
public class IdentifierBuilder {
	
	private LocationService locationService;
	
	private IdentifierSourceService iss;
	
	private PatientService patientService;
	
	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
	
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	/**
	 * Generate new identifier
	 * 
	 * @param sourceId identifier source id
	 * @param location identifier location
	 * @return generated patient identifier
	 * @deprecated as of 1.9.0, replaced by {@link #generateIdentifier(String, Location)}
	 */
	@Deprecated
	public PatientIdentifier generateIdentifier(Integer sourceId, Location location) {
		return generateIdentifier(sourceId.toString(), location);
	}
	
	/**
	 * Generate new identifier
	 * 
	 * @param sourceIdentifier identifier source id or uuid
	 * @param location identifier location
	 * @return generated patient identifier
	 */
	public PatientIdentifier generateIdentifier(String sourceIdentifier, Location location) {
		location = getLocation(location);
		IdentifierSource idSource = getSource(sourceIdentifier);
		String identifierValue = getIss().generateIdentifier(idSource, null);
		
		return new PatientIdentifier(identifierValue, idSource.getIdentifierType(), location);
	}
	
	/**
	 * Create identifier with existing value.
	 * 
	 * @param identifierTypeUuid identifier type uuid
	 * @param identifierValue identifier value
	 * @param location identifier location
	 * @return created patient identifier
	 */
	public PatientIdentifier createIdentifier(String identifierTypeUuid, String identifierValue, Location location) {
		location = getLocation(location);
		PatientIdentifierType identifierType = patientService.getPatientIdentifierTypeByUuid(identifierTypeUuid);
		PatientIdentifierValidator.validateIdentifier(identifierValue, identifierType);
		
		return new PatientIdentifier(identifierValue, identifierType, location);
	}
	
	private Location getLocation(Location identifierLocation) {
		if (identifierLocation == null) {
			identifierLocation = locationService.getDefaultLocation();
			validateIdentifierLocation(identifierLocation);
		}
		return identifierLocation;
	}
	
	private IdentifierSource getSource(String sourceIdentifier) {
		IdentifierSource identifierSource = null;
		if (StringUtils.isNumeric(sourceIdentifier)) {
			identifierSource = getIss().getIdentifierSource(Integer.valueOf(sourceIdentifier));
		} else {
			identifierSource = iss.getIdentifierSourceByUuid(sourceIdentifier);
		}
		validateIdentifierSource(identifierSource, sourceIdentifier);
		return identifierSource;
	}
	
	private IdentifierSourceService getIss() {
		if (iss == null) {
			iss = Context.getService(IdentifierSourceService.class);
		}
		return iss;
	}
	
	private void validateIdentifierLocation(Location identifierLocation) {
		if (identifierLocation == null)
			throw new APIException("Failed to resolve location to associate to patient identifiers");
	}
	
	private void validateIdentifierSource(IdentifierSource idSource, String sourceIdentifier) {
		if (idSource == null)
			throw new APIException("cannot find identifier source with id:" + sourceIdentifier);
	}
}
