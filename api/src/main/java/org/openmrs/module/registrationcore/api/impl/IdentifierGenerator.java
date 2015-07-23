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

    private AdministrationService adminService;

    private IdentifierSourceService iss;

    private LocationService locationService;

    private PatientService patientService;

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setIdentifierSourceService(IdentifierSourceService iss) {
        this.iss = iss;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public Integer getOpenMrsIdentifier() {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID;
        return getIdentifierIdFromProperties(propertyName);
    }

    public Integer getIdentifierIdByName(String identifierName) {
        String propertyName = RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID_COMMON + identifierName;
        return getIdentifierIdFromProperties(propertyName);
    }

    private Integer getIdentifierIdFromProperties(String propertyName) {
        String propertyValue = adminService.getGlobalProperty(propertyName);
        Integer idSourceId;
        try {
            idSourceId = Integer.valueOf(propertyValue);
        } catch (NumberFormatException e) {
            throw new APIException("Identifier source id should be a number");
        }
        return idSourceId;
    }

    public PatientIdentifier generateIdentifier(Integer identifierId, Location location) {
        validateIss();

        location = getLocation(location);

        IdentifierSource idSource = getSource(identifierId);
        String identifierValue = iss.generateIdentifier(idSource, null);

        return new PatientIdentifier(identifierValue, idSource.getIdentifierType(), location);
    }

    public PatientIdentifier createIdentifier(Integer identifierId, String identifierValue, Location location) {
        location = getLocation(location);

        PatientIdentifierType identifierType = new PatientIdentifierType(identifierId);
        PatientIdentifierValidator.validateIdentifier(identifierValue, identifierType);
        return new PatientIdentifier(identifierValue, identifierType, location);
    }

    private void validateIss() {
        //TODO should be replaced by correct injection.
        if (iss == null) {
            iss = Context.getService(IdentifierSourceService.class);
        }
    }

    private IdentifierSource getSource(Integer identifierId) {
        IdentifierSource identifierSource = iss.getIdentifierSource(identifierId);
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
}
