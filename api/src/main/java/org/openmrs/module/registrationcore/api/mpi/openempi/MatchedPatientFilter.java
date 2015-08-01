package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;

import java.util.List;

public class MatchedPatientFilter {

    private AdministrationService adminService;

    private Integer filterIdentifierId;

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void filter(List<PatientAndMatchQuality> patients) {
        validateInitialization();

        for (PatientAndMatchQuality patientWrapper : patients) {
            if (patientWrapper.getPatient().getPatientIdentifier(filterIdentifierId) != null) {
                filterPatientsWithSameIdentifier(patientWrapper, patients);
            }
        }
    }

    private void filterPatientsWithSameIdentifier(PatientAndMatchQuality initialPatient,
                                                  List<PatientAndMatchQuality> patients) {
        String initialPatientIdentifier = initialPatient.getPatient().getPatientIdentifier(filterIdentifierId)
                .getIdentifier();

        for (PatientAndMatchQuality secondaryPatient : patients) {
            if (secondaryPatient == initialPatient)
                continue;

            PatientIdentifier secondaryPatientIdentifier = secondaryPatient.getPatient()
                    .getPatientIdentifier(filterIdentifierId);

            if (secondaryPatientIdentifier != null &&
                    secondaryPatientIdentifier.getIdentifier().equals(initialPatientIdentifier)) {
                removePatientFromList(initialPatient, secondaryPatient, patients);
            }
        }
    }

    private void removePatientFromList(PatientAndMatchQuality initialPatient,
                                       PatientAndMatchQuality secondaryPatient, List<PatientAndMatchQuality> patients) {
        if (initialPatient.getPatient() instanceof MpiPatient) {
            patients.remove(initialPatient);
        } else if (secondaryPatient.getPatient() instanceof MpiPatient) {
            patients.remove(secondaryPatient);
        } else {
            throw new APIException("Two local patients have same identifier. Identifier type: " +
                    filterIdentifierId + ". Identifier value:" +
                    initialPatient.getPatient().getPatientIdentifier(filterIdentifierId).getIdentifier());
        }
    }

    private void validateInitialization() {
        if (filterIdentifierId == null)
            initializeFilterIdentifierType();
    }

    //TODO refactor to use Global domain identifier from MpiProperties.
    private void initializeFilterIdentifierType() {
        String propertyValue = adminService.getGlobalProperty(RegistrationCoreConstants.GP_FILTER_IDENTIFIER_TYPE_ID);
        if (propertyValue == null)
            throw new APIException("Filter identifier type id is not set.");
        try {
            filterIdentifierId = Integer.valueOf(propertyValue);
        } catch (NumberFormatException e) {
            throw new APIException("Incorrect filter identifier type id. Value " + propertyValue + " is not Integer.");
        }
    }
}
