package org.openmrs.module.registrationcore.api.mpi.openempi;

import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.module.registrationcore.api.mpi.common.MpiPatient;
import org.openmrs.module.registrationcore.api.mpi.common.MpiProperties;
import org.openmrs.module.registrationcore.api.search.PatientAndMatchQuality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class MatchedPatientFilter {

    @Autowired
    @Qualifier("registrationcore.mpiProperties")
    private MpiProperties mpiProperties;

    public void filter(List<PatientAndMatchQuality> patients) {
        Integer globalIdentifierDomainId = mpiProperties.getGlobalIdentifierDomainId();

        for (PatientAndMatchQuality patientWrapper : patients) {
            if (patientWrapper.getPatient().getPatientIdentifier(globalIdentifierDomainId) != null) {
                filterPatientsWithSameIdentifier(globalIdentifierDomainId, patientWrapper, patients);
            }
        }
    }

    private void filterPatientsWithSameIdentifier(Integer globalIdentifierDomainId, PatientAndMatchQuality initialPatient,
                                                  List<PatientAndMatchQuality> patients) {
        String initialPatientIdentifier = initialPatient.getPatient().getPatientIdentifier(globalIdentifierDomainId)
                .getIdentifier();

        for (PatientAndMatchQuality secondaryPatient : patients) {
            if (secondaryPatient == initialPatient)
                continue;

            PatientIdentifier secondaryPatientIdentifier = secondaryPatient.getPatient()
                    .getPatientIdentifier(globalIdentifierDomainId);

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
            throw new APIException("Two local patients have same MPI global identifiers.");
        }
    }
}
