package org.openmrs.module.registrationcore.api.impl;

import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.validator.PatientIdentifierValidator;

public class IdentifierGenerator {

    private LocationService locationService;

    private IdentifierSourceService iss;

    private AdministrationService adminService;

    private PatientService patientService;

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public Integer getOpenMrsIdentifierSourceId() {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID;
        return getProperty(propertyName);
    }

    public Integer getIdentifierIdByName(String identifierName) {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID_COMMON + identifierName;
        return getProperty(propertyName);
    }

    private Integer getProperty(String propertyName) {
        String propertyValue = adminService.getGlobalProperty(propertyName);
        Integer intPropertyValue;
        try {
            intPropertyValue = Integer.valueOf(propertyValue);
        } catch (NumberFormatException e) {
            throw new APIException("Identifier source id should be a number");
        }
        return intPropertyValue;
    }

    public PatientIdentifier generateIdentifier(Integer sourceId, Location location) {
        location = getLocation(location);
        IdentifierSource idSource = getSource(sourceId);
        String identifierValue = getIss().generateIdentifier(idSource, null);

        return new PatientIdentifier(identifierValue, idSource.getIdentifierType(), location);
    }

    public PatientIdentifier createIdentifier(Integer identifierId, String identifierValue, Location location) {
        location = getLocation(location);
        PatientIdentifierType identifierType = patientService.getPatientIdentifierType(identifierId);
        PatientIdentifierValidator.validateIdentifier(identifierValue, identifierType);

        return new PatientIdentifier(identifierValue, identifierType, location);
    }

    private IdentifierSource getSource(Integer identifierId) {
        IdentifierSource identifierSource = getIss().getIdentifierSource(identifierId);
        validateIdentifierSource(identifierSource, identifierId);
        return identifierSource;
    }

    private void validateIdentifierSource(IdentifierSource idSource, Integer identifierId) {
        if (idSource == null)
            throw new APIException("cannot find identifier source with id:" + identifierId);
    }

    private Location getLocation(Location identifierLocation) {
        if (identifierLocation == null) {
            identifierLocation = locationService.getDefaultLocation();
            validateIdentifierLocation(identifierLocation);
        }
        return identifierLocation;
    }

    private void validateIdentifierLocation(Location identifierLocation) {
        if (identifierLocation == null)
            throw new APIException("Failed to resolve location to associate to patient identifiers");
    }

    public IdentifierSourceService getIss() {
        if (iss == null) {
            iss = Context.getService(IdentifierSourceService.class);
        }
        return iss;
    }
}
