package org.openmrs.module.registrationcore.api.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.validator.PatientIdentifierValidator;

public class IdentifierGenerator {

    private AdministrationService adminService;

    private IdentifierSourceService iss;

    private LocationService locationService;

    public Integer getOpenMrsIdentifier() {
        //TODO change it to RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID_COMMON + "OpenMRS"
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

    public PatientIdentifier generateIdentifier(Integer identifierId,
                                                String identifierString,
                                                Location location) {
        //TODO should be replaced by correct injection.
        if (iss == null) {
            iss = Context.getService(IdentifierSourceService.class);
        }

        IdentifierSource idSource = getSource(identifierId);

        location = getLocation(location);

        // generate identifier if necessary, otherwise validate
        if (StringUtils.isBlank(identifierString)) {
            identifierString = iss.generateIdentifier(idSource, null);
        } else {
            PatientIdentifierValidator.validateIdentifier(identifierString, idSource.getIdentifierType());
        }

        return new PatientIdentifier(identifierString, idSource.getIdentifierType(), location);
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

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setIdentifierSourceService(IdentifierSourceService iss) {
        this.iss = iss;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }
}
